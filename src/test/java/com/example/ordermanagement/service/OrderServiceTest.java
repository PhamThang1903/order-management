package com.example.ordermanagement.service;

import com.example.ordermanagement.cache.OrderCacheService;
import com.example.ordermanagement.domain.Order;
import com.example.ordermanagement.domain.OrderStatus;
import com.example.ordermanagement.domain.User;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.event.OrderCreatedEvent;
import com.example.ordermanagement.repository.OrderRepository;
import com.example.ordermanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderCacheService orderCacheService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderSuccessfully() {
        User user = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@gmail.com")
                .build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .productName("LaptopGaming")
                .quantity(1)
                .unitPrice(new BigDecimal(25000000))
                .userId(1L)
                .build();
        Order savedOrder = Order.builder()
                .id(1L)
                .productName("LaptopGaming")
                .quantity(1)
                .totalPrice(new BigDecimal(25000000))
                .status(OrderStatus.PENDING)
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getId()).isEqualTo(1L);

        assertThat(response.getProductName()).isEqualTo("LaptopGaming");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderCacheService).cacheOrder(savedOrder);
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }
}
