package com.trulybare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.trulybare.entity.Address;
import com.trulybare.entity.User;
import com.trulybare.entity.Product;
import com.trulybare.entity.Order;
import com.trulybare.entity.Review;
import com.trulybare.service.ReviewService;
import com.trulybare.service.ProductService;
import com.trulybare.service.UserService;

import com.trulybare.repository.AddressRepository;
import com.trulybare.repository.CategoryRepository;
import com.trulybare.repository.OrderRepository;
import com.trulybare.repository.ProductRepository;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    // Services (preferred over direct repo injection)
    private final UserService userService;
    private final ReviewService reviewService;
    private final ProductService productService;
    private final com.trulybare.service.AdminService adminService;

    // Repositories used only where no matching service method exists
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // =========================================================
    // HOME PAGE
    // =========================================================

    @GetMapping({ "/", "/home" })
    public String home(Model model) {
        model.addAttribute("supplements", productRepository.findTop3ByCategory_NameAndIsActiveTrue("SUPPLEMENTS"));
        model.addAttribute("weightLoss", productRepository.findTop3ByCategory_NameAndIsActiveTrue("WEIGHT LOSS"));
        model.addAttribute("skinCare", productRepository.findTop3ByCategory_NameAndIsActiveTrue("SKIN CARE"));
        model.addAttribute("hairCare", productRepository.findTop3ByCategory_NameAndIsActiveTrue("HAIR CARE"));
        model.addAttribute("painRelief", productRepository.findTop3ByCategory_NameAndIsActiveTrue("PAIN RELIEF"));

        // Category objects for "View All" links
        model.addAttribute("supplementsCat", categoryRepository.findByName("SUPPLEMENTS").orElse(null));
        model.addAttribute("weightLossCat", categoryRepository.findByName("WEIGHT LOSS").orElse(null));
        model.addAttribute("skinCareCat", categoryRepository.findByName("SKIN CARE").orElse(null));
        model.addAttribute("hairCareCat", categoryRepository.findByName("HAIR CARE").orElse(null));
        model.addAttribute("painReliefCat", categoryRepository.findByName("PAIN RELIEF").orElse(null));
        model.addAttribute("cosmeticsCat", categoryRepository.findByName("COSMETICS").orElse(null));
        
        // Featured & Dynamic collections
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("bestSellers", productService.getBestSellerProducts());
        model.addAttribute("newArrivals", productService.getNewArrivalProducts());
        model.addAttribute("classicCollection", productService.getClassicProducts());
        
        // Dynamic Hero Banners
        model.addAttribute("slides", adminService.getActiveSlides());
        
        // Homepage Section Texts
        model.addAttribute("ritualEyebrow", adminService.getSetting("ritual_eyebrow", "The Ritual Edit"));
        model.addAttribute("ritualTitle",   adminService.getSetting("ritual_title", "Crafted for the senses"));
        model.addAttribute("ritualDesc",    adminService.getSetting("ritual_desc", "Our masterclass formulations will help you create a unique skincare ritual. Our team has done everything to ensure you can express your inner world through your daily routine."));

        // Ritual Edit Cards
        model.addAttribute("ritualCards", adminService.getActiveRitualCards());
        
        // Vlogs
        model.addAttribute("vlogs", adminService.getActiveVlogs());

        // Dynamic categories (excluding hardcoded ones)
        java.util.List<String> knownCats = java.util.Arrays.asList("SUPPLEMENTS", "WEIGHT LOSS", "SKIN CARE", "HAIR CARE", "PAIN RELIEF", "COSMETICS");
        java.util.List<com.trulybare.entity.Category> otherCategories = categoryRepository.findAll().stream()
                .filter(c -> c.getIsActive() != null && c.getIsActive() && !knownCats.contains(c.getName().toUpperCase()))
                .collect(java.util.stream.Collectors.toList());
                
        java.util.Map<com.trulybare.entity.Category, java.util.List<Product>> otherCategoriesMap = new java.util.LinkedHashMap<>();
        for (com.trulybare.entity.Category cat : otherCategories) {
            java.util.List<Product> topProds = productRepository.findTop3ByCategory_NameAndIsActiveTrue(cat.getName());
            if (topProds != null && !topProds.isEmpty()) {
                otherCategoriesMap.put(cat, topProds);
            }
        }
        model.addAttribute("otherCategoriesMap", otherCategoriesMap);

        return "home";
    }

    // =========================================================
    // PRODUCT CATEGORY PAGE
    // =========================================================

    @GetMapping("/products/category/{categoryId}")
    public String showProductsByCategory(@PathVariable Long categoryId, Model model) {
        if (categoryId == null) {
            return "redirect:/home";
        }

        com.trulybare.entity.Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null)
            return "redirect:/home";

        model.addAttribute("activeCategory", category);
        // Determine root category to keep filter tabs isolated within this tree
        com.trulybare.entity.Category rootCategory = category.getParentCategory() != null ? category.getParentCategory() : category;

        // Filter products for THIS category OR any of its active subcategories
        java.util.List<Product> allActive = productRepository.findByIsActiveTrue();
        java.util.List<Product> products = allActive.stream()
            .filter(p -> p.getCategory() != null && 
                        (p.getCategory().getCategoryId().equals(categoryId) || 
                        (p.getCategory().getParentCategory() != null && p.getCategory().getParentCategory().getCategoryId().equals(categoryId))))
            .collect(java.util.stream.Collectors.toList());

        // Always fetch subcategories of the root category so the tabs remain identical
        java.util.List<com.trulybare.entity.Category> subcategories = allActive.stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getParentCategory() != null && p.getCategory().getParentCategory().getCategoryId().equals(rootCategory.getCategoryId()))
            .map(p -> p.getCategory())
            .distinct()
            .collect(java.util.stream.Collectors.toList());

        model.addAttribute("rootCategory", rootCategory);
        model.addAttribute("products", products);
        model.addAttribute("categories", subcategories);
        return "products";
    }

    // =========================================================
    // STATIC PAGES
    // =========================================================

    @GetMapping("/legal")
    public String legal() {
        return "legal";
    }

    @GetMapping("/house-of-vegam")
    public String houseOfVegam(Model model) {
        // Fetch products to bind dynamically to the House of Vegam store section
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "house-of-vegam";
    }

    // =========================================================
    // PROFILE
    // =========================================================

    @GetMapping("/profile")
    public String showProfilePage(Principal principal, Model model) {
        if (principal == null)
            return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user", user);

        // Default address (first one)
        Address address = addressRepository.findByUserUserId(user.getUserId())
                .stream().findFirst().orElse(null);
        model.addAttribute("address", address);

        // Orders, newest first
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
        model.addAttribute("orders", orders);

        // Calculate Stats
        int orderCount = orders.size();
        java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getStatus() != com.trulybare.enums.OrderStatus.CANCELLED && o.getTotalAmount() != null) {
                totalSpent = totalSpent.add(o.getTotalAmount());
            }
        }
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalSpent", totalSpent);

        String memberStatus = user.getCustomerStatus();
        if (memberStatus == null || "Bronze".equalsIgnoreCase(memberStatus)) {
            if (orderCount >= 100) memberStatus = "Elite";
            else if (orderCount >= 50) memberStatus = "Platinum";
            else if (orderCount >= 25) memberStatus = "Gold";
            else if (orderCount >= 10) memberStatus = "Silver";
            else memberStatus = "Bronze";
        }
        model.addAttribute("memberStatus", memberStatus);

        // Points to next tier
        int nextTierOrders = orderCount < 10 ? 10 : orderCount < 25 ? 25 : orderCount < 50 ? 50 : orderCount < 100 ? 100 : -1;
        model.addAttribute("nextTierOrders", nextTierOrders);
        model.addAttribute("ordersToNextTier", nextTierOrders > 0 ? nextTierOrders - orderCount : 0);

        // All Products for Review Search (Mapped to avoid infinite recursion in JSON serialization)
        java.util.List<java.util.Map<String, Object>> productSummaries = productService.getAllProducts().stream()
            .map(p -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("productId", p.getProductId());
                map.put("name", p.getName());
                map.put("imageUrl", p.getImageUrl());
                if (p.getCategory() != null) {
                    java.util.Map<String, Object> catMap = new java.util.HashMap<>();
                    catMap.put("name", p.getCategory().getName());
                    map.put("category", catMap);
                }
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("allProducts", productSummaries);

        // User's reviews
        List<Review> reviews = reviewService.getUserReviews(user);
        model.addAttribute("reviews", reviews);
        return "profile";
    }

    @GetMapping("/loyalty-program")
    public String showLoyaltyProgram(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            model.addAttribute("user", user);
            
            // Calculate memberStatus - Prioritize Admin manual choice
            int orderCount = orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId()).size();
            String memberStatus = user.getCustomerStatus();
            if (memberStatus == null || "Bronze".equalsIgnoreCase(memberStatus)) {
                if (orderCount >= 100) memberStatus = "Elite";
                else if (orderCount >= 50) memberStatus = "Platinum";
                else if (orderCount >= 25) memberStatus = "Gold";
                else if (orderCount >= 10) memberStatus = "Silver";
                else memberStatus = "Bronze";
            }
            
            model.addAttribute("memberStatus", memberStatus);
            model.addAttribute("orderCount", orderCount);
            int nextTierOrders = orderCount < 10 ? 10 : orderCount < 25 ? 25 : orderCount < 50 ? 50 : orderCount < 100 ? 100 : -1;
            model.addAttribute("nextTierOrders", nextTierOrders);
            model.addAttribute("ordersToNextTier", nextTierOrders > 0 ? nextTierOrders - orderCount : 0);
            
            // Calculate progress percentage relative to the next tier goal
            double progress = 100.0;
            if (nextTierOrders != -1) {
                progress = (orderCount * 100.0) / nextTierOrders;
                // Cap at 100 just in case
                if (progress > 100.0) progress = 100.0;
            }
            
            model.addAttribute("progressPercentage", progress);
        }
        return "loyalty-program";
    }

    @GetMapping("/order-history")
    public String showOrderHistory(Principal principal, Model model) {
        if (principal == null)
            return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user", user);

        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
        model.addAttribute("orders", orders);

        // Calculate Stats
        int orderCount = orders.size();
        java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getStatus() != com.trulybare.enums.OrderStatus.CANCELLED && o.getTotalAmount() != null) {
                totalSpent = totalSpent.add(o.getTotalAmount());
            }
        }
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalSpent", totalSpent);

        String memberStatus = user.getCustomerStatus();
        if (memberStatus == null || "Bronze".equalsIgnoreCase(memberStatus)) {
            if (orderCount >= 100) memberStatus = "Elite";
            else if (orderCount >= 50) memberStatus = "Platinum";
            else if (orderCount >= 25) memberStatus = "Gold";
            else if (orderCount >= 10) memberStatus = "Silver";
            else memberStatus = "Bronze";
        }
        model.addAttribute("memberStatus", memberStatus);
        int nextTierOrders = orderCount < 10 ? 10 : orderCount < 25 ? 25 : orderCount < 50 ? 50 : orderCount < 100 ? 100 : -1;
        model.addAttribute("nextTierOrders", nextTierOrders);
        model.addAttribute("ordersToNextTier", nextTierOrders > 0 ? nextTierOrders - orderCount : 0);

        // Calculate progress percentage relative to the next tier goal
        double progress = 100.0;
        if (nextTierOrders != -1) {
            progress = (orderCount * 100.0) / nextTierOrders;
            // Cap at 100 just in case
            if (progress > 100.0) progress = 100.0;
        }
        model.addAttribute("progressPercentage", progress);

        return "order-history";
    }

    @GetMapping("/order/invoice/{orderId}")
    public String showInvoice(@PathVariable Long orderId, Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        if (orderId == null) return "redirect:/order-history";
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/order-history";
        }
        model.addAttribute("order", order);
        return "invoice";
    }

    @GetMapping("/order/invoice/download/{orderId}")
    public String downloadInvoice(@PathVariable Long orderId, Principal principal) {
        // For simplicity, we redirect to the invoice page which has a print trigger
        return "redirect:/order/invoice/" + orderId + "?download=true";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "addressLine1", required = false) String addressLine1,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "pincode", required = false) String pincode,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            Principal principal,
            RedirectAttributes redirectAttrs) {

        if (principal == null)
            return "redirect:/login";

        try {
            User user = userService.findByEmail(principal.getName());
            user.setFullName(fullName);
            user.setPhone(phone);

            if (gender != null && !gender.isBlank()) {
                user.setGender(com.trulybare.enums.Gender.valueOf(gender));
            }
            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                user.setDateOfBirth(LocalDate.parse(dateOfBirth));
            }

            // Use service layer — not raw repository
            userService.updateProfile(user);

            // Handle address upsert
            if (addressLine1 != null && !addressLine1.isBlank()) {
                List<Address> existing = addressRepository.findByUserUserId(user.getUserId());
                Address address = existing.isEmpty() ? new Address() : existing.get(0);
                address.setUser(user);
                address.setFullName(fullName);
                address.setPhone(phone);
                address.setAddressLine1(addressLine1);
                address.setCity(city);
                address.setState(state);
                address.setPincode(pincode);
                address.setIsDefault(true);
                addressRepository.save(address);
            }

            redirectAttrs.addFlashAttribute("successMessage", "Profile updated successfully!");
            log.info("Profile updated for: {}", principal.getName());

        } catch (Exception e) {
            log.error("Profile update failed for {}: {}", principal.getName(), e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // =========================================================
    // REVIEWS
    // =========================================================

    @GetMapping("/review/write/{productId}")
    public String showWriteReviewPage(@PathVariable Long productId,
            @RequestParam(required = false) Long orderId,
            Principal principal,
            Model model) {

        if (principal == null)
            return "redirect:/login";

        try {
            User user = userService.findByEmail(principal.getName());
            Product product = productService.getProductById(productId);

            if (product == null)
                return "redirect:/profile?error=Product+not+found";

            model.addAttribute("user", user);
            model.addAttribute("product", product);
            model.addAttribute("orderId", orderId);
            return "write-review";

        } catch (Exception e) {
            log.error("Error loading review page: {}", e.getMessage());
            return "redirect:/profile?error=Something+went+wrong";
        }
    }

    @GetMapping("/review/store")
    public String showStoreReviewPage(Principal principal, Model model) {
        if (principal == null)
            return "redirect:/login";

        // Store product is seeded via DataSeeder — just look it up
        Product storeProduct = productRepository.findByName("Signature Store Experience").orElse(null);
        if (storeProduct == null) {
            log.warn("Signature Store Experience product not found in DB. Please run DataSeeder.");
            return "redirect:/profile?error=Store+review+product+not+found";
        }
        return showWriteReviewPage(storeProduct.getProductId(), null, principal, model);
    }

    @PostMapping("/review/submit")
    public String submitReview(@RequestParam Long productId,
            @RequestParam(required = false) Long orderId,
            @RequestParam int rating,
            @RequestParam String comment,
            Principal principal,
            RedirectAttributes redirectAttrs) {

        // Guard: principal null check (security should prevent this, but be safe)
        if (principal == null)
            return "redirect:/login";

        try {
            User user = userService.findByEmail(principal.getName());
            reviewService.addReview(user, productId, orderId, rating, comment);
            redirectAttrs.addFlashAttribute("successMessage", "Your reflection was submitted!");
        } catch (Exception e) {
            log.error("Review submission failed: {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage", "Could not submit review: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/review/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId,
            Principal principal,
            RedirectAttributes ra) {

        if (principal == null)
            return "redirect:/login";

        try {
            User user = userService.findByEmail(principal.getName());

            // Ownership check: verify this review belongs to the requesting user
            boolean owned = reviewService.getUserReviews(user).stream()
                    .anyMatch(r -> r.getReviewId().equals(reviewId));

            if (!owned) {
                ra.addFlashAttribute("errorMessage", "You can only delete your own reflections.");
                return "redirect:/profile";
            }

            reviewService.deleteReview(reviewId);
            ra.addFlashAttribute("successMessage", "Reflection removed.");

        } catch (Exception e) {
            log.error("Review delete failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Could not remove reflection.");
        }

        return "redirect:/profile";
    }

    @PostMapping("/review/edit")
    public String editReview(@RequestParam Long reviewId,
            @RequestParam int rating,
            @RequestParam String comment,
            Principal principal,
            RedirectAttributes ra) {

        if (principal == null)
            return "redirect:/login";

        try {
            User user = userService.findByEmail(principal.getName());

            // Ownership check
            boolean owned = reviewService.getUserReviews(user).stream()
                    .anyMatch(r -> r.getReviewId().equals(reviewId));

            if (!owned) {
                ra.addFlashAttribute("errorMessage", "You can only edit your own reflections.");
                return "redirect:/profile";
            }

            reviewService.updateReview(reviewId, rating, comment);
            ra.addFlashAttribute("successMessage", "Reflection updated successfully.");

        } catch (Exception e) {
            log.error("Review edit failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Could not update reflection.");
        }

        return "redirect:/profile";
    }

    @PostMapping("/order/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, @RequestParam(required = false) String cancelReason, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null");
            User user = userService.findByEmail(principal.getName());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!order.getUser().getUserId().equals(user.getUserId())) {
                ra.addFlashAttribute("errorMessage", "Not authorized.");
                return "redirect:/profile#order-history";
            }

            if (order.getStatus() == com.trulybare.enums.OrderStatus.PENDING || order.getStatus() == com.trulybare.enums.OrderStatus.CONFIRMED) {
                order.setStatus(com.trulybare.enums.OrderStatus.CANCELLED);
                if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                    order.setCancelReason(cancelReason);
                }
                
                // Restore stock
                for (com.trulybare.entity.OrderItem item : order.getOrderItems()) {
                    com.trulybare.entity.Product product = item.getProduct();
                    if (product != null) {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
                
                orderRepository.save(order);
                ra.addFlashAttribute("successMessage", "Order cancelled successfully.");
            } else {
                ra.addFlashAttribute("errorMessage", "Order cannot be cancelled at this stage.");
            }
        } catch (Exception e) {
            log.error("Order cancel failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Could not cancel order: " + e.getMessage());
        }

        return "redirect:/profile#order-history";
    }

    @PostMapping("/order/return/{orderId}")
    public String returnOrder(@PathVariable Long orderId, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null");
            User user = userService.findByEmail(principal.getName());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!order.getUser().getUserId().equals(user.getUserId())) {
                ra.addFlashAttribute("errorMessage", "Not authorized.");
                return "redirect:/profile#order-history";
            }

            if (order.getStatus() == com.trulybare.enums.OrderStatus.DELIVERED) {
                // Redirect to the proper return request form (ReturnService workflow)
                return "redirect:/return/request/" + orderId;
            } else {
                ra.addFlashAttribute("errorMessage", "Only delivered orders can be returned.");
            }
        } catch (Exception e) {
            log.error("Order return redirect failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Could not initiate return: " + e.getMessage());
        }

        return "redirect:/profile#order-history";
    }

    @PostMapping("/order/reorder/{orderId}")
    public String reorderOrder(@PathVariable Long orderId, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null");
            User user = userService.findByEmail(principal.getName());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!order.getUser().getUserId().equals(user.getUserId())) {
                ra.addFlashAttribute("errorMessage", "Not authorized.");
                return "redirect:/profile#order-history";
            }

            if (order.getStatus() == com.trulybare.enums.OrderStatus.CANCELLED && order.isReorderable()) {
                order.setStatus(com.trulybare.enums.OrderStatus.PENDING);
                
                // Re-deduct stock
                for (com.trulybare.entity.OrderItem item : order.getOrderItems()) {
                    com.trulybare.entity.Product product = item.getProduct();
                    if (product != null) {
                        if (product.getStockQuantity() < item.getQuantity()) {
                            ra.addFlashAttribute("errorMessage", "Insufficient stock for product: " + product.getName() + " to reorder.");
                            return "redirect:/profile#order-history";
                        }
                        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                        productRepository.save(product);
                    }
                }
                
                orderRepository.save(order);
                ra.addFlashAttribute("successMessage", "Order successfully reordered.");
            } else {
                ra.addFlashAttribute("errorMessage", "Only recently cancelled orders can be reordered.");
            }
        } catch (Exception e) {
            log.error("Order reorder failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Could not reorder: " + e.getMessage());
        }

        return "redirect:/profile#order-history";
    }
}