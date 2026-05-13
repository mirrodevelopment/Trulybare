package com.trulybare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private com.trulybare.entity.Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private com.trulybare.entity.Product product;

    private Integer quantity = 1;
}