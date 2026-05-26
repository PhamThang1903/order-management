package com.example.ordermanagement.kafka;

import com.example.ordermanagement.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventProducer {

    public void publishOrderCreated(Order order) {
        log.info("Kafka stub publish ORDER_CREATED: orderId={}", order.getId());
    }
}
