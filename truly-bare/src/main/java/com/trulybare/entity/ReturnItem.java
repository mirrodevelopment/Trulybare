package com.trulybare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "return_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnItemId;

    @ManyToOne
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnEntity;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;

    private String reason;
}
