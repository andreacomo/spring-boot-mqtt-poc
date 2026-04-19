package it.codingjam.springbootmqttpoc.model;

public class StatusResponse {
    private boolean connected;
    private String brokerUrl;
    private String clientId;

    public StatusResponse(boolean connected, String brokerUrl, String clientId) {
        this.connected = connected;
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
