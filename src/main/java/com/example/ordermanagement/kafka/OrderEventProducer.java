package com.example.ordermanagement.kafka;

import com.example.ordermanagement.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.order.topic}")
    private String orderTopic;

    public void publishOrderCreated(Order order) {
        String key = String.valueOf(order.getUser().getId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "ORDER_CREATED");
        payload.put("orderId", order.getId());
        payload.put("userId", order.getUser().getId());
        payload.put("productName", order.getProductName());
        payload.put("totalPrice", order.getTotalPrice());
        payload.put("timestamp", LocalDateTime.now());

        kafkaTemplate.send(orderTopic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka produced ORDR_CREATED: topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Kafka produce ORDER_CREATED failed: orderId={}",
                                order.getId(), ex);
                    }
                });
    }
}
