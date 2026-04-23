package it.codingjam.springbootmqttpoc.handler;

import it.codingjam.springbootmqttpoc.config.MqttProperties;
import it.codingjam.springbootmqttpoc.service.MessageStorage;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.MqttMessage;
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
            MqttProperties.TopicConfig config = mqttProperties.subscriptions().get(getConfigKey());
            if (config != null) {
                logger.info("Subscribing {} to topic '{}' with QoS {}", getClass().getSimpleName(), config.name(), config.qos());
                // Use MqttSubscription[] overload to avoid a known infinite recursion bug
                // in Paho mqttv5 1.2.5 subscribe(String, int, IMqttMessageListener)
                MqttSubscription subscription = new MqttSubscription(config.name(), config.qos());
                mqttClient.subscribe(new MqttSubscription[]{subscription}, new IMqttMessageListener[]{this});
                logger.info("Successfully subscribed {} to topic '{}'", getClass().getSimpleName(), config.name());
            } else {
                logger.error("Config key '{}' not found in mqtt.subscriptions. Available keys: {}", getConfigKey(), mqttProperties.subscriptions().keySet());
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

            mqttClient.messageArrivedComplete(message.getId(), message.getQos());
            logger.info("{} acknowledged message from {}", getClass().getSimpleName(), topic);
        } catch (Exception e) {
            logger.error("{} failed to process message: {}", getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}
