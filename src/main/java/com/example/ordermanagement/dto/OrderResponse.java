package com.example.ordermanagement.dto;

import com.example.ordermanagement.domain.Order;
import com.example.ordermanagement.domain.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String productName;
    private int quantity;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private Long userId;
    private String userEmail;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .createAt(order.getCreateAt())
                .updateAt(order.getUpdateAt())
                .build();
    }
}
