package it.codingjam.springbootmqttpoc.handler;

import it.codingjam.springbootmqttpoc.config.MqttProperties;
import it.codingjam.springbootmqttpoc.service.MessageStorage;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;

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
    public void subscribe() throws Exception {
        MqttProperties.TopicConfig config = mqttProperties.getSubscriptions().get(getConfigKey());
        if (config != null) {
            logger.info("Subscribing {} to {} with QoS {}", getClass().getSimpleName(), config.getName(), config.getQos());
            mqttClient.subscribe(config.getName(), config.getQos(), this);
        } else {
            logger.warn("Config key {} not found in mqtt.subscriptions", getConfigKey());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String payload = new String(message.getPayload());
            logger.info("{} received: topic={}, message={}", getClass().getSimpleName(), topic, payload);

            handleMessage(payload);
            messageStorage.store(topic, payload);

            mqttClient.messageArrivedComplete(message.getId(), message.getQos());
            logger.info("{} acknowledged message from {}", getClass().getSimpleName(), topic);
        } catch (Exception e) {
            logger.error("{} failed to process message: {}", getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
