package it.codingjam.springbootmqttpoc.handler;

import it.codingjam.springbootmqttpoc.config.MqttProperties;
import it.codingjam.springbootmqttpoc.service.MessageStorage;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

public abstract class MqttMessageHandlerBase implements IMqttMessageListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final MessageStorage messageStorage;
    protected final MqttClient mqttClient;
    protected final MqttProperties mqttProperties;

    public MqttMessageHandlerBase(MessageStorage messageStorage, MqttClient mqttClient, MqttProperties mqttProperties) {
        this.messageStorage = messageStorage;
        this.mqttClient = mqttClient;
        this.mqttProperties = mqttProperties;
    }

    protected abstract String getConfigKey();

    protected abstract void handleMessage(String payload) throws Exception;

    @PostConstruct
    public void subscribe() {
        try {
            logger.info("{} @PostConstruct subscribe() called", getClass().getSimpleName());
            MqttProperties.TopicConfig config = mqttProperties.getSubscriptions().get(getConfigKey());
            if (config != null) {
                logger.info("Subscribing {} to topic '{}' with QoS {}", getClass().getSimpleName(), config.getName(), config.getQos());
                mqttClient.subscribe(config.getName(), config.getQos(), this);
                logger.info("Successfully subscribed {} to topic '{}'", getClass().getSimpleName(), config.getName());
            } else {
                logger.error("Config key '{}' not found in mqtt.subscriptions. Available keys: {}", getConfigKey(), mqttProperties.getSubscriptions().keySet());
            }
        } catch (Exception e) {
            logger.error("{} failed to subscribe: {}", getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.info("{} received: topic={}, message={}", getClass().getSimpleName(), topic, payload);

        try {
            handleMessage(payload);
            logger.info("{} finished processing message from {}", getClass().getSimpleName(), topic);
            
            messageStorage.store(topic, payload);
            
            try {
                mqttClient.messageArrivedComplete(message.getId(), message.getQos());
                logger.info("{} acknowledged message from {}", getClass().getSimpleName(), topic);
            } catch (Exception ackException) {
                logger.error("{} failed to acknowledge message: {}", getClass().getSimpleName(), ackException.getMessage(), ackException);
                throw ackException;
            }
        } catch (Exception e) {
            logger.error("{} failed to process message: {}", getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}
