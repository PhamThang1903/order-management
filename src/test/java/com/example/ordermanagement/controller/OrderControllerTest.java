package com.example.ordermanagement.controller;

import com.example.ordermanagement.cache.OrderCacheService;
import com.example.ordermanagement.domain.OrderStatus;
import com.example.ordermanagement.dto.CreateOrderRequest;
import com.example.ordermanagement.dto.OrderResponse;
import com.example.ordermanagement.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderCacheService cacheService;

    @Test
    void shouldReturn201WhenCreateOrderValid() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .productName("Laptop Gaming")
                .quantity(1)
                .totalPrice(new BigDecimal(25000000))
                .status(OrderStatus.PENDING)
                .userId(1L)
                .userEmail("admin@gmail.com")
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "productName": "Laptop Gaming",
                            "quantity": 1,
                            "unitPrice": 25000000,
                            "userId": 1
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop Gaming"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userId").value(1));
        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }
}
