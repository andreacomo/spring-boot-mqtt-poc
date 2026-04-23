package it.codingjam.springbootmqttpoc.service;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MqttPublisher {
    private static final Logger logger = LoggerFactory.getLogger(MqttPublisher.class);
    private final MqttClient mqttClient;

    public MqttPublisher(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void publish(String topic, String message, int qos) throws MqttException {
        try {
            if (!mqttClient.isConnected()) {
                throw new MqttException(new Exception("Broker not connected"));
            }

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(qos);
            mqttClient.publish(topic, mqttMessage);

            logger.info("Published message to topic {}: {}", topic, message);
        } catch (MqttException e) {
            logger.error("Failed to publish message to {}: {}", topic, e.getMessage(), e);
            throw e;
        }
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }
}

