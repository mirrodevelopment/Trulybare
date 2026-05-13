package com.trulybare.service;

import com.trulybare.entity.*;
import com.trulybare.enums.OrderStatus;
import com.trulybare.enums.ReturnStatus;
import com.trulybare.enums.ReviewStatus;
import com.trulybare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Objects;

@Service
public class AdminService {

    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    
    @jakarta.annotation.PostConstruct
    public void cleanupSchema() {
        String[] columnsToDrop = {"active", "sort_order"};
        for (String col : columnsToDrop) {
            try {
                jdbcTemplate.execute("ALTER TABLE vlogs DROP COLUMN IF EXISTS " + col);
            } catch (Exception e) {
                try {
                    jdbcTemplate.execute("ALTER TABLE vlogs DROP COLUMN " + col);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
    }
    
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReturnRepository returnRepository;
    @Autowired private SlideRepository slideRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AdminActivityLogRepository adminActivityLogRepository;
    @Autowired private AppSettingRepository appSettingRepository;
    @Autowired private RitualCardRepository ritualCardRepository;
    @Autowired private ReturnRequestRepository returnRequestRepository;
    @Autowired private VlogRepository vlogRepository;
    @Autowired private GcsStorageService gcsStorageService;

    // ─────────────────────────────────────────────
    // RITUAL CARD MANAGEMENT
    // ─────────────────────────────────────────────

    public List<RitualCard> getAllRitualCards() {
        return ritualCardRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<RitualCard> getActiveRitualCards() {
        return ritualCardRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public Optional<RitualCard> getRitualCardById(Long id) {
        if (id == null) return Optional.empty();
        return ritualCardRepository.findById(Objects.requireNonNull(id));
    }

    @Transactional
    public void saveRitualCard(RitualCard card) {
        if (card.getProduct() != null && card.getProduct().getProductId() != null) {
            Product managed = productRepository.findById(Objects.requireNonNull(card.getProduct().getProductId())).orElse(null);
            card.setProduct(managed);
        } else {
            card.setProduct(null);
        }
        ritualCardRepository.save(card);
    }

    @Transactional
    public void deleteRitualCard(Long id) {
        if (id != null) ritualCardRepository.deleteById(Objects.requireNonNull(id));
    }

    // ─────────────────────────────────────────────
    // DASHBOARD SUMMARY
    // ─────────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = LocalDate.now().atTime(23, 59, 59);

        // Totals
        stats.put("totalProducts",  productRepository.count());
        stats.put("totalOrders",    orderRepository.count());
        stats.put("totalUsers",     userRepository.count());
        stats.put("pendingReviews", reviewRepository.countByReviewStatus(ReviewStatus.PENDING));
        stats.put("pendingReturns", returnRequestRepository.countByStatus(ReturnStatus.PENDING_REVIEW));
        stats.put("pendingOrders",  orderRepository.countByStatus(OrderStatus.PENDING));

        // Today
        BigDecimal todayRevenue = orderItemRepository.findTotalRevenueBetween(todayStart, todayEnd);
        Long       todayOrders  = orderItemRepository.findTotalOrdersBetween(todayStart, todayEnd);
        stats.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        stats.put("todayOrders",  todayOrders  != null ? todayOrders  : 0L);

        // Low stock
        long lowStock = productRepository.findAll()
                .stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() < 10).count();
        stats.put("lowStockCount", lowStock);

        return stats;
    }

    @Transactional
    public void logActivity(Long adminId, String adminName, String action, String targetType, Long targetId,
                             String oldVal, String newVal, String desc, String ip) {
        AdminActivityLog log = new AdminActivityLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setActionType(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setDescription(desc);
        log.setIpAddress(ip);
        adminActivityLogRepository.save(log);
    }

    // ─────────────────────────────────────────────
    // PRODUCT MANAGEMENT
    // ─────────────────────────────────────────────

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        if (id == null) return Optional.empty();
        return productRepository.findById(Objects.requireNonNull(id));
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Optional<Category> getCategoryById(Long id) {
        if (id == null) return Optional.empty();
        return categoryRepository.findById(Objects.requireNonNull(id));
    }

    @Transactional
    public Category saveCategory(Category category) {
        if (category == null) return null;
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (id != null) categoryRepository.deleteById(Objects.requireNonNull(id));
    }

    @Transactional
    public boolean toggleCategoryStatus(Long id) {
        if (id == null) return false;
        Category cat = categoryRepository.findById(Objects.requireNonNull(id)).orElseThrow();
        boolean newStatus = !Boolean.TRUE.equals(cat.getIsActive());
        cat.setIsActive(newStatus);
        categoryRepository.save(cat);
        return newStatus;
    }

    @Transactional
    public Product saveProduct(Product product) {
        if (product == null) return null;
        return productRepository.save(Objects.requireNonNull(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (id != null) productRepository.deleteById(Objects.requireNonNull(id));
    }

    public boolean productNameExists(String name, Long excludeId) {
        Optional<Product> existing = productRepository.findByName(name);
        return existing.isPresent() && !existing.get().getProductId().equals(excludeId);
    }

    public String saveProductImage(MultipartFile file, String productName) {
        if (file == null || file.isEmpty()) return null;
        return gcsStorageService.uploadFile(file, "products", productName);
    }

    public String saveRitualCardImage(MultipartFile file, String cardTitle) {
        if (file == null || file.isEmpty()) return null;
        return gcsStorageService.uploadFile(file, "ritual-cards", cardTitle);
    }

    /** Deletes an old image from GCS when it is being replaced. */
    public void deleteOldImage(String oldUrl) {
        gcsStorageService.deleteByUrl(oldUrl);
    }

    // ─────────────────────────────────────────────
    // INVENTORY MANAGEMENT
    // ─────────────────────────────────────────────

    public List<Product> getAllInventory() {
        return productRepository.findAll();
    }

    @Transactional
    public void updateStock(Long productId, int quantity) {
        if (productId == null) return;
        productRepository.findById(Objects.requireNonNull(productId)).ifPresent(p -> {
            if (quantity < 0) throw new IllegalArgumentException("Stock cannot be negative.");
            p.setStockQuantity(quantity);
            productRepository.save(p);
        });
    }

    // ─────────────────────────────────────────────
    // ORDER MANAGEMENT
    // ─────────────────────────────────────────────

    public List<Order> getAllOrders() {
        return orderRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Optional<Order> getOrderById(Long id) {
        if (id == null) return Optional.empty();
        return orderRepository.findById(Objects.requireNonNull(id));
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderId == null) return;
        orderRepository.findById(Objects.requireNonNull(orderId)).ifPresent(o -> {
            OrderStatus oldStatus = o.getStatus();
            o.setStatus(newStatus);

            // Auto-deduct inventory when order is marked as packed/shipped
            if (newStatus == OrderStatus.SHIPPED && oldStatus != OrderStatus.SHIPPED) {
                if (o.getOrderItems() != null) {
                    for (OrderItem item : o.getOrderItems()) {
                        Product p = item.getProduct();
                        if (p != null) {
                            int currentStock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                            int newStock = Math.max(0, currentStock - item.getQuantity());
                            p.setStockQuantity(newStock);
                            productRepository.save(p);
                        }
                    }
                }
            }

            // Auto-mark COD payment as SUCCESS when order is DELIVERED
            if (newStatus == OrderStatus.DELIVERED) {
                boolean isCod = o.getPayment() != null
                        && "COD".equalsIgnoreCase(o.getPayment().getPaymentMethod());
                boolean alreadyPaid = o.getPaymentStatus() == com.trulybare.enums.PaymentStatus.SUCCESS;
                if (isCod && !alreadyPaid) {
                    o.setPaymentStatus(com.trulybare.enums.PaymentStatus.SUCCESS);
                    if (o.getPayment() != null) {
                        o.getPayment().setStatus(com.trulybare.enums.PaymentStatus.SUCCESS);
                    }
                }
            }

            orderRepository.save(o);
        });
    }

    // ─────────────────────────────────────────────
    // REVIEW MANAGEMENT
    // ─────────────────────────────────────────────

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Review> getReviewsByStatus(ReviewStatus status) {
        return reviewRepository.findByReviewStatusOrderByCreatedAtDesc(status);
    }

    @Transactional
    public void approveReview(Long reviewId) {
        if (reviewId == null) return;
        reviewRepository.findById(Objects.requireNonNull(reviewId)).ifPresent(r -> {
            r.setReviewStatus(ReviewStatus.APPROVED);
            r.setIsApproved(true);
            reviewRepository.save(r);
        });
    }

    @Transactional
    public void rejectReview(Long reviewId) {
        if (reviewId == null) return;
        reviewRepository.findById(Objects.requireNonNull(reviewId)).ifPresent(r -> {
            r.setReviewStatus(ReviewStatus.REJECTED);
            r.setIsApproved(false);
            reviewRepository.save(r);
        });
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        if (reviewId != null) reviewRepository.deleteById(Objects.requireNonNull(reviewId));
    }

    // ─────────────────────────────────────────────
    // RETURNS & REFUNDS
    // ─────────────────────────────────────────────

    public List<Return> getAllReturns() {
        return returnRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public List<Return> getReturnsByStatus(ReturnStatus status) {
        return returnRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Optional<Return> getReturnById(Long id) {
        if (id == null) return Optional.empty();
        return returnRepository.findById(Objects.requireNonNull(id));
    }

    @Transactional
    public void updateReturnStatus(Long returnId, ReturnStatus newStatus, String adminRemarks, BigDecimal refundAmount) {
        if (returnId == null) return;
        returnRepository.findById(Objects.requireNonNull(returnId)).ifPresent(r -> {
            // Map extended statuses to basic ones the old entity supports
            ReturnStatus mapped = newStatus;
            if (mapped == ReturnStatus.PENDING_REVIEW || mapped == ReturnStatus.UNDER_REVIEW) {
                mapped = ReturnStatus.PENDING_REVIEW;
            } else if (mapped == ReturnStatus.REFUND_INITIATED || mapped == ReturnStatus.REFUND_COMPLETED
                    || mapped == ReturnStatus.REPLACEMENT_PROCESSING) {
                mapped = ReturnStatus.APPROVED;
            } else if (mapped == ReturnStatus.REFUND_FAILED) {
                mapped = ReturnStatus.REJECTED;
            }
            r.setStatus(mapped);
            if (adminRemarks != null && !adminRemarks.isBlank()) r.setAdminRemarks(adminRemarks);
            if (refundAmount != null) r.setRefundAmount(refundAmount);
            returnRepository.save(r);
        });
    }

    // ─────────────────────────────────────────────
    // SLIDES / BANNERS
    // ─────────────────────────────────────────────

    public List<Slide> getAllSlides() {
        return slideRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<Slide> getActiveSlides() {
        return slideRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public Optional<Slide> getSlideById(Long id) {
        if (id == null) return Optional.empty();
        return slideRepository.findById(Objects.requireNonNull(id));
    }

    @Transactional
    public @NonNull Slide saveSlide(@NonNull Slide slide) {
        if (slide.getProduct() != null && slide.getProduct().getProductId() != null) {
            Product managed = productRepository.findById(Objects.requireNonNull(slide.getProduct().getProductId())).orElse(null);
            slide.setProduct(managed);
        } else {
            slide.setProduct(null);
        }
        return Objects.requireNonNull(slideRepository.save(slide));
    }

    @Transactional
    public void deleteSlide(Long id) {
        if (id != null) slideRepository.deleteById(Objects.requireNonNull(id));
    }

    // ─────────────────────────────────────────────
    // SALES ANALYTICS
    // ─────────────────────────────────────────────

    public Map<String, Object> getSalesData(String filterType, LocalDate startD, LocalDate endD) {
        LocalDateTime start;
        LocalDateTime end;

        if ("OVERALL".equalsIgnoreCase(filterType)) {
            start = LocalDateTime.of(2020, 1, 1, 0, 0); // Far past
            end = LocalDateTime.now();
        } else if ("YESTERDAY".equalsIgnoreCase(filterType)) {
            start = LocalDate.now().minusDays(1).atStartOfDay();
            end = LocalDate.now().minusDays(1).atTime(23, 59, 59);
        } else if ("CUSTOM".equalsIgnoreCase(filterType) && startD != null && endD != null) {
            start = startD.atStartOfDay();
            end = endD.atTime(23, 59, 59);
        } else if ("SELECTIVE".equalsIgnoreCase(filterType) && startD != null) {
            start = startD.atStartOfDay();
            end = startD.atTime(23, 59, 59);
        } else {
            // Default: TODAY
            start = LocalDate.now().atStartOfDay();
            end = LocalDate.now().atTime(23, 59, 59);
        }

        Map<String, Object> data = new LinkedHashMap<>();

        BigDecimal revenue = orderItemRepository.findTotalRevenueBetween(start, end);
        Long orders        = orderItemRepository.findTotalOrdersBetween(start, end);
        List<Object[]> productSales = orderItemRepository.findProductSalesBetween(start, end);

        data.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        data.put("totalOrders",  orders  != null ? orders  : 0L);
        
        // Calculate percentages in Java to prevent Thymeleaf crashes
        List<Map<String, Object>> safeSales = new ArrayList<>();
        BigDecimal totalRev = revenue != null ? revenue : BigDecimal.ZERO;
        
        if (productSales != null) {
            for (Object[] row : productSales) {
                Map<String, Object> item = new HashMap<>();
                item.put("name",    row[0] != null ? row[0] : "Unknown Product");
                item.put("units",   row[1] != null ? row[1] : 0);
                
                BigDecimal rowRev = (row[2] != null) ? (BigDecimal) row[2] : BigDecimal.ZERO;
                item.put("revenue", rowRev);
                
                int pct = 0;
                if (totalRev.compareTo(BigDecimal.ZERO) > 0) {
                    pct = rowRev.divide(totalRev, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).intValue();
                }
                item.put("pct", pct);
                safeSales.add(item);
            }
        }
        
        data.put("productSales", safeSales);
        data.put("start",        start.toLocalDate().toString());
        data.put("end",          end.toLocalDate().toString());
        data.put("filterType",   filterType != null ? filterType : "TODAY");

        return data;
    }
    // ─────────────────────────────────────────────
    // USER MANAGEMENT
    // ─────────────────────────────────────────────
    
    public List<User> getAllUsers() {
        return userRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public Optional<User> getUserById(Long id) {
        if (id == null) return Optional.empty();
        return userRepository.findById(Objects.requireNonNull(id));
    }

    @Transactional
    public void saveUser(User user) {
        if (user == null) return;
        Long userId = user.getUserId();
        
        if (userId != null) {
            // Update existing user
            userRepository.findById(Objects.requireNonNull(userId)).ifPresent(old -> {
                if (user.getPassword() == null || user.getPassword().isBlank()) {
                    user.setPassword(old.getPassword());
                }
                userRepository.save(user);
            });
        } else {
            // Create new user
            userRepository.save(user);
        }
    }

    // ─── APP SETTINGS (key-value store for CMS) ───

    /** Read a setting by key; returns defaultValue if not found. */
    public String getSetting(String key, String defaultValue) {
        return appSettingRepository.findById(key)
                .map(AppSetting::getValue)
                .orElse(defaultValue);
    }

    /** Insert or update a setting. */
    public void saveSetting(String key, String value) {
        if (key == null || key.isBlank()) return;
        appSettingRepository.save(new AppSetting(key, value == null ? "" : value));
    }
    // ─────────────────────────────────────────────
    // VLOG MANAGEMENT
    // ─────────────────────────────────────────────

    public List<Vlog> getAllVlogs() {
        return vlogRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.ASC, "displayOrder"));
    }

    public List<Vlog> getActiveVlogs() {
        return vlogRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public Optional<Vlog> getVlogById(Long id) {
        if (id == null) return Optional.empty();
        return vlogRepository.findById(id);
    }

    @Transactional
    public void saveVlog(Vlog vlog) {
        if (vlog == null) return;
        vlogRepository.save(vlog);
    }

    @Transactional
    public void deleteVlog(Long id) {
        if (id != null) vlogRepository.deleteById(id);
    }

    public String saveVlogFile(MultipartFile file, String title, String subDir) {
        if (file == null || file.isEmpty()) return null;
        return gcsStorageService.uploadFile(file, "vlogs/" + subDir, title);
    }
}