package com.trulybare.entity;

import com.trulybare.enums.OrderStatus;
import com.trulybare.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal shippingFees;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column
    private Integer pointsUsed = 0;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToMany(mappedBy = "order")
    private List<Return> returns = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    private List<ReturnRequest> returnRequests = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Column(columnDefinition = "TEXT")
    private String returnReason;

    @PrePersist
    public void generateOrderNumber() {
        this.orderNumber = "TB-ORD-" + (int)(Math.random() * 9000 + 1000);
    }

    public boolean isReturnable() {
        if (this.status == OrderStatus.DELIVERED && this.updatedAt != null) {
            return this.updatedAt.plusDays(7).isAfter(java.time.LocalDateTime.now());
        }
        return false;
    }

    public boolean isReorderable() {
        if (this.status == OrderStatus.CANCELLED && this.updatedAt != null) {
            return this.updatedAt.plusDays(3).isAfter(java.time.LocalDateTime.now());
        }
        return false;
    }
}