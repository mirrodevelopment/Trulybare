package com.trulybare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "admin_name", length = 100)
    private String adminName;

    @Column(name = "action_type", length = 60, nullable = false)
    private String actionType; // STATUS_CHANGE, REFUND_INITIATED, REFUND_RESULT, REJECTION, REPLACEMENT_APPROVED, ADMIN_MESSAGE

    @Column(name = "target_type", length = 40)
    private String targetType; // RETURN_REQUEST, ORDER, USER, etc.

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "old_value", length = 100)
    private String oldValue;

    @Column(name = "new_value", length = 100)
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
