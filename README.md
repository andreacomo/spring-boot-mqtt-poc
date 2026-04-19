# Spring Boot MQTT PoC

Small sample project that integrates Spring Boot and MQTT (Eclipse Paho) to:
- publish messages to MQTT topics via REST,
- subscribe to configured topics,
- read the latest received message per topic.

## Stack

- Java 21
- Spring Boot 4
- Eclipse Paho MQTT Client
- Maven Wrapper (`./mvnw`)
- Eclipse Mosquitto (via Docker Compose)

## Quick Start

### 1) Start the MQTT broker

```bash
docker compose up -d
```

The broker exposes:
- `1883` (MQTT)
- `9883` (Mosquitto HTTP API/dashboard, if enabled)

### 2) Start the Spring Boot app

```bash
./mvnw spring-boot:run
```

By default, the app uses:
- broker: `tcp://localhost:1883`
- client id: `spring-mqtt-poc`
- subscribed topics: `commands` (QoS 1), `alerts` (QoS 2)

## Main REST APIs

Base path: `/mqtt`

### Connection status

```bash
curl -s http://localhost:8080/mqtt/status
```

### Publish message

```bash
curl -s -X POST http://localhost:8080/mqtt/publish \
  -H 'Content-Type: application/json' \
  -d '{"topic":"commands","message":"hello"}'
```

For the `alerts` topic, QoS 2 is used; QoS 1 is used for all other topics.

### Read latest received message for a topic

```bash
curl -s http://localhost:8080/mqtt/message/commands
```

## Configuration

Main configuration is in `src/main/resources/application.yaml`:
- `mqtt.broker-url`
- `mqtt.client-id`
- `mqtt.subscriptions`

Local Mosquitto broker configuration is in:
- `docker-compose.yml`
- `mosquitto/mosquitto.conf`

## Notes

- This repository is a PoC (not production-hardened).
- Broker persistence is enabled (`persistence true`) to keep sessions/QoS 1-2 messages across broker restarts.


