package it.codingjam.springbootmqttpoc.model;

import java.time.OffsetDateTime;

public class MqttMessage {
    private String topic;
    private String message;
    private OffsetDateTime receivedAt;

    public MqttMessage(String topic, String message, OffsetDateTime receivedAt) {
        this.topic = topic;
        this.message = message;
        this.receivedAt = receivedAt;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
