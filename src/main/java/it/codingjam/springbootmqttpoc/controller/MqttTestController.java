package it.codingjam.springbootmqttpoc.controller;

import it.codingjam.springbootmqttpoc.config.MqttProperties;
import it.codingjam.springbootmqttpoc.model.MqttMessage;
import it.codingjam.springbootmqttpoc.model.PublishRequest;
import it.codingjam.springbootmqttpoc.model.PublishResponse;
import it.codingjam.springbootmqttpoc.model.StatusResponse;
import it.codingjam.springbootmqttpoc.model.ErrorResponse;
import it.codingjam.springbootmqttpoc.service.MqttPublisher;
import it.codingjam.springbootmqttpoc.service.MessageStorage;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/mqtt")
public class MqttTestController {
    private static final Logger logger = LoggerFactory.getLogger(MqttTestController.class);

    private final MqttPublisher mqttPublisher;
    private final MessageStorage messageStorage;
    private final MqttProperties mqttProperties;

    public MqttTestController(MqttPublisher mqttPublisher, MessageStorage messageStorage, MqttProperties mqttProperties) {
        this.mqttPublisher = mqttPublisher;
        this.messageStorage = messageStorage;
        this.mqttProperties = mqttProperties;
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody PublishRequest request) {
        try {
            if (request.getTopic() == null || request.getTopic().isEmpty() || request.getMessage() == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("topic and message are required"));
            }

            int qos = "alerts".equals(request.getTopic()) ? 2 : 1;
            mqttPublisher.publish(request.getTopic(), request.getMessage(), qos);

            return ResponseEntity.ok(new PublishResponse(true, request.getTopic(), request.getMessage()));
        } catch (MqttException e) {
            logger.error("Publish failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Broker unavailable"));
        }
    }

    @GetMapping("/message/{topic}")
    public ResponseEntity<?> getMessage(@PathVariable String topic) {
        try {
            if (!mqttPublisher.isConnected()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse("Broker not connected"));
            }

            if (!messageStorage.exists(topic)) {
                return ResponseEntity.notFound().build();
            }

            MqttMessage msg = messageStorage.retrieve(topic);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            logger.error("Failed to retrieve message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Broker unavailable"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        try {
            boolean connected = mqttPublisher.isConnected();

            if (!connected) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse("Broker not connected"));
            }

            return ResponseEntity.ok(new StatusResponse(true, mqttProperties.brokerUrl(), mqttProperties.clientId()));
        } catch (Exception e) {
            logger.error("Status check failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Status check failed"));
        }
    }
}
