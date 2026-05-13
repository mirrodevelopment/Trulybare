package com.trulybare.entity;

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
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(length = 200)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String usageInstructions;

    @Column(columnDefinition = "TEXT")
    private String warnings;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;

    private Integer stockQuantity = 0;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(columnDefinition = "LONGTEXT")
    private String subImageUrl1;

    @Column(columnDefinition = "LONGTEXT")
    private String subImageUrl2;

    @Column(columnDefinition = "LONGTEXT")
    private String subImageUrl3;

    @Column(columnDefinition = "LONGTEXT")
    private String subImageUrl4;

    private Boolean isFeatured = false;
    private Boolean isBestSeller = false;
    private Boolean isNew = false;
    private Boolean isClassic = false;
    private Boolean isActive = true;
    private Boolean forceSoldOut = false;

    // Skin & Hair specific categorisation
    private String skinType; // e.g., "Oily, Dry, Sensitive, Combo"
    private String hairType; // e.g., "Straight, Wavy, Curly, Coily"

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "product")
    private List<com.trulybare.entity.Review> reviews = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "product")
    private List<com.trulybare.entity.OrderItem> orderItems = new ArrayList<>();
}