package com.trulybare.service;

import com.trulybare.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductService {

    // Get all active products
    List<Product> getAllProducts();

    // Get products with pagination
    Page<Product> getAllProductsPaginated(Pageable pageable);

    // Get single product by ID
    Product getProductById(Long productId);

    // Get products by category
    List<Product> getProductsByCategory(String category);

    // Get featured products for homepage
    List<Product> getFeaturedProducts();

    // Get best seller products for homepage
    List<Product> getBestSellerProducts();

    // Get new arrival products for homepage
    List<Product> getNewArrivalProducts();

    // Get classic products for homepage
    List<Product> getClassicProducts();

    // Search products by keyword
    List<Product> searchProducts(String keyword);

    // Admin: Save or update product
    Product saveProduct(Product product);

    // Admin: Delete product
    void deleteProduct(Long productId);

    // Update stock after order
    void updateStock(Long productId, int quantity);

    // Check if product has sufficient stock
    boolean hasSufficientStock(Long productId, int quantity);
}