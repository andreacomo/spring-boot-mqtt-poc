package it.codingjam.springbootmqttpoc.config;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MqttClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqttClientConfig.class);

    @Bean(destroyMethod = "disconnect")
    public MqttClient mqttClient(MqttProperties mqttProperties) throws MqttException {
        // Use MemoryPersistence for stateless K8s-ready architecture
        // Broker (Mosquitto) handles message durability for QoS 1/2 with cleanSession=false
        MqttClient client = new MqttClient(
            mqttProperties.brokerUrl(),
            mqttProperties.clientId(),
            new MemoryPersistence()
        );

        // Add connection callback to track connection lifecycle
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.warn("MQTT connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                logger.debug("Message arrived on topic: {}", topic);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.debug("Message delivery complete");
            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setMaxReconnectDelay(10000);

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
