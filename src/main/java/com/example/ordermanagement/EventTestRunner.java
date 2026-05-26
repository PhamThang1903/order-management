package com.example.ordermanagement;

import com.example.ordermanagement.domain.OrderStatus;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.service.OrderService;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;

import java.math.BigDecimal;

@CommandNaming
@RequiredArgsConstructor
public class EventTestRunner implements CommandLineRunner {

    private final OrderService orderService;

    @Override
    public void run(String... args) throws Exception {
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                "This is product name",
                1000,
                BigDecimal.valueOf(1L),
                100L
        );
        orderService.createOrder(orderRequest);
        orderService.updateStatus(1L, OrderStatus.CONFIRMED);
    }
}
