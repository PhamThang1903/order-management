package com.example.ordermanagement.service;

import com.example.ordermanagement.cache.OrderCacheService;
import com.example.ordermanagement.domain.Order;
import com.example.ordermanagement.domain.OrderStatus;
import com.example.ordermanagement.domain.User;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.event.OrderCreatedEvent;
import com.example.ordermanagement.event.OrderStatusChangedEvent;
import com.example.ordermanagement.repository.OrderRepository;
import com.example.ordermanagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderCacheService orderCacheService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));
        BigDecimal totalPrice = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        Order order = Order.builder()
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .user(user)
                .build();
        Order savedOrder = orderRepository.save(order);
        orderCacheService.cacheOrder(savedOrder);
        orderCacheService.incrementTodayOrderCount();
        eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder));
        return OrderResponse.from(savedOrder);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        orderCacheService.cacheOrder(savedOrder);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, savedOrder, oldStatus, newStatus));
        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return orderCacheService.getFromCache(orderId)
                .map(OrderResponse::from)
                .orElseGet(() -> {
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
                    orderCacheService.cacheOrder(order);
                    return OrderResponse.from(order);
                });
    }
}
