package it.codingjam.springbootmqttpoc.config;

import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MqttClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqttClientConfig.class);

    @Bean(destroyMethod = "disconnect")
    public MqttClient mqttClient(it.codingjam.springbootmqttpoc.config.MqttProperties mqttProperties) throws MqttException {
        // Use MemoryPersistence for stateless K8s-ready architecture
        // Broker (Mosquitto) handles message durability for QoS 1/2 with cleanStart=false
        MqttClient client = new MqttClient(
            mqttProperties.brokerUrl(),
            mqttProperties.clientId(),
            new MemoryPersistence()
        );

        // Add connection callback to track connection lifecycle (MQTT5 MqttCallback interface)
        client.setCallback(new MqttCallback() {
            @Override
            public void disconnected(MqttDisconnectResponse response) {
                logger.warn("MQTT disconnected: {}", response.getReasonString());
            }

            @Override
            public void mqttErrorOccurred(MqttException e) {
                logger.error("MQTT error occurred: {}", e.getMessage(), e);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                logger.debug("Message arrived on topic: {}", topic);
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
                logger.debug("Message delivery complete");
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                logger.info("MQTT connect complete (reconnect={}): {}", reconnect, serverURI);
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {
                logger.debug("Auth packet arrived, reasonCode={}", reasonCode);
            }
        });

        it.codingjam.springbootmqttpoc.config.MqttProperties.ConnectionOptions co = mqttProperties.connection();
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setCleanStart(co.cleanStart());
        options.setSessionExpiryInterval(co.sessionExpiryInterval());
        options.setConnectionTimeout(co.connectionTimeout());
        options.setKeepAliveInterval(co.keepAliveInterval());
        options.setAutomaticReconnect(co.automaticReconnect());
        options.setAutomaticReconnectDelay(1, co.maxReconnectDelay());
        options.setReceiveMaximum(co.receiveMaximum());

        client.setManualAcks(true);

        logger.info("Connecting to MQTT broker at {} with clientId {}", mqttProperties.brokerUrl(), mqttProperties.clientId());
        try {
            client.connect(options);
            logger.info("Successfully connected to MQTT broker");
        } catch (MqttException e) {
            logger.warn("Failed to connect to MQTT broker at {}. Will attempt automatic reconnection.",
                mqttProperties.brokerUrl(), e);
            // The client will attempt to reconnect automatically with setAutomaticReconnect(true)
        }

        return client;
    }
}
