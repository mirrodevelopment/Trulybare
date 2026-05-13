package com.trulybare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    // USER or ADMIN
    @Column(name = "sender_type", nullable = false, length = 10)
    private String senderType;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
