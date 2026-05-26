package com.example.ordermanagement.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

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
        Object orderId = event.get("orderId");
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add("recent:orders", orderId, score);
        log.info("Saved ORDER_CREATED to Redis ZSet: key=recent:orders, orderId={}, score={}", orderId, score);
    }

    private void handleStatusChanged(Map<String, Object> event) {
        String orderId = String.valueOf(event.get("orderId"));
        Object newStatus = event.get("newStatus");

        redisTemplate.opsForHash().put("order:status", orderId, newStatus);
        log.info("Saved STATUS_CHANGED to Redis hash: key=order:status, orderId={}, newStatus={}", orderId, newStatus);
    }
}
