package com.trulybare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}