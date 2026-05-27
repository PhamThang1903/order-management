package com.example.ordermanagement.integration;

import com.example.ordermanagement.domain.User;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.repository.UserRepository;
import com.example.ordermanagement.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@EmbeddedKafka(
        partitions = 3,
        topics = {"order-events"}
)
public class OrderIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateOrderFullFlow() {
        User user = User.builder()
                .name("Admin")
                .email("admin@gmail.com")
                .build();

        user = userRepository.save(user);

        CreateOrderRequest request = CreateOrderRequest.builder()
                .productName("Laptop Gaming")
                .quantity(1)
                .unitPrice(new BigDecimal(25000000))
                .userId(user.getId())
                .build();

        OrderResponse response = orderService.createOrder(request);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getProductName()).isEqualTo("Laptop Gaming");
        assertThat(response.getUserId()).isEqualTo(user.getId());
    }
}
