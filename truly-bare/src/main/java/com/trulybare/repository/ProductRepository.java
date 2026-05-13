package com.trulybare.repository;

import com.trulybare.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    // Find all active products
    List<Product> findByIsActiveTrue();

    // Find featured products for homepage
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();
    List<Product> findByIsFeaturedTrue();

    // Find best seller products for homepage
    List<Product> findByIsBestSellerTrueAndIsActiveTrue();
    List<Product> findByIsBestSellerTrue();

    // Find new arrival products
    List<Product> findByIsNewTrueAndIsActiveTrue();

    // Find classic products
    List<Product> findByIsClassicTrueAndIsActiveTrue();

    // Find products by category name
    List<Product> findByCategory_Name(String categoryName);

    // Search products by name (case-insensitive)
    List<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword);
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Find products by category and active status
    List<Product> findByCategory_NameAndIsActiveTrue(String categoryName);

    // Get top 3 products for preview
    List<Product> findTop3ByCategory_NameAndIsActiveTrue(String categoryName);

    // Get all products in a category by ID
    List<Product> findByCategory_CategoryIdAndIsActiveTrue(Long categoryId);
}