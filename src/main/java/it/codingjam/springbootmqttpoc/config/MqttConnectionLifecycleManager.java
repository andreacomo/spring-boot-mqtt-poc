package it.codingjam.springbootmqttpoc.config;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages MQTT connection lifecycle after Spring application startup.
 * <p>
 * The first connection attempt starts on {@link ApplicationReadyEvent}. If the broker is unavailable,
 * this component keeps retrying with exponential backoff. After the first successful connect,
 * runtime reconnect behavior is handled by Paho automatic reconnect.
 */
public class MqttConnectionLifecycleManager implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MqttConnectionLifecycleManager.class);

    private final MqttClient mqttClient;
    private final MqttConnectionOptions options;
    private final String brokerUrl;
    private final boolean enabled;
    private final int maxReconnectDelaySeconds;
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);

    private volatile Thread retryThread;

    MqttConnectionLifecycleManager(
            MqttClient mqttClient,
            MqttConnectionOptions options,
            String brokerUrl,
            boolean enabled,
            int maxReconnectDelaySeconds) {
        this.mqttClient = mqttClient;
        this.options = options;
        this.brokerUrl = brokerUrl;
        this.enabled = enabled;
        this.maxReconnectDelaySeconds = Math.max(1, maxReconnectDelaySeconds);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Application is ready, starting MQTT connection lifecycle for broker {}", brokerUrl);
        start();
    }

    void start() {
        if (!enabled || mqttClient.isConnected() || stopped.get() || !started.compareAndSet(false, true)) {
            return;
        }

        retryThread = Thread.ofVirtual()
                .name("mqtt-connection")
                .start(this::tryConnect);
    }

    void stop() {
        if (stopped.compareAndSet(false, true)) {
            Thread localRetryThread = retryThread;
            if (localRetryThread != null) {
                localRetryThread.interrupt();
            }
            try {
                if (mqttClient.isConnected()) {
                    logger.info("Disconnecting MQTT client from {}", brokerUrl);
                    mqttClient.disconnect();
                }
            } catch (MqttException e) {
                logger.warn("Error disconnecting MQTT client from {}", brokerUrl, e);
            }
        }
    }

    private void tryConnect() {
        int delaySeconds = 1;

        while (!stopped.get() && !mqttClient.isConnected()) {
            try {
                logger.info("Attempting MQTT connection to {}", brokerUrl);
                mqttClient.connect(options);
                logger.info("Successfully connected to MQTT broker at {}", brokerUrl);
                return;
            } catch (MqttException e) {
                logger.warn(
                        "MQTT connection to {} failed. Retrying in {} second(s)",
                        brokerUrl,
                        delaySeconds,
                        e
                );
            }

            try {
                TimeUnit.SECONDS.sleep(delaySeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            delaySeconds = Math.min(delaySeconds * 2, maxReconnectDelaySeconds);
        }
    }
}



