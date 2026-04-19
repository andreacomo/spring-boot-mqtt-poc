package it.codingjam.springbootmqttpoc.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MqttClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqttClientConfig.class);

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

        return client;
    }
}
