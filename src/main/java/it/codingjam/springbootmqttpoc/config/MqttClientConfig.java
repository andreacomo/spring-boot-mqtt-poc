package it.codingjam.springbootmqttpoc.config;

import it.codingjam.springbootmqttpoc.handler.MqttMessageHandlerBase;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Configuration
public class MqttClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqttClientConfig.class);

    @Bean
    public MqttClient mqttClient(it.codingjam.springbootmqttpoc.config.MqttProperties mqttProperties,
                                 ObjectProvider<List<MqttMessageHandlerBase>> listenersProvider) throws MqttException {

        // Use MemoryPersistence for stateless K8s-ready architecture.
        // Broker handles message durability for QoS 1/2 with cleanStart=false.
        MqttClient client = new MqttClient(
                mqttProperties.brokerUrl(),
                mqttProperties.clientId(),
                new MemoryPersistence()
        );

        client.setManualAcks(true);
        client.setCallback(newCallback(listenersProvider));

        logger.info("Configured MQTT client for broker {} with clientId {}", mqttProperties.brokerUrl(), mqttProperties.clientId());

        return client;
    }

    @Bean(destroyMethod = "stop")
    public MqttConnectionLifecycleManager mqttConnectionLifecycleManager(
            MqttClient mqttClient,
            it.codingjam.springbootmqttpoc.config.MqttProperties mqttProperties) {
        return new MqttConnectionLifecycleManager(
                mqttClient,
                getMqttConnectionOptions(mqttProperties),
                mqttProperties.brokerUrl(),
                mqttProperties.connection().automaticReconnect(),
                mqttProperties.connection().maxReconnectDelay()
        );
    }

    private static @NonNull MqttCallback newCallback(ObjectProvider<List<MqttMessageHandlerBase>> listenersProvider) {
        return new MqttCallback() {
            @Override
            public void disconnected(MqttDisconnectResponse response) {
                logger.warn("MQTT client disconnected: {}", response.getReasonString());
            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                logger.error("MQTT error occurred", exception);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                // Handled by per-subscription IMqttMessageListener callbacks
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
                // Used by publisher side
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    logger.info("Reconnected to MQTT broker at {}. Resubscribing listeners...", serverURI);
                } else {
                    logger.info("Connected to MQTT broker at {}. Subscribing listeners...", serverURI);
                }
                List<MqttMessageHandlerBase> listeners = listenersProvider.getIfAvailable();
                if (listeners != null) {
                    listeners.forEach(MqttMessageHandlerBase::subscribe);
                }
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {
                // Not used
            }
        };
    }

    private static MqttConnectionOptions getMqttConnectionOptions(
            it.codingjam.springbootmqttpoc.config.MqttProperties mqttProperties) {
        it.codingjam.springbootmqttpoc.config.MqttProperties.ConnectionOptions co = mqttProperties.connection();
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setCleanStart(co.cleanStart());
        options.setSessionExpiryInterval(co.sessionExpiryInterval());
        options.setConnectionTimeout(co.connectionTimeout());
        options.setKeepAliveInterval(co.keepAliveInterval());
        options.setAutomaticReconnect(co.automaticReconnect());
        options.setAutomaticReconnectDelay(1, co.maxReconnectDelay());
        options.setReceiveMaximum(co.receiveMaximum());
        return options;
    }
}
