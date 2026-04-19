package it.codingjam.springbootmqttpoc.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PreDestroy;

@Configuration
public class MqttClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqttClientConfig.class);
    private MqttClient mqttClient;

    @Bean
    public MqttClient mqttClient(MqttProperties mqttProperties) throws MqttException {
        MqttClient client = new MqttClient(
            mqttProperties.getBrokerUrl(),
            mqttProperties.getClientId(),
            new MemoryPersistence()
        );

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);

        client.setManualAcks(true);

        logger.info("Connecting to MQTT broker at {}", mqttProperties.getBrokerUrl());
        client.connect(options);
        logger.info("Connected to MQTT broker");

        this.mqttClient = client;
        return client;
    }

    @PreDestroy
    public void shutdown() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            logger.info("Disconnecting from MQTT broker");
            mqttClient.disconnect();
            logger.info("Disconnected from MQTT broker");
        }
    }
}
