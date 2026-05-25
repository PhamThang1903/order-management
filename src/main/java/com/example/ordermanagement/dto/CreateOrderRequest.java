package com.example.ordermanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Ten san pham khong duoc de trong")
    @Size(min = 2, max = 100, message = "Ten san pham phai tu 2 - 100 ky tu")
    private String productName;

    @Min(value = 1, message = "so luong toi thieu la 1")
    @Max(value = 1000, message = "So luong toi da la 1000")
    private int quantity;

    @NotNull(message = "Don gia khong duoc null")
    @DecimalMin(value = "0.01", message = "Don gia phai lon hon hoac bang 0.01")
    private BigDecimal unitPrice;

    @NotNull(message = "user id khong duoc null")
    @Positive(message = "user id phai la so duong")
    private Long userId;

}
