package it.codingjam.springbootmqttpoc.model;

public class PublishResponse {
    private boolean success;
    private String topic;
    private String message;

    public PublishResponse(boolean success, String topic, String message) {
        this.success = success;
        this.topic = topic;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
}
