package com.trulybare.dto;

import com.trulybare.enums.OrderStatus;
import com.trulybare.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItemDto> items = new ArrayList<>();

    // Get status badge color
    public String getStatusBadgeClass() {
        return switch (status) {
            case PENDING -> "bg-warning";
            case CONFIRMED -> "bg-info";
            case SHIPPED -> "bg-primary";
            case OUT_FOR_DELIVERY -> "bg-info";
            case DELIVERED -> "bg-success";
            case CANCELLED -> "bg-danger";
            case RETURNED -> "bg-secondary";
        };
    }

    // Check if order can be cancelled
    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    // Check if order can be returned
    public boolean isReturnable() {
        return status == OrderStatus.DELIVERED;
    }
}