package it.codingjam.springbootmqttpoc.handler;

import it.codingjam.springbootmqttpoc.config.MqttProperties;
import it.codingjam.springbootmqttpoc.service.MessageStorage;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CommandMessageHandler extends MqttMessageHandlerBase {
    private static final Logger logger = LoggerFactory.getLogger(CommandMessageHandler.class);

    public CommandMessageHandler(MessageStorage messageStorage, MqttClient mqttClient, MqttProperties mqttProperties) {
        super(messageStorage, mqttClient, mqttProperties);
        logger.info("CommandMessageHandler constructed");
    }

    @Override
    protected String getConfigKey() {
        return "commands";
    }

    @Override
    protected void handleMessage(String payload) throws Exception {
        logger.info("Executing command: {}", payload);
        // Add business logic here
    }
}
