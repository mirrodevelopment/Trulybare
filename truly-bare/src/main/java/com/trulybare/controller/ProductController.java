package com.trulybare.controller;

import com.trulybare.entity.Product;
import com.trulybare.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.trulybare.repository.CategoryRepository categoryRepository;

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String category,
                           @RequestParam(required = false) String search,
                           Model model) {
        List<Product> products;

        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
            model.addAttribute("isSearch", true);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);

        // Fetch selected category object for heading/subheading
        if (category != null && !category.isEmpty()) {
            categoryRepository.findByName(category).ifPresent(c -> {
                model.addAttribute("activeCategory", c);
                if ("COSMETICS".equalsIgnoreCase(category)) {
                    // Only show Skin Care and Hair Care tabs under Cosmetics
                    List<com.trulybare.entity.Category> cosmeticTabs = new java.util.ArrayList<>();
                    categoryRepository.findByName("SKIN CARE").ifPresent(cosmeticTabs::add);
                    categoryRepository.findByName("HAIR CARE").ifPresent(cosmeticTabs::add);
                    model.addAttribute("subCategories", cosmeticTabs);
                } else if (c.getSubCategories() != null && !c.getSubCategories().isEmpty()) {
                    model.addAttribute("subCategories", c.getSubCategories());
                }
            });
        }

        // Build the filter tabs for the sidebar — for COSMETICS show only SKIN CARE + HAIR CARE
        List<com.trulybare.entity.Category> dynamicCategories;
        if ("COSMETICS".equalsIgnoreCase(category)) {
            dynamicCategories = new java.util.ArrayList<>();
            categoryRepository.findByName("SKIN CARE").ifPresent(dynamicCategories::add);
            categoryRepository.findByName("HAIR CARE").ifPresent(dynamicCategories::add);
        } else {
            dynamicCategories = productService.getAllProducts().stream()
                    .map(p -> p.getCategory())
                    .filter(c -> c != null)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        }
        model.addAttribute("categories", dynamicCategories);

        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/api/products/search")
    @ResponseBody
    public List<Product> searchApi(@RequestParam String query) {
        return productService.searchProducts(query);
    }
}