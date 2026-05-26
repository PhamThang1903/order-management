package com.example.ordermanagement;

import com.example.ordermanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AopTestRunner implements CommandLineRunner {

    private final OrderService orderService;

    @Override
    public void run(String... args) throws Exception {
//        try {
//            orderService.getOrder(999L);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
}
