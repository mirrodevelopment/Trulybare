package com.trulybare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ritual_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RitualCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 300)
    private String buttonLink;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
