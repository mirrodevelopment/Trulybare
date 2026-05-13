package com.trulybare.controller;

import com.trulybare.entity.*;
import com.trulybare.enums.OrderStatus;
import com.trulybare.enums.ReturnStatus;
import com.trulybare.enums.ReviewStatus;
import com.trulybare.repository.UserRepository;
import com.trulybare.service.AdminService;
import com.trulybare.service.ReturnService;
import com.trulybare.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReturnService returnService;

    @Autowired
    private UserService userService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private String getAdminName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            try {
                User u = userService.findByEmail(auth.getName());
                return u != null ? u.getFullName() : auth.getName();
            } catch (Exception e) {
                return auth.getName();
            }
        }
        return "Admin";
    }

    private Long getAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            try {
                User u = userService.findByEmail(auth.getName());
                return u != null ? u.getUserId() : null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ip;
    }

    // ─────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        List<User> users = adminService.getAllUsers();
        model.addAttribute("recentUsers", users.size() > 5 ? users.subList(0, 5) : users);
        model.addAttribute("activeMenu", "dashboard");
        // Recent activity logs for dashboard widget
        model.addAttribute("recentActivity", returnService.getRecentActivityLogs(8));
        return "admin/dashboard";
    }

    // ─────────────────────────────────────────────
    // PRODUCTS
    // ─────────────────────────────────────────────

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String q,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String stock,
                           Model model) {

        List<Product> all = adminService.getAllProducts();

        // ── text search (name or subtitle) ──
        if (q != null && !q.isBlank()) {
            String lower = q.trim().toLowerCase();
            all = all.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lower))
                            || (p.getSubtitle() != null && p.getSubtitle().toLowerCase().contains(lower)))
                    .collect(java.util.stream.Collectors.toList());
        }

        // ── category filter ──
        if (categoryId != null) {
            all = all.stream()
                    .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getCategoryId()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // ── active / inactive filter ──
        if ("active".equalsIgnoreCase(status)) {
            all = all.stream().filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                    .collect(java.util.stream.Collectors.toList());
        } else if ("inactive".equalsIgnoreCase(status)) {
            all = all.stream().filter(p -> !Boolean.TRUE.equals(p.getIsActive()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // ── stock filter ──
        if ("low".equalsIgnoreCase(stock)) {
            all = all.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStockQuantity() < 10)
                    .collect(java.util.stream.Collectors.toList());
        } else if ("out".equalsIgnoreCase(stock)) {
            all = all.stream().filter(p -> Boolean.TRUE.equals(p.getForceSoldOut())
                            || (p.getStockQuantity() != null && p.getStockQuantity() <= 0))
                    .collect(java.util.stream.Collectors.toList());
        } else if ("ok".equalsIgnoreCase(stock)) {
            all = all.stream().filter(p -> !Boolean.TRUE.equals(p.getForceSoldOut())
                            && p.getStockQuantity() != null && p.getStockQuantity() >= 10)
                    .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("products",   all);
        model.addAttribute("categories", adminService.getAllCategories());
        
        // Find all products that are currently used in Ritual Cards
        java.util.Set<Long> ritualProductIds = adminService.getAllRitualCards().stream()
                .filter(rc -> rc.getProduct() != null)
                .map(rc -> rc.getProduct().getProductId())
                .collect(java.util.stream.Collectors.toSet());
        model.addAttribute("ritualProductIds", ritualProductIds);

        model.addAttribute("activeMenu", "products");
        // preserve filter state for the UI
        model.addAttribute("filterQ",          q != null ? q : "");
        model.addAttribute("filterCategoryId", categoryId);
        model.addAttribute("filterStatus",     status != null ? status : "");
        model.addAttribute("filterStock",      stock  != null ? stock  : "");
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product",    new Product());
        model.addAttribute("categories", adminService.getAllCategories());
        model.addAttribute("activeMenu", "products");
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return adminService.getProductById(id).map(p -> {
            model.addAttribute("product",    p);
            model.addAttribute("categories", adminService.getAllCategories());
            model.addAttribute("activeMenu", "products");
            return "admin/product-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Product not found.");
            return "redirect:/admin/products";
        });
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(value = "mainImageFile", required = false) MultipartFile mainFile,
                              @RequestParam(value = "sub1File",      required = false) MultipartFile sub1File,
                              @RequestParam(value = "sub2File",      required = false) MultipartFile sub2File,
                              @RequestParam(value = "sub3File",      required = false) MultipartFile sub3File,
                              @RequestParam(value = "sub4File",      required = false) MultipartFile sub4File,
                              @RequestParam(value = "skinTypes",     required = false) java.util.List<String> skinTypes,
                              @RequestParam(value = "hairTypes",     required = false) java.util.List<String> hairTypes,
                              HttpServletRequest request,
                              RedirectAttributes ra) {

        // ── Load old URLs so we can (a) fall back when no new file and (b) delete from GCS when replaced ──
        Product old = (product.getProductId() != null)
                ? adminService.getProductById(product.getProductId()).orElse(null)
                : null;

        // ── Upload new images to GCS; delete old GCS object if being replaced ──
        String pName = product.getName(); // used for filename
        try {
            if (mainFile != null && !mainFile.isEmpty()) {
                if (old != null) adminService.deleteOldImage(old.getImageUrl());
                product.setImageUrl(adminService.saveProductImage(mainFile, pName));
            } else if (old != null) {
                product.setImageUrl(old.getImageUrl());
            }

            if (sub1File != null && !sub1File.isEmpty()) {
                if (old != null) adminService.deleteOldImage(old.getSubImageUrl1());
                product.setSubImageUrl1(adminService.saveProductImage(sub1File, pName));
            } else if (old != null) {
                product.setSubImageUrl1(old.getSubImageUrl1());
            }

            if (sub2File != null && !sub2File.isEmpty()) {
                if (old != null) adminService.deleteOldImage(old.getSubImageUrl2());
                product.setSubImageUrl2(adminService.saveProductImage(sub2File, pName));
            } else if (old != null) {
                product.setSubImageUrl2(old.getSubImageUrl2());
            }

            if (sub3File != null && !sub3File.isEmpty()) {
                if (old != null) adminService.deleteOldImage(old.getSubImageUrl3());
                product.setSubImageUrl3(adminService.saveProductImage(sub3File, pName));
            } else if (old != null) {
                product.setSubImageUrl3(old.getSubImageUrl3());
            }

            if (sub4File != null && !sub4File.isEmpty()) {
                if (old != null) adminService.deleteOldImage(old.getSubImageUrl4());
                product.setSubImageUrl4(adminService.saveProductImage(sub4File, pName));
            } else if (old != null) {
                product.setSubImageUrl4(old.getSubImageUrl4());
            }
        } catch (IllegalArgumentException e) {
            // Validation failure (wrong type / too large)
            ra.addFlashAttribute("error", "Image upload failed: " + e.getMessage());
            return (product.getProductId() != null)
                    ? "redirect:/admin/products/edit/" + product.getProductId()
                    : "redirect:/admin/products/new";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not upload image to Cloud Storage: " + e.getMessage());
            return "redirect:/admin/products";
        }

        // ── Duplicate name guard ──
        if (adminService.productNameExists(product.getName(), product.getProductId())) {
            ra.addFlashAttribute("error", "A product with this name already exists.");
            return "redirect:/admin/products";
        }

        // ── Category ──
        if (categoryId != null) {
            adminService.getAllCategories().stream()
                    .filter(c -> c.getCategoryId().equals(categoryId))
                    .findFirst().ifPresent(product::setCategory);
        }

        // ── Boolean defaults ──
        if (product.getIsActive()     == null) product.setIsActive(false);
        if (product.getIsFeatured()   == null) product.setIsFeatured(false);
        if (product.getIsBestSeller() == null) product.setIsBestSeller(false);
        if (product.getIsNew()        == null) product.setIsNew(false);
        if (product.getIsClassic()    == null) product.setIsClassic(false);
        if (product.getStockQuantity()== null) product.setStockQuantity(0);
        if (product.getForceSoldOut() == null) product.setForceSoldOut(false);

        // ── Skin / hair types ──
        product.setSkinType((skinTypes != null && !skinTypes.isEmpty()) ? String.join(", ", skinTypes) : null);
        product.setHairType((hairTypes != null && !hairTypes.isEmpty()) ? String.join(", ", hairTypes) : null);

        adminService.saveProduct(product);
        adminService.logActivity(getAdminId(), getAdminName(), "PRODUCT_SAVE", "PRODUCT", product.getProductId(),
                                 null, product.getName(), "Saved/Updated product: " + product.getName(), getIp(request));
        ra.addFlashAttribute("success", "Product saved successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        // Delete associated GCS images before removing the DB record
        adminService.getProductById(id).ifPresent(p -> {
            adminService.deleteOldImage(p.getImageUrl());
            adminService.deleteOldImage(p.getSubImageUrl1());
            adminService.deleteOldImage(p.getSubImageUrl2());
            adminService.deleteOldImage(p.getSubImageUrl3());
            adminService.deleteOldImage(p.getSubImageUrl4());
        });
        adminService.deleteProduct(id);
        adminService.logActivity(getAdminId(), getAdminName(), "PRODUCT_DELETE", "PRODUCT", id,
                                 null, null, "Deleted product ID: " + id, getIp(request));
        ra.addFlashAttribute("success", "Product deleted.");
        return "redirect:/admin/products";
    }

    // ─────────────────────────────────────────────
    // CATEGORY MANAGEMENT
    // ─────────────────────────────────────────────

    @GetMapping("/categories")
    @Transactional(readOnly = true)
    public String categories(Model model) {
        model.addAttribute("categories", adminService.getAllCategories());
        model.addAttribute("activeMenu", "categories");
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) Long categoryId,
                               @RequestParam String name,
                               @RequestParam String description,
                               @RequestParam(required = false) String badgeText,
                               @RequestParam(required = false, defaultValue = "0") Integer displayOrder,
                               RedirectAttributes ra,
                               HttpServletRequest request) {
        try {
            Category category;
            String oldDesc = null;
            if (categoryId != null) {
                Optional<Category> opt = adminService.getCategoryById(categoryId);
                if (opt.isEmpty()) {
                    ra.addFlashAttribute("error", "Category not found.");
                    return "redirect:/admin/categories";
                }
                category = opt.get();
                oldDesc = category.getDescription();
            } else {
                category = new Category();
                category.setIsActive(true);
            }
            
            category.setName(name);
            category.setDescription(description);
            category.setBadgeText(badgeText);
            category.setDisplayOrder(displayOrder);
            
            category = adminService.saveCategory(category);
            
            String action = categoryId != null ? "UPDATE" : "CREATE";
            String safeOldDesc = oldDesc != null && oldDesc.length() > 95 ? oldDesc.substring(0, 95) + "..." : oldDesc;
            String safeNewDesc = description != null && description.length() > 95 ? description.substring(0, 95) + "..." : description;

            adminService.logActivity(getAdminId(), getAdminName(), action, "CATEGORY", 
                category.getCategoryId(), safeOldDesc, safeNewDesc, 
                (categoryId != null ? "Updated" : "Created") + " category: " + name, getIp(request));
                
            ra.addFlashAttribute("success", "Category '" + name + "' " + (categoryId != null ? "updated" : "created") + " successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        try {
            adminService.getCategoryById(id).ifPresent(cat -> {
                if (cat.getProducts() != null && !cat.getProducts().isEmpty()) {
                    throw new IllegalStateException("Cannot delete '" + cat.getName() + "': it still has " + cat.getProducts().size() + " product(s) assigned.");
                }
                adminService.deleteCategory(id);
                adminService.logActivity(getAdminId(), getAdminName(), "CATEGORY_DELETE", "CATEGORY", id,
                        cat.getName(), null, "Deleted category: " + cat.getName(), getIp(request));
            });
            ra.addFlashAttribute("success", "Category deleted successfully.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not delete category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/toggle/{id}")
    public String toggleCategoryStatus(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        try {
            boolean nowActive = adminService.toggleCategoryStatus(id);
            String label = nowActive ? "activated" : "deactivated";
            adminService.getCategoryById(id).ifPresent(cat ->
                adminService.logActivity(getAdminId(), getAdminName(), "CATEGORY_TOGGLE", "CATEGORY", id,
                        null, Boolean.toString(nowActive), "Category " + label + ": " + cat.getName(), getIp(request))
            );
            ra.addFlashAttribute("success", "Category " + label + " successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not toggle category status: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }


    // INVENTORY MANAGEMENT
    // ─────────────────────────────────────────────

    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("products",   adminService.getAllInventory());
        model.addAttribute("activeMenu", "inventory");
        return "admin/inventory";
    }

    @PostMapping("/inventory/update")
    public String updateStock(@RequestParam Long productId,
                              @RequestParam int quantity,
                              HttpServletRequest request,
                              RedirectAttributes ra) {
        try {
            adminService.updateStock(productId, quantity);
            adminService.logActivity(getAdminId(), getAdminName(), "STOCK_UPDATE", "PRODUCT", productId, 
                                     null, String.valueOf(quantity), "Updated stock to: " + quantity, getIp(request));
            ra.addFlashAttribute("success", "Stock updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    // ─────────────────────────────────────────────
    // ORDERS
    // ─────────────────────────────────────────────

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders;
        if (status != null && !status.isBlank()) {
            try {
                orders = adminService.getOrdersByStatus(OrderStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                orders = adminService.getAllOrders();
            }
        } else {
            orders = adminService.getAllOrders();
        }
        model.addAttribute("orders",      orders);
        model.addAttribute("statuses",    OrderStatus.values());
        model.addAttribute("activeStatus", status);
        model.addAttribute("activeMenu",  "orders");
        return "admin/orders";
    }

    @PostMapping("/orders/status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam String status,
                                    HttpServletRequest request,
                                    RedirectAttributes ra) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            adminService.updateOrderStatus(orderId, newStatus);
            adminService.logActivity(getAdminId(), getAdminName(), "ORDER_STATUS_UPDATE", "ORDER", orderId, 
                                     null, newStatus.name(), "Updated order status to: " + newStatus.name(), getIp(request));
            ra.addFlashAttribute("success", "Order status updated!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Invalid order status: " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ─────────────────────────────────────────────
    // REVIEWS
    // ─────────────────────────────────────────────

    @GetMapping("/reviews")
    public String reviews(@RequestParam(required = false) String status, Model model) {
        List<Review> reviews;
        if (status != null && !status.isBlank()) {
            try {
                reviews = adminService.getReviewsByStatus(ReviewStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                reviews = adminService.getAllReviews();
            }
        } else {
            reviews = adminService.getAllReviews();
        }
        model.addAttribute("reviews",      reviews);
        model.addAttribute("activeStatus", status);
        model.addAttribute("pendingCount", adminService.getReviewsByStatus(ReviewStatus.PENDING).size());
        model.addAttribute("activeMenu",   "reviews");
        return "admin/reviews";
    }

    @PostMapping("/reviews/approve/{id}")
    public String approveReview(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        adminService.approveReview(id);
        adminService.logActivity(getAdminId(), getAdminName(), "REVIEW_APPROVE", "REVIEW", id, 
                                 null, "APPROVED", "Approved review ID: " + id, getIp(request));
        ra.addFlashAttribute("success", "Review approved and published!");
        return "redirect:/admin/reviews?status=PENDING";
    }

    @PostMapping("/reviews/reject/{id}")
    public String rejectReview(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        adminService.rejectReview(id);
        adminService.logActivity(getAdminId(), getAdminName(), "REVIEW_REJECT", "REVIEW", id, 
                                 null, "REJECTED", "Rejected review ID: " + id, getIp(request));
        ra.addFlashAttribute("success", "Review rejected.");
        return "redirect:/admin/reviews?status=PENDING";
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        adminService.deleteReview(id);
        adminService.logActivity(getAdminId(), getAdminName(), "REVIEW_DELETE", "REVIEW", id, 
                                 null, null, "Deleted review ID: " + id, getIp(request));
        ra.addFlashAttribute("success", "Review deleted.");
        return "redirect:/admin/reviews";
    }

    // ─────────────────────────────────────────────
    // RETURNS & REFUNDS — DASHBOARD
    // ─────────────────────────────────────────────

    @GetMapping("/returns")
    public String returns(@RequestParam(required = false) String status, Model model) {
        List<ReturnRequest> returns;
        if (status != null && !status.isBlank()) {
            try {
                returns = returnService.getReturnsByStatus(ReturnStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                returns = returnService.getAllReturns();
            }
        } else {
            returns = returnService.getAllReturns();
        }
        model.addAttribute("returns",      returns);
        model.addAttribute("statuses",     ReturnStatus.values());
        model.addAttribute("activeStatus", status);
        model.addAttribute("statusCounts", returnService.getReturnStatusCounts());
        model.addAttribute("activeMenu",   "returns");
        return "admin/returns";
    }

    // ─────────────────────────────────────────────
    // RETURNS — DETAIL + CHAT VIEW
    // ─────────────────────────────────────────────

    @GetMapping("/returns/{id}")
    public String returnDetail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return returnService.getById(id).map(rr -> {
            model.addAttribute("rr", rr);
            model.addAttribute("statuses", ReturnStatus.values());
            model.addAttribute("activityLogs", returnService.getActivityLogsForReturn(id));
            model.addAttribute("activeMenu", "returns");
            return "admin/return-detail";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Return request not found.");
            return "redirect:/admin/returns";
        });
    }

    // ─────────────────────────────────────────────
    // RETURNS — APPROVE REFUND (Razorpay)
    // ─────────────────────────────────────────────

    @PostMapping("/returns/{id}/approve-refund")
    public String approveRefund(@PathVariable Long id,
                                HttpServletRequest request,
                                RedirectAttributes ra) {
        try {
            returnService.approveRefund(id, getAdminId(), getAdminName(), getIp(request));
            ra.addFlashAttribute("success", "Refund initiated successfully via Razorpay!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Refund failed: " + e.getMessage());
        }
        return "redirect:/admin/returns/" + id;
    }

    // ─────────────────────────────────────────────
    // RETURNS — APPROVE REPLACEMENT
    // ─────────────────────────────────────────────

    @PostMapping("/returns/{id}/approve-replacement")
    public String approveReplacement(@PathVariable("id") Long id, HttpServletRequest request, RedirectAttributes ra) {
        try {
            returnService.approveReplacement(id, getAdminId(), getAdminName(), getIp(request));
            ra.addFlashAttribute("success", "Replacement approved and processing started!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/returns/" + id;
    }

    // ─────────────────────────────────────────────
    // RETURNS — REJECT
    // ─────────────────────────────────────────────

    @PostMapping("/returns/{id}/reject")
    public String rejectReturn(@PathVariable("id") Long id, @RequestParam(required = false) String rejectionReason, HttpServletRequest request, RedirectAttributes ra) {
        try {
            returnService.rejectReturn(id, rejectionReason, getAdminId(), getAdminName(), getIp(request));
            ra.addFlashAttribute("success", "Return request rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/returns/" + id;
    }

    // ─────────────────────────────────────────────
    // RETURNS — UPDATE STATUS
    // ─────────────────────────────────────────────

    @PostMapping("/returns/{id}/status")
    public String updateReturnStatus(@PathVariable("id") Long id, @RequestParam("status") String status, @RequestParam(required = false) String adminRemarks, HttpServletRequest request, RedirectAttributes ra) {
        try {
            ReturnStatus newStatus = ReturnStatus.valueOf(status.trim().toUpperCase());
            returnService.updateStatus(id, newStatus, adminRemarks, getAdminId(), getAdminName(), getIp(request));
            ra.addFlashAttribute("success", "Status updated to " + newStatus.name());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Invalid status: " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/returns/" + id;
    }

    // ─────────────────────────────────────────────
    // RETURNS — ADMIN CHAT MESSAGE
    // ─────────────────────────────────────────────

    @PostMapping("/returns/{id}/message")
    public String sendAdminMessage(@PathVariable Long id,
                                   @RequestParam(required = false) String message,
                                   @RequestParam(required = false) MultipartFile attachment,
                                   HttpServletRequest request,
                                   RedirectAttributes ra) {
        try {
            if ((message == null || message.isBlank()) && (attachment == null || attachment.isEmpty())) {
                ra.addFlashAttribute("error", "Please enter a message or attach a file.");
                return "redirect:/admin/returns/" + id;
            }
            returnService.sendAdminMessage(id, getAdminName(), message, attachment, uploadDir, getAdminId(), getIp(request));
            ra.addFlashAttribute("success", "Message sent.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not send message: " + e.getMessage());
        }
        return "redirect:/admin/returns/" + id;
    }

    // ─────────────────────────────────────────────
    // LEGACY RETURNS (old Return entity) — kept for backward compat
    // ─────────────────────────────────────────────

    @PostMapping("/returns/update")
    public String updateReturn(@RequestParam Long returnId,
                               @RequestParam String status,
                               @RequestParam(required = false) String adminRemarks,
                               @RequestParam(required = false) BigDecimal refundAmount,
                               RedirectAttributes ra) {
        try {
            adminService.updateReturnStatus(returnId,
                    com.trulybare.enums.ReturnStatus.valueOf(status.trim().toUpperCase()),
                    adminRemarks, refundAmount);
            ra.addFlashAttribute("success", "Return status updated!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Invalid return status: " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating return: " + e.getMessage());
        }
        return "redirect:/admin/returns";
    }

    // ─────────────────────────────────────────────
    // ACTIVITY LOG
    // ─────────────────────────────────────────────

    @GetMapping("/activity-log")
    public String activityLog(@RequestParam(required = false) String date, Model model) {
        LocalDate queryDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
        List<AdminActivityLog> logs = returnService.getAllActivityLogs(queryDate);
        
        model.addAttribute("logs", logs != null ? logs : java.util.Collections.emptyList());
        model.addAttribute("queryDate", queryDate != null ? queryDate.toString() : "");
        model.addAttribute("activeMenu", "activity-log");
        return "admin/activity-log";
    }

    // ─────────────────────────────────────────────
    // SLIDES / BANNERS
    // ─────────────────────────────────────────────

    @GetMapping("/slides")
    public String slides(Model model) {
        model.addAttribute("slides",     adminService.getAllSlides());
        model.addAttribute("activeMenu", "slides");
        return "admin/slides";
    }

    // ─────────────────────────────────────────────
    // VLOGS
    // ─────────────────────────────────────────────

    @GetMapping("/vlogs")
    public String vlogs(Model model) {
        List<Vlog> vlogs = adminService.getAllVlogs();
        
        // Auto-seed if empty
        if (vlogs.isEmpty()) {
            Vlog v1 = new Vlog(null, "Inside Our Delhi Flagship", "Store Tour", 
                "Step inside the calm, botanical world of our Connaught Place store — curated for the senses.",
                "/assets/videos/vlog1.mp4", "/assets/images/vlogs/vlog1.jpg", "Delhi", "April 2024", 
                0, true, java.time.LocalDateTime.now());
            
            Vlog v2 = new Vlog(null, "Skincare Ritual with Our Experts", "Expert Talk",
                "Our Mumbai team walks through the perfect morning ritual using Truly Bare botanicals.",
                "/assets/videos/vlog2.mp4", "/assets/images/vlogs/vlog2.jpg", "Mumbai", "March 2024",
                1, true, java.time.LocalDateTime.now());
                
            Vlog v3 = new Vlog(null, "Formulation Lab — Bangalore", "Behind the Scenes",
                "An exclusive look at how our botanical formulations are created with care and precision.",
                "/assets/videos/vlog3.mp4", "/assets/images/vlogs/vlog3.jpg", "Bangalore", "February 2024",
                2, true, java.time.LocalDateTime.now());
            
            adminService.saveVlog(v1);
            adminService.saveVlog(v2);
            adminService.saveVlog(v3);
            vlogs = adminService.getAllVlogs();
        }
        
        model.addAttribute("vlogs", vlogs);
        model.addAttribute("activeMenu", "vlogs");
        return "admin/vlogs";
    }

    @GetMapping("/vlogs/new")
    public String newVlogForm(Model model) {
        model.addAttribute("vlog", new Vlog());
        model.addAttribute("activeMenu", "vlogs");
        model.addAttribute("pageTitle", "Add New Vlog");
        return "admin/vlog-form";
    }

    @GetMapping("/vlogs/edit/{id}")
    public String editVlogForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return adminService.getVlogById(id).map(v -> {
            model.addAttribute("vlog", v);
            model.addAttribute("activeMenu", "vlogs");
            model.addAttribute("pageTitle", "Edit Vlog");
            return "admin/vlog-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Vlog not found.");
            return "redirect:/admin/vlogs";
        });
    }

    @PostMapping("/vlogs/save")
    public String saveVlog(@ModelAttribute Vlog vlog,
                           @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                           @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
                           HttpServletRequest request,
                           RedirectAttributes ra) {
        
        Vlog old = (vlog.getVlogId() != null) 
            ? adminService.getVlogById(vlog.getVlogId()).orElse(null) 
            : null;

        try {
            if (videoFile != null && !videoFile.isEmpty()) {
                if (old != null && old.getVideoUrl() != null && !old.getVideoUrl().contains("youtube.com")) {
                    adminService.deleteOldImage(old.getVideoUrl());
                }
                vlog.setVideoUrl(adminService.saveVlogFile(videoFile, vlog.getTitle(), "videos"));
            } else if (vlog.getVideoUrl() != null && !vlog.getVideoUrl().trim().isEmpty()) {
                vlog.setVideoUrl(formatYouTubeUrl(vlog.getVideoUrl()));
            } else if (old != null) {
                vlog.setVideoUrl(old.getVideoUrl());
            }

            if (posterFile != null && !posterFile.isEmpty()) {
                if (old != null) adminService.deleteOldImage(old.getPosterUrl());
                vlog.setPosterUrl(adminService.saveVlogFile(posterFile, vlog.getTitle(), "posters"));
            } else if (old != null) {
                vlog.setPosterUrl(old.getPosterUrl());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Upload failed: " + e.getMessage());
            return "redirect:/admin/vlogs";
        }

        if (vlog.getDisplayOrder() == null) vlog.setDisplayOrder(0);
        
        adminService.saveVlog(vlog);
        adminService.logActivity(getAdminId(), getAdminName(), "VLOG_SAVE", "VLOG", vlog.getVlogId(),
            null, vlog.getTitle(), "Saved/Updated vlog: " + vlog.getTitle(), getIp(request));
            
        ra.addFlashAttribute("success", "Vlog saved successfully!");
        return "redirect:/admin/vlogs";
    }

    @PostMapping("/vlogs/delete/{id}")
    public String deleteVlog(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        adminService.getVlogById(id).ifPresent(v -> {
            adminService.deleteOldImage(v.getVideoUrl());
            adminService.deleteOldImage(v.getPosterUrl());
            adminService.deleteVlog(id);
            adminService.logActivity(getAdminId(), getAdminName(), "VLOG_DELETE", "VLOG", id,
                v.getTitle(), null, "Deleted vlog: " + v.getTitle(), getIp(request));
        });
        ra.addFlashAttribute("success", "Vlog deleted.");
        return "redirect:/admin/vlogs";
    }

    // ─────────────────────────────────────────────
    // HOMEPAGE CONTENT MANAGEMENT
    // ─────────────────────────────────────────────

    @GetMapping("/content/homepage")
    public String homepageContent(Model model) {
        model.addAttribute("ritualEyebrow", adminService.getSetting("ritual_eyebrow", "The Ritual Edit"));
        model.addAttribute("ritualTitle",   adminService.getSetting("ritual_title", "Crafted for the senses"));
        model.addAttribute("ritualDesc",    adminService.getSetting("ritual_desc", "Our masterclass formulations will help you create a unique skincare ritual. Our team has done everything to ensure you can express your inner world through your daily routine."));
        model.addAttribute("announcementText", adminService.getSetting("announcement_text", "Complimentary shipping on all orders over ₹2,500  ·  New: The Reverence Collection"));
        model.addAttribute("activeMenu",    "homepage-content");
        return "admin/homepage-content";
    }

    @PostMapping("/content/homepage/save")
    public String saveHomepageContent(@RequestParam String ritualEyebrow,
                                      @RequestParam String ritualTitle,
                                      @RequestParam String ritualDesc,
                                      @RequestParam(required = false) String announcementText,
                                      RedirectAttributes ra,
                                      HttpServletRequest request) {
        try {
            adminService.saveSetting("ritual_eyebrow", ritualEyebrow);
            adminService.saveSetting("ritual_title",   ritualTitle);
            adminService.saveSetting("ritual_desc",    ritualDesc);
            
            if (announcementText != null) {
                adminService.saveSetting("announcement_text", announcementText);
            }
            
            adminService.logActivity(getAdminId(), getAdminName(), "UPDATE", "HOMEPAGE_CONTENT", null,
                null, null, "Updated homepage content and announcement bar", getIp(request));
                
            ra.addFlashAttribute("success", "Homepage content updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating content: " + e.getMessage());
        }
        return "redirect:/admin/content/homepage";
    }
    // ─── CONTENT MANAGER (tabbed) ───

    @GetMapping("/content/manager")
    public String contentManager(Model model) {
        model.addAttribute("announcementText", adminService.getSetting("announcement_text","Complimentary shipping on all orders over Rs.2500"));
        model.addAttribute("heroTagline",      adminService.getSetting("hero_tagline","Pure. Natural. Radiant."));
        model.addAttribute("heroSubtext",      adminService.getSetting("hero_subtext",""));
        model.addAttribute("ritualEyebrow",    adminService.getSetting("ritual_eyebrow","The Ritual Edit"));
        model.addAttribute("ritualTitle",      adminService.getSetting("ritual_title","Crafted for the senses"));
        model.addAttribute("ritualDesc",       adminService.getSetting("ritual_desc",""));
        model.addAttribute("philosophyTitle",  adminService.getSetting("philosophy_title","Our Philosophy"));
        model.addAttribute("philosophyText",   adminService.getSetting("philosophy_text",""));
        model.addAttribute("communityEyebrow", adminService.getSetting("community_eyebrow","Real Stories"));
        model.addAttribute("communityTitle",   adminService.getSetting("community_title","What Our Community Feels"));
        model.addAttribute("storyTitle",       adminService.getSetting("story_title","Born from Nature"));
        model.addAttribute("storyText",        adminService.getSetting("story_text",""));
        model.addAttribute("chapterEyebrow",   adminService.getSetting("chapter_eyebrow","The Chapter Edit"));
        model.addAttribute("chapterTitle",     adminService.getSetting("chapter_title","Begin Your Ritual"));
        model.addAttribute("chapterDesc",      adminService.getSetting("chapter_desc",""));
        model.addAttribute("shopFilterTitle",  adminService.getSetting("shop_filter_title","Shop by Category"));
        model.addAttribute("shopPageTitle",    adminService.getSetting("shop_page_title","Our Collection"));
        model.addAttribute("shopPageSubtitle", adminService.getSetting("shop_page_subtitle",""));
        model.addAttribute("contactEmail",     adminService.getSetting("contact_email",""));
        model.addAttribute("contactPhone",     adminService.getSetting("contact_phone",""));
        model.addAttribute("contactAddress",   adminService.getSetting("contact_address",""));
        model.addAttribute("businessHours",    adminService.getSetting("business_hours","Mon-Sat 10am-6pm IST"));
        model.addAttribute("freeShippingThreshold", adminService.getSetting("free_shipping_threshold","2500"));
        model.addAttribute("shippingMessage",  adminService.getSetting("shipping_message",""));
        model.addAttribute("returnWindowDays", adminService.getSetting("return_window_days","7"));
        model.addAttribute("returnsMessage",   adminService.getSetting("returns_message",""));
        model.addAttribute("profileWelcome",   adminService.getSetting("profile_welcome","Welcome back,"));
        model.addAttribute("loyaltyTagline",   adminService.getSetting("loyalty_tagline","Earn points with every purchase"));
        model.addAttribute("footerTagline",    adminService.getSetting("footer_tagline","Rooted in nature, crafted with care."));
        model.addAttribute("footerCopyright",  adminService.getSetting("footer_copyright","2025 Truly Bare. All rights reserved."));
        model.addAttribute("socialInstagram",  adminService.getSetting("social_instagram",""));
        model.addAttribute("socialFacebook",   adminService.getSetting("social_facebook",""));
        model.addAttribute("socialYoutube",    adminService.getSetting("social_youtube",""));
        model.addAttribute("socialTwitter",    adminService.getSetting("social_twitter",""));
        model.addAttribute("ritualCards",      adminService.getAllRitualCards());
        model.addAttribute("activeMenu","homepage-content");
        return "admin/homepage-content";
    }

    @PostMapping("/content/manager/home/save")
    public String saveHomeContent(@RequestParam(required=false) String announcementText, @RequestParam(required=false) String heroTagline, @RequestParam(required=false) String heroSubtext, @RequestParam(required=false) String ritualEyebrow, @RequestParam(required=false) String ritualTitle, @RequestParam(required=false) String ritualDesc, @RequestParam(required=false) String philosophyTitle, @RequestParam(required=false) String philosophyText, @RequestParam(required=false) String communityEyebrow, @RequestParam(required=false) String communityTitle, @RequestParam(required=false) String storyTitle, @RequestParam(required=false) String storyText, @RequestParam(required=false) String chapterEyebrow, @RequestParam(required=false) String chapterTitle, @RequestParam(required=false) String chapterDesc, RedirectAttributes ra) {
        adminService.saveSetting("announcement_text",announcementText); adminService.saveSetting("hero_tagline",heroTagline); adminService.saveSetting("hero_subtext",heroSubtext);
        adminService.saveSetting("ritual_eyebrow",ritualEyebrow); adminService.saveSetting("ritual_title",ritualTitle); adminService.saveSetting("ritual_desc",ritualDesc);
        adminService.saveSetting("philosophy_title",philosophyTitle); adminService.saveSetting("philosophy_text",philosophyText);
        adminService.saveSetting("community_eyebrow",communityEyebrow); adminService.saveSetting("community_title",communityTitle);
        adminService.saveSetting("story_title",storyTitle); adminService.saveSetting("story_text",storyText);
        adminService.saveSetting("chapter_eyebrow",chapterEyebrow); adminService.saveSetting("chapter_title",chapterTitle); adminService.saveSetting("chapter_desc",chapterDesc);
        ra.addFlashAttribute("success","Home content saved!"); return "redirect:/admin/content/manager";
    }

    @PostMapping("/content/manager/shop/save")
    public String saveShopContent(@RequestParam(required=false) String shopFilterTitle, @RequestParam(required=false) String shopPageTitle, @RequestParam(required=false) String shopPageSubtitle, RedirectAttributes ra) {
        adminService.saveSetting("shop_filter_title",shopFilterTitle); adminService.saveSetting("shop_page_title",shopPageTitle); adminService.saveSetting("shop_page_subtitle",shopPageSubtitle);
        ra.addFlashAttribute("success","Shop content saved!"); return "redirect:/admin/content/manager";
    }

    @PostMapping("/content/manager/contact/save")
    public String saveContactContent(@RequestParam(required=false) String contactEmail, @RequestParam(required=false) String contactPhone, @RequestParam(required=false) String contactAddress, @RequestParam(required=false) String businessHours, RedirectAttributes ra) {
        adminService.saveSetting("contact_email",contactEmail); adminService.saveSetting("contact_phone",contactPhone); adminService.saveSetting("contact_address",contactAddress); adminService.saveSetting("business_hours",businessHours);
        ra.addFlashAttribute("success","Contact content saved!"); return "redirect:/admin/content/manager";
    }

    @PostMapping("/content/manager/orders/save")
    public String saveOrdersContent(@RequestParam(required=false) String freeShippingThreshold, @RequestParam(required=false) String shippingMessage, @RequestParam(required=false) String returnWindowDays, @RequestParam(required=false) String returnsMessage, RedirectAttributes ra) {
        adminService.saveSetting("free_shipping_threshold",freeShippingThreshold); adminService.saveSetting("shipping_message",shippingMessage); adminService.saveSetting("return_window_days",returnWindowDays); adminService.saveSetting("returns_message",returnsMessage);
        ra.addFlashAttribute("success","Orders content saved!"); return "redirect:/admin/content/manager";
    }

    @PostMapping("/content/manager/profile/save")
    public String saveProfileContent(@RequestParam(required=false) String profileWelcome, @RequestParam(required=false) String loyaltyTagline, RedirectAttributes ra) {
        adminService.saveSetting("profile_welcome",profileWelcome); adminService.saveSetting("loyalty_tagline",loyaltyTagline);
        ra.addFlashAttribute("success","Profile content saved!"); return "redirect:/admin/content/manager";
    }

    @PostMapping("/content/manager/footer/save")
    public String saveFooterContent(@RequestParam(required=false) String footerTagline, @RequestParam(required=false) String footerCopyright, @RequestParam(required=false) String socialInstagram, @RequestParam(required=false) String socialFacebook, @RequestParam(required=false) String socialYoutube, @RequestParam(required=false) String socialTwitter, RedirectAttributes ra) {
        adminService.saveSetting("footer_tagline",footerTagline); adminService.saveSetting("footer_copyright",footerCopyright);
        adminService.saveSetting("social_instagram",socialInstagram); adminService.saveSetting("social_facebook",socialFacebook); adminService.saveSetting("social_youtube",socialYoutube); adminService.saveSetting("social_twitter",socialTwitter);
        ra.addFlashAttribute("success","Footer content saved!"); return "redirect:/admin/content/manager";
    }


    // ─────────────────────────────────────────────
    // RITUAL EDIT CARDS (ANTI-GRAVITY CAROUSEL)
    // ─────────────────────────────────────────────

    @GetMapping("/ritual-cards")
    public String ritualCards(Model model) {
        model.addAttribute("cards",      adminService.getAllRitualCards());
        model.addAttribute("activeMenu", "ritual-cards");
        return "admin/ritual-cards";
    }

    @GetMapping("/ritual-cards/new")
    public String newRitualCardForm(Model model) {
        model.addAttribute("card",       new RitualCard());
        model.addAttribute("products",   adminService.getAllProducts());
        model.addAttribute("categories", adminService.getAllCategories());
        model.addAttribute("activeMenu", "ritual-cards");
        return "admin/ritual-card-form";
    }

    @GetMapping("/ritual-cards/edit/{id}")
    public String editRitualCardForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return adminService.getRitualCardById(id).map(c -> {
            model.addAttribute("card",       c);
            model.addAttribute("products",   adminService.getAllProducts());
            model.addAttribute("categories", adminService.getAllCategories());
            model.addAttribute("activeMenu", "ritual-cards");
            return "admin/ritual-card-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Card not found.");
            return "redirect:/admin/ritual-cards";
        });
    }

    @PostMapping("/ritual-cards/save")
    public String saveRitualCard(@ModelAttribute RitualCard card,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 HttpServletRequest request, RedirectAttributes ra) {
        try {
            if (card.getIsActive() == null) card.setIsActive(true);
            if (card.getDisplayOrder() == null) card.setDisplayOrder(0);

            // Delete old GCS image if a new one is being uploaded
            if (imageFile != null && !imageFile.isEmpty()) {
                adminService.getRitualCardById(card.getCardId()).ifPresent(existing ->
                        adminService.deleteOldImage(existing.getImageUrl()));
                card.setImageUrl(adminService.saveRitualCardImage(imageFile, card.getTitle()));
            }

            adminService.saveRitualCard(card);
            adminService.logActivity(getAdminId(), getAdminName(), "SAVE", "RITUAL_CARD", card.getCardId(),
                null, card.getTitle(), "Saved ritual card: " + card.getTitle(), getIp(request));
            ra.addFlashAttribute("success", "Card saved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving card: " + e.getMessage());
        }
        return "redirect:/admin/ritual-cards";
    }

    @PostMapping("/ritual-cards/delete/{id}")
    public String deleteRitualCard(@PathVariable Long id, RedirectAttributes ra, HttpServletRequest request) {
        // Clean up GCS image before deleting the record
        adminService.getRitualCardById(id).ifPresent(c -> adminService.deleteOldImage(c.getImageUrl()));
        adminService.deleteRitualCard(id);
        adminService.logActivity(getAdminId(), getAdminName(), "DELETE", "RITUAL_CARD", id, 
            null, null, "Deleted ritual card ID: " + id, getIp(request));
        ra.addFlashAttribute("success", "Card deleted.");
        return "redirect:/admin/ritual-cards";
    }

    @GetMapping("/slides/new")
    public String newSlideForm(Model model) {
        model.addAttribute("slide",      new Slide());
        model.addAttribute("categories", adminService.getAllCategories());
        model.addAttribute("products",   adminService.getAllProducts());
        model.addAttribute("activeMenu", "slides");
        return "admin/slide-form";
    }

    @GetMapping("/slides/edit/{id}")
    public String editSlideForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return adminService.getSlideById(id).map(s -> {
            model.addAttribute("slide",      s);
            model.addAttribute("categories", adminService.getAllCategories());
            model.addAttribute("products",   adminService.getAllProducts());
            model.addAttribute("activeMenu", "slides");
            return "admin/slide-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Slide not found.");
            return "redirect:/admin/slides";
        });
    }

    @PostMapping("/slides/save")
    public String saveSlide(@ModelAttribute Slide slide, HttpServletRequest request, RedirectAttributes ra) {
        if (slide.getIsActive() == null) slide.setIsActive(true);
        if (slide.getDisplayOrder() == null) slide.setDisplayOrder(0);
        adminService.saveSlide(slide);
        adminService.logActivity(getAdminId(), getAdminName(), "SLIDE_SAVE", "SLIDE", slide.getSlideId(), 
                                 null, slide.getTitle(), "Saved banner: " + slide.getTitle(), getIp(request));
        ra.addFlashAttribute("success", "Banner saved successfully!");
        return "redirect:/admin/slides";
    }

    @PostMapping("/slides/delete/{id}")
    public String deleteSlide(@PathVariable Long id, HttpServletRequest request, RedirectAttributes ra) {
        adminService.deleteSlide(id);
        adminService.logActivity(getAdminId(), getAdminName(), "SLIDE_DELETE", "SLIDE", id, 
                                 null, null, "Deleted banner ID: " + id, getIp(request));
        ra.addFlashAttribute("success", "Banner deleted.");
        return "redirect:/admin/slides";
    }

    // ─────────────────────────────────────────────
    // SALES ANALYTICS
    // ─────────────────────────────────────────────

    @GetMapping("/sales")
    public String sales(@RequestParam(required = false, defaultValue = "TODAY") String type,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        Model model) {
        
        try {
            LocalDate startD = LocalDate.now();
            LocalDate endD   = LocalDate.now();
            
            if (startDate != null && !startDate.isBlank()) {
                try { startD = LocalDate.parse(startDate); } catch (Exception e) { startD = LocalDate.now(); }
            }
            if (endDate != null && !endDate.isBlank()) {
                try { endD = LocalDate.parse(endDate); } catch (Exception e) { endD = LocalDate.now(); }
            }
            
            Map<String, Object> salesData = adminService.getSalesData(type, startD, endD);
            model.addAttribute("salesData",  salesData);
            model.addAttribute("startDate",  startD.toString());
            model.addAttribute("endDate",    endD.toString());
            model.addAttribute("activeType", type);
            model.addAttribute("activeMenu", "sales");
            return "admin/sales";
        } catch (Exception e) {
            // Ultimate fallback to prevent blank screen
            return "redirect:/admin/sales?type=TODAY";
        }
    }

    // ─────────────────────────────────────────────
    // USERS
    // ─────────────────────────────────────────────

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users",      adminService.getAllUsers());
        model.addAttribute("activeMenu", "users");
        return "admin/users";
    }

    @PostMapping("/users/toggle-status")
    public String toggleUserStatus(@RequestParam Long userId, HttpServletRequest request, RedirectAttributes ra) {
        Optional<User> userOpt = userRepository.findById(java.util.Objects.requireNonNull(userId));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("Trulybare@admin".equalsIgnoreCase(user.getEmail())) {
                ra.addFlashAttribute("error", "Cannot suspend the primary admin account!");
            } else {
                boolean newStatus = !user.getIsActive();
                user.setIsActive(newStatus);
                userRepository.save(user);
                adminService.logActivity(getAdminId(), getAdminName(), "USER_TOGGLE_STATUS", "USER", userId, 
                                         null, String.valueOf(newStatus), "User " + (newStatus ? "Activated" : "Suspended"), getIp(request));
                ra.addFlashAttribute("success", "User status updated to " + (newStatus ? "Active" : "Suspended"));
            }
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return adminService.getUserById(id).map(u -> {
            model.addAttribute("user",       u);
            model.addAttribute("activeMenu", "users");
            return "admin/user-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        });
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user,
                           @RequestParam(required = false) String newPassword,
                           HttpServletRequest request,
                           RedirectAttributes ra) {
        if ("Trulybare@admin".equalsIgnoreCase(user.getEmail())) {
            user.setRole(com.trulybare.enums.UserRole.ADMIN);
            user.setIsActive(true);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        adminService.saveUser(user);
        adminService.logActivity(getAdminId(), getAdminName(), "USER_SAVE", "USER", user.getUserId(), 
                                 null, user.getEmail(), "Updated user profile: " + user.getEmail(), getIp(request));
        ra.addFlashAttribute("success", "User details updated successfully!");
        return "redirect:/admin/users";
    }
    private String formatYouTubeUrl(String url) {
        if (url == null) return null;
        String formatted = url.trim();
        if (formatted.contains("youtube.com/watch?v=")) {
            String videoId = formatted.substring(formatted.indexOf("v=") + 2);
            int ampersandPos = videoId.indexOf("&");
            if (ampersandPos != -1) {
                videoId = videoId.substring(0, ampersandPos);
            }
            return "https://www.youtube.com/embed/" + videoId;
        } else if (formatted.contains("youtu.be/")) {
            String videoId = formatted.substring(formatted.indexOf("youtu.be/") + 9);
            int queryPos = videoId.indexOf("?");
            if (queryPos != -1) {
                videoId = videoId.substring(0, queryPos);
            }
            return "https://www.youtube.com/embed/" + videoId;
        }
        return formatted;
    }
}
