package com.trulybare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long productId;
    private String name;
    private String description;
    private String ingredients;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private Integer stockQuantity;
    private Boolean isFeatured;
    private Boolean isBestSeller;
    private Double averageRating;
    private Long reviewCount;
}