package com.trulybare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal shipping = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private int totalItems = 0;

    // Calculate totals
    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalItems = items.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();

        // Shipping: Free over ₹500, else ₹50
        this.shipping = subtotal.compareTo(new BigDecimal("500")) > 0
                ? BigDecimal.ZERO
                : new BigDecimal("50");

        this.total = subtotal.add(shipping).add(tax);
    }
}