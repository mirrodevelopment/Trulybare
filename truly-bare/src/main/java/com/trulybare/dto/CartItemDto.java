package com.trulybare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer maxStock;

    // Calculate subtotal for this item
    public BigDecimal getSubtotal() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(new BigDecimal(quantity));
    }
}