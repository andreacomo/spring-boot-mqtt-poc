package it.codingjam.springbootmqttpoc.service;

import it.codingjam.springbootmqttpoc.model.MqttMessage;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageStorage {
    private final Map<String, MqttMessage> messages = new ConcurrentHashMap<>();

    public void store(String topic, String message) {
        messages.put(topic, new MqttMessage(topic, message, OffsetDateTime.now()));
    }

    public MqttMessage retrieve(String topic) {
        return messages.get(topic);
    }

    public boolean exists(String topic) {
        return messages.containsKey(topic);
    }
}
