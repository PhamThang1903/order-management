package com.example.ordermanagement.service;

import com.example.ordermanagement.event.OrderCreatedEvent;
import com.example.ordermanagement.event.OrderStatusChangedEvent;
import com.example.ordermanagement.kafka.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final OrderEventProducer eventProducer;

    private final MessageSource messageSource;

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for orderId={}", event.getOrder().getId());
        eventProducer.publishOrderCreated(event.getOrder());
    }

    @Async
    @EventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        log.info("onStatusChanged running on thread: {}", Thread.currentThread().getName());
        String message = messageSource.getMessage(
                "order.status.changed",
                new Object[] {
                        event.getOrder().getId(),
                        event.getNewStatus()
                },
                Locale.forLanguageTag("vi")
        );

        log.info("Status changed message: {}", message);
        simulateSendEmail(event.getOrder().getUser().getEmail(), message);
    }

    private void simulateSendEmail(String email, String message) {
        log.info("Sending email to {} with message {}", email, message);
    }
}
