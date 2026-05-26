package com.example.ordermanagement.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    @KafkaListener(
            topics = "${app.order.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    public void consume(Map<String, Object> event) {
        log.info("Kafka consumed: {}", event);
        String eventType = String.valueOf(event.get("eventType"));
        switch (eventType) {
            case "ORDER_CREATED" -> handleOrderCreated(event);
            case "STATUS_CHANGED" -> handleStatusChanged(event);
            default -> log.warn("Unknown eventType: {}", eventType);
        }
    }

    private void handleOrderCreated(Map<String, Object> event) {
        log.info("Handle ORDER_CREATED event: orderId={}", event.get("orderId"));
    }

    private void handleStatusChanged(Map<String, Object> event) {
        log.info("Handle STATUS_CHANGED event: orderId={}, oldStatus={}, newStatus={}",
                event.get("orderId"),
                event.get("oldStatus"),
                event.get("newStatus"));
    }
}
