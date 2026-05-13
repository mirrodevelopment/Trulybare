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
@Table(name = "returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String returnReason;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING_REVIEW;

    @Column(columnDefinition = "TEXT")
    private String adminRemarks;

    private BigDecimal refundAmount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL)
    private List<ReturnItem> returnItems = new ArrayList<>();
}