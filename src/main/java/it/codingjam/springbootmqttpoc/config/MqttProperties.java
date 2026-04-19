package it.codingjam.springbootmqttpoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;

@ConfigurationProperties(prefix = "mqtt")
public record MqttProperties(
        String brokerUrl,
        String clientId,
        Map<String, TopicConfig> subscriptions
) {
    public record TopicConfig(
            String name,
            int qos
    ) {}
}
