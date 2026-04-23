package it.codingjam.springbootmqttpoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

@ConfigurationProperties(prefix = "mqtt")
public record MqttProperties(
        String brokerUrl,
        String clientId,
        ConnectionOptions connection,
        Map<String, TopicConfig> subscriptions
) {
    public record TopicConfig(
            String name,
            int qos
    ) {}

    public record ConnectionOptions(
            boolean cleanStart,
            long sessionExpiryInterval,
            int connectionTimeout,
            int keepAliveInterval,
            boolean automaticReconnect,
            int maxReconnectDelay,
            int receiveMaximum
    ) {}
}
