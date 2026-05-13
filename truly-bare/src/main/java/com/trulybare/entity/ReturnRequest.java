package com.trulybare.entity;

import com.trulybare.enums.ReturnStatus;
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
@Table(name = "return_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(nullable = false, length = 50)
    private String reason; // Damaged, Wrong Item, Size Issue, Quality Issue, Other

    @Column(name = "return_type", nullable = false, length = 20)
    private String returnType; // RETURN or REPLACEMENT

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReturnStatus status = ReturnStatus.PENDING_REVIEW;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "delivery_fee_deducted", precision = 10, scale = 2)
    private BigDecimal deliveryFeeDeducted;

    @Column(name = "razorpay_refund_id")
    private String razorpayRefundId;

    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "chat_open")
    private Boolean chatOpen = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ReturnMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<ReturnStatusLog> statusLogs = new ArrayList<>();
}
