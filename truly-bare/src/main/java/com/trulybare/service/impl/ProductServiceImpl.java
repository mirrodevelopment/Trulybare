package com.trulybare.service.impl;

import com.trulybare.entity.Product;
import com.trulybare.repository.ProductRepository;
import com.trulybare.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Product> getAllProductsPaginated(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        // For the COSMETICS parent, only show Skin Care and Hair Care (not Supplements/Weight Loss)
        if ("COSMETICS".equalsIgnoreCase(category)) {
            return productRepository.findByIsActiveTrue().stream()
                .filter(p -> p.getCategory() != null &&
                    ("SKIN CARE".equalsIgnoreCase(p.getCategory().getName()) ||
                     "HAIR CARE".equalsIgnoreCase(p.getCategory().getName())))
                .collect(java.util.stream.Collectors.toList());
        }
        return productRepository.findByCategory_NameAndIsActiveTrue(category);
    }

    @Override
    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue();
    }

    @Override
    public List<Product> getBestSellerProducts() {
        return productRepository.findByIsBestSellerTrueAndIsActiveTrue();
    }

    @Override
    public List<Product> getNewArrivalProducts() {
        return productRepository.findByIsNewTrueAndIsActiveTrue();
    }

    @Override
    public List<Product> getClassicProducts() {
        return productRepository.findByIsClassicTrueAndIsActiveTrue();
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);
    }

    @Override
    public Product saveProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = getProductById(productId);
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public void updateStock(Long productId, int quantity) {
        Product product = getProductById(productId);
        int newStock = product.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    @Override
    public boolean hasSufficientStock(Long productId, int quantity) {
        Product product = getProductById(productId);
        return product.getStockQuantity() >= quantity;
    }
}