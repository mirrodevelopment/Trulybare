package com.trulybare.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.trulybare.entity.*;
import com.trulybare.enums.ReturnStatus;
import com.trulybare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnMessageRepository returnMessageRepository;
    private final AdminActivityLogRepository adminActivityLogRepository;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // ─────────────────────────────────────────────
    // SUBMIT RETURN REQUEST
    // ─────────────────────────────────────────────

    @Transactional
    public ReturnRequest submitReturn(User user,
                                      Long orderId,
                                      String reason,
                                      String returnType,
                                      String description,
                                      List<MultipartFile> images,
                                      String uploadDir) throws IOException {

        if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null.");
        Order order = orderRepository.findById(Objects.requireNonNull(orderId))
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Ownership check
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("You are not authorised to return this order.");
        }

        // Duplicate check
        if (returnRequestRepository.existsByOrder_OrderIdAndUser(orderId, user)) {
            throw new IllegalStateException("A return request already exists for this order.");
        }

        ReturnRequest rr = new ReturnRequest();
        rr.setOrder(order);
        rr.setUser(user);
        rr.setReason(reason);
        rr.setReturnType(returnType);
        rr.setDescription(description);
        rr.setStatus(ReturnStatus.PENDING_REVIEW);
        rr.setChatOpen(true);

        // Product info from first order item
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            OrderItem first = order.getOrderItems().get(0);
            if (first.getProduct() != null) {
                rr.setProductId(first.getProduct().getProductId());
                rr.setProductName(first.getProduct().getName());
            }
        }

        // Payment ID
        if (order.getPayment() != null && order.getPayment().getRazorpayPaymentId() != null) {
            rr.setPaymentId(order.getPayment().getRazorpayPaymentId());
        }

        // Save to get ID
        rr = returnRequestRepository.save(rr);

        // Process images (1–5)
        if (images != null) {
            int count = 0;
            for (MultipartFile file : images) {
                if (count >= 5) break;
                if (file == null || file.isEmpty()) continue;

                String imageUrl = saveReturnImage(file, rr.getId(), uploadDir);
                ReturnImage img = new ReturnImage();
                img.setReturnRequest(rr);
                img.setImageUrl(imageUrl);
                rr.getImages().add(img);
                count++;
            }
        }

        // Initial status log
        addStatusLog(rr, null, ReturnStatus.PENDING_REVIEW, user.getFullName(), "Return request submitted.");

        // Auto welcome message from system
        ReturnMessage welcome = new ReturnMessage();
        welcome.setReturnRequest(rr);
        welcome.setSenderType("ADMIN");
        welcome.setSenderName("Truly Bare Support");
        welcome.setMessage("Hi " + user.getFullName() + "! We've received your return request (RET-" +
                String.format("%03d", rr.getId()) + "). Our team will review it shortly. You can track the status and chat with us here.");
        rr.getMessages().add(welcome);

        return returnRequestRepository.save(rr);
    }

    // ─────────────────────────────────────────────
    // USER SENDS CHAT MESSAGE
    // ─────────────────────────────────────────────

    @Transactional
    public ReturnMessage sendUserMessage(Long returnRequestId, User user,
                                         String message, MultipartFile attachment,
                                         String uploadDir) throws IOException {

        if (returnRequestId == null) throw new IllegalArgumentException("Return ID cannot be null.");
        ReturnRequest rr = returnRequestRepository.findById(Objects.requireNonNull(returnRequestId))
                .orElseThrow(() -> new RuntimeException("Return request not found."));

        if (!rr.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("Not authorised.");
        }
        if (!Boolean.TRUE.equals(rr.getChatOpen())) {
            throw new IllegalStateException("Chat is closed for this request.");
        }

        ReturnMessage msg = new ReturnMessage();
        msg.setReturnRequest(rr);
        msg.setSenderType("USER");
        msg.setSenderName(user.getFullName());
        msg.setMessage(message);

        if (attachment != null && !attachment.isEmpty()) {
            String url = saveReturnImage(attachment, returnRequestId, uploadDir);
            msg.setAttachmentUrl(url);
        }

        return returnMessageRepository.save(msg);
    }

    // ─────────────────────────────────────────────
    // ADMIN SENDS CHAT MESSAGE
    // ─────────────────────────────────────────────

    @Transactional
    public ReturnMessage sendAdminMessage(Long returnRequestId, String adminName,
                                           String message, MultipartFile attachment, String uploadDir,
                                           Long adminId, String ipAddress) throws IOException {

        if (returnRequestId == null) throw new IllegalArgumentException("Return ID cannot be null.");
        ReturnRequest rr = returnRequestRepository.findById(Objects.requireNonNull(returnRequestId))
                .orElseThrow(() -> new RuntimeException("Return request not found."));

        ReturnMessage msg = new ReturnMessage();
        msg.setReturnRequest(rr);
        msg.setSenderType("ADMIN");
        msg.setSenderName(adminName);
        msg.setMessage(message);

        if (attachment != null && !attachment.isEmpty()) {
            String url = saveReturnImage(attachment, returnRequestId, uploadDir);
            msg.setAttachmentUrl(url);
        }

        msg = returnMessageRepository.save(msg);

        // Auto-reopen chat when admin sends a message
        if (!Boolean.TRUE.equals(rr.getChatOpen())) {
            rr.setChatOpen(true);
            returnRequestRepository.save(rr);
        }

        logActivity(adminId, adminName, "ADMIN_MESSAGE", "RETURN_REQUEST", returnRequestId,
                null, null, "Admin sent message: \"" + truncate(message, 80) + "\"", ipAddress);

        return msg;
    }

    // ─────────────────────────────────────────────
    // ADMIN: UPDATE STATUS
    // ─────────────────────────────────────────────

    @Transactional
    public ReturnRequest updateStatus(Long returnRequestId, ReturnStatus newStatus,
                                       String adminRemarks, Long adminId, String adminName, String ipAddress) {

        if (returnRequestId == null) throw new IllegalArgumentException("Return ID cannot be null.");
        ReturnRequest rr = returnRequestRepository.findById(Objects.requireNonNull(returnRequestId))
                .orElseThrow(() -> new RuntimeException("Return request not found."));

        ReturnStatus oldStatus = rr.getStatus();
        rr.setStatus(newStatus);
        if (adminRemarks != null && !adminRemarks.isBlank()) {
            rr.setAdminRemarks(adminRemarks);
        }

        // Toggle chat status based on terminal states
        if (newStatus == ReturnStatus.COMPLETED || newStatus == ReturnStatus.REJECTED
                || newStatus == ReturnStatus.REFUND_COMPLETED || newStatus == ReturnStatus.REFUND_FAILED) {
            rr.setChatOpen(false);
        } else {
            // Re-open chat if moved back to active states
            rr.setChatOpen(true);
        }

        addStatusLog(rr, oldStatus, newStatus, adminName, adminRemarks);

        logActivity(adminId, adminName, "STATUS_CHANGE", "RETURN_REQUEST", returnRequestId,
                oldStatus.name(), newStatus.name(),
                "Status changed from " + oldStatus.name() + " -> " + newStatus.name(), ipAddress);

        return returnRequestRepository.save(rr);
    }

    // ─────────────────────────────────────────────
    // ADMIN: APPROVE REFUND (Razorpay)
    // ─────────────────────────────────────────────

    @Transactional
    public ReturnRequest approveRefund(Long returnRequestId, Long adminId, String adminName, String ipAddress) {

        if (returnRequestId == null) throw new IllegalArgumentException("Return ID cannot be null.");
        ReturnRequest rr = returnRequestRepository.findById(Objects.requireNonNull(returnRequestId))
                .orElseThrow(() -> new RuntimeException("Return request not found."));

        // Duplicate refund guard
        if (rr.getRazorpayRefundId() != null || rr.getStatus() == ReturnStatus.REFUND_COMPLETED) {
            throw new IllegalStateException("Refund already completed or initiated for this request.");
        }

        Order order = rr.getOrder();
        if (order == null) {
            throw new RuntimeException("Associated order not found.");
        }

        // Check if COD
        boolean isCod = order.getPayment() != null && "COD".equalsIgnoreCase(order.getPayment().getPaymentMethod());

        // Fetch payment details (only if not COD)
        String paymentId = null;
        if (!isCod) {
            paymentId = rr.getPaymentId();
            if (paymentId == null || paymentId.isBlank()) {
                // Try to get from order.payment
                if (order.getPayment() != null && order.getPayment().getRazorpayPaymentId() != null) {
                    paymentId = order.getPayment().getRazorpayPaymentId();
                } else {
                    throw new RuntimeException("No Razorpay payment ID found — cannot process refund.");
                }
            }
        }

        // Calculate refund amount: Always deduct delivery fee from the total amount
        BigDecimal total = order.getTotalAmount();
        BigDecimal deliveryFee = order.getShippingFees() != null ? order.getShippingFees() : BigDecimal.ZERO;

        BigDecimal refundAmount = total.subtract(deliveryFee);
        BigDecimal deducted = deliveryFee;

        // Validation: refund >= 0
        if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            refundAmount = BigDecimal.ZERO;
        }

        rr.setRefundAmount(refundAmount);
        rr.setDeliveryFeeDeducted(deducted);

        if (isCod) {
            // For COD, we mark as completed immediately as it's a manual process
            rr.setStatus(ReturnStatus.REFUND_COMPLETED);
            rr.setChatOpen(false);

            logActivity(adminId, adminName, "REFUND_COMPLETED", "RETURN_REQUEST", returnRequestId,
                    null, null,
                    "Manual refund approved for COD order. Amount: ₹" + refundAmount, ipAddress);

            addStatusLog(rr, ReturnStatus.APPROVED, ReturnStatus.REFUND_COMPLETED, adminName,
                    "Refund of ₹" + refundAmount + " approved for COD order (Manual process).");

            // Update Order Status to RETURNED
            order.setStatus(com.trulybare.enums.OrderStatus.RETURNED);
            orderRepository.save(order);

            return returnRequestRepository.save(rr);
        }

        // Trigger Razorpay refund (only for ONLINE)
        rr.setStatus(ReturnStatus.REFUND_INITIATED);
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject refundRequest = new JSONObject();
            // Razorpay uses paise (multiply by 100)
            long amountPaise = refundAmount.multiply(new BigDecimal("100")).longValue();
            refundRequest.put("amount", amountPaise);
            refundRequest.put("speed", "normal");
            refundRequest.put("notes", new JSONObject()
                    .put("return_id", "RET-" + String.format("%03d", rr.getId()))
                    .put("reason", rr.getReason()));

            com.razorpay.Refund refund = client.payments.refund(paymentId, refundRequest);
            String refundId = refund.get("id");
            rr.setRazorpayRefundId(refundId);

            log.info("Razorpay refund created: {} for payment: {}", refundId, paymentId);

            logActivity(adminId, adminName, "REFUND_INITIATED", "RETURN_REQUEST", returnRequestId,
                    null, refundId,
                    "Refund initiated ₹" + refundAmount + (deducted.compareTo(BigDecimal.ZERO) > 0
                            ? " (delivery ₹" + deducted + " deducted)" : " (full refund)"),
                    ipAddress);

        } catch (RazorpayException e) {
            log.error("Razorpay refund failed: {}", e.getMessage());
            rr.setStatus(ReturnStatus.REFUND_FAILED);
            addStatusLog(rr, ReturnStatus.REFUND_INITIATED, ReturnStatus.REFUND_FAILED, adminName, "Razorpay error: " + e.getMessage());
            returnRequestRepository.save(rr);
            throw new RuntimeException("Razorpay refund failed: " + e.getMessage());
        }

        addStatusLog(rr, ReturnStatus.APPROVED, ReturnStatus.REFUND_INITIATED, adminName,
                "Refund of ₹" + refundAmount + " initiated via Razorpay.");

        return returnRequestRepository.save(rr);
    }

    // ─────────────────────────────────────────────
    // ADMIN: APPROVE REPLACEMENT
    // ─────────────────────────────────────────────

    @Transactional
    public ReturnRequest approveReplacement(Long returnRequestId, Long adminId, String adminName, String ipAddress) {
        if (returnRequestId == null) throw new IllegalArgumentException("Return ID cannot be null.");
        ReturnRequest rr = returnRequestRepository.findById(Objects.requireNonNull(returnRequestId))
                .orElseThrow(() -> new RuntimeException("Return request not found."));

        ReturnStatus oldStatus = rr.getStatus();
        rr.setStatus(ReturnStatus.REPLACEMENT_PROCESSING);
        addStatusLog(rr, oldStatus, ReturnStatus.REPLACEMENT_PROCESSING, adminName, "Replacement approved, processing dispatch.");

        logActivity(adminId, adminName, "REPLACEMENT_APPROVED", "RETURN_REQUEST", returnRequestId,
                oldStatus.name(), ReturnStatus.REPLACEMENT_PROCESSING.name(),
                "Replacement approved for RET-" + String.format("%03d", returnRequestId), ipAddress);

        // Notify customer via chat
        ReturnMessage notif = new ReturnMessage();
        notif.setReturnRequest(rr);
        notif.setSenderType("ADMIN");
        notif.setSenderName(adminName);
        notif.setMessage("Great news! Your replacement has been approved and is being processed. We'll dispatch your new item soon. Thank you for your patience!");
        rr.getMessages().add(notif);

        // Update Order Status to RETURNED
        Order order = rr.getOrder();
        if (order != null) {
            order.setStatus(com.trulybare.enums.OrderStatus.RETURNED);
            orderRepository.save(order);
        }

        return returnRequestRepository.save(rr);
    }

    // ─────────────────────────────────────────────
    // ADMIN: REJECT
    // ─────────────────────────────────────────────

    @Transactional
    public ReturnRequest rejectReturn(Long returnRequestId, String rejectionReason,
                                       Long adminId, String adminName, String ipAddress) {
        if (returnRequestId == null) throw new IllegalArgumentException("Return ID cannot be null.");
        ReturnRequest rr = returnRequestRepository.findById(Objects.requireNonNull(returnRequestId))
                .orElseThrow(() -> new RuntimeException("Return request not found."));

        ReturnStatus oldStatus = rr.getStatus();
        rr.setStatus(ReturnStatus.REJECTED);
        rr.setAdminRemarks(rejectionReason);
        rr.setChatOpen(false);

        addStatusLog(rr, oldStatus, ReturnStatus.REJECTED, adminName, rejectionReason);

        logActivity(adminId, adminName, "REJECTION", "RETURN_REQUEST", returnRequestId,
                oldStatus.name(), ReturnStatus.REJECTED.name(),
                "Request rejected. Reason: " + truncate(rejectionReason, 100), ipAddress);

        // Notify user via chat
        ReturnMessage msg = new ReturnMessage();
        msg.setReturnRequest(rr);
        msg.setSenderType("ADMIN");
        msg.setSenderName(adminName);
        msg.setMessage("We're sorry, but your return request has been reviewed and cannot be approved at this time. " +
                (rejectionReason != null ? "Reason: " + rejectionReason : "") +
                " If you have questions, please contact our support team.");
        rr.getMessages().add(msg);

        return returnRequestRepository.save(rr);
    }

    // ─────────────────────────────────────────────
    // WEBHOOK: Update refund status
    // ─────────────────────────────────────────────

    @Transactional
    public void handleRefundWebhook(String razorpayRefundId, String event) {
        returnRequestRepository.findAll().stream()
                .filter(r -> razorpayRefundId.equals(r.getRazorpayRefundId()))
                .findFirst()
                .ifPresent(rr -> {
                    ReturnStatus oldStatus = rr.getStatus();
                    if ("refund.processed".equals(event)) {
                        rr.setStatus(ReturnStatus.REFUND_COMPLETED);
                        rr.setChatOpen(false);
                        addStatusLog(rr, oldStatus, ReturnStatus.REFUND_COMPLETED, "SYSTEM",
                                "Refund confirmed by Razorpay webhook.");
                        // Notify user
                        ReturnMessage msg = new ReturnMessage();
                        msg.setReturnRequest(rr);
                        msg.setSenderType("ADMIN");
                        msg.setSenderName("Truly Bare Support");
                        msg.setMessage("Your refund of ₹" + rr.getRefundAmount() +
                                " has been successfully processed. It should reflect in your account within 5–7 business days.");
                        rr.getMessages().add(msg);

                        // Update Order Status to RETURNED
                        Order order = rr.getOrder();
                        if (order != null) {
                            order.setStatus(com.trulybare.enums.OrderStatus.RETURNED);
                            orderRepository.save(order);
                        }
                    } else if ("refund.failed".equals(event)) {
                        rr.setStatus(ReturnStatus.REFUND_FAILED);
                        addStatusLog(rr, oldStatus, ReturnStatus.REFUND_FAILED, "SYSTEM",
                                "Refund failed per Razorpay webhook.");
                    }
                    returnRequestRepository.save(rr);
                });
    }

    // ─────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────

    public List<ReturnRequest> getAllReturns() {
        return returnRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ReturnRequest> getReturnsByStatus(ReturnStatus status) {
        return returnRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Optional<ReturnRequest> getById(Long id) {
        if (id == null) return Optional.empty();
        return returnRequestRepository.findById(Objects.requireNonNull(id));
    }

    public List<ReturnRequest> getUserReturns(User user) {
        return returnRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public boolean hasReturnForOrder(Long orderId, User user) {
        return returnRequestRepository.existsByOrder_OrderIdAndUser(orderId, user);
    }

    public List<AdminActivityLog> getRecentActivityLogs(int limit) {
        List<AdminActivityLog> all = adminActivityLogRepository.findAllByOrderByCreatedAtDesc();
        return all != null ? (all.size() > limit ? all.subList(0, limit) : all) : new ArrayList<>();
    }

    public List<AdminActivityLog> getActivityLogsForReturn(Long returnRequestId) {
        return adminActivityLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("RETURN_REQUEST", returnRequestId);
    }

    public List<AdminActivityLog> getAllActivityLogs() {
        return getAllActivityLogs(null);
    }

    public List<AdminActivityLog> getAllActivityLogs(java.time.LocalDate date) {
        List<AdminActivityLog> logs;
        if (date != null) {
            java.time.LocalDateTime start = date.atStartOfDay();
            java.time.LocalDateTime end = date.atTime(23, 59, 59);
            logs = adminActivityLogRepository.findByActionTypeNotAndCreatedAtBetweenOrderByCreatedAtDesc("ADMIN_MESSAGE", start, end);
        } else {
            logs = adminActivityLogRepository.findByActionTypeNotOrderByCreatedAtDesc("ADMIN_MESSAGE");
        }
        return logs != null ? logs : new ArrayList<>();
    }

    public Map<String, Long> getReturnStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ReturnStatus s : ReturnStatus.values()) {
            counts.put(s.name(), returnRequestRepository.countByStatus(s));
        }
        return counts;
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private String saveReturnImage(MultipartFile file, Long returnId, String uploadDir) throws IOException {
        String dir = uploadDir + File.separator + "returns" + File.separator + returnId;
        Path dirPath = Paths.get(dir);
        Files.createDirectories(dirPath);

        String rawName = file.getOriginalFilename();
        String originalName = (rawName != null) ? rawName : "img";
        String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : ".jpg";
        String fileName = "img_" + System.currentTimeMillis() + ext;

        Path filePath = dirPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/returns/" + returnId + "/" + fileName;
    }

    private void addStatusLog(ReturnRequest rr, ReturnStatus oldStatus, ReturnStatus newStatus,
                               String changedBy, String note) {
        ReturnStatusLog log = new ReturnStatusLog();
        log.setReturnRequest(rr);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangedBy(changedBy);
        log.setNote(note);
        rr.getStatusLogs().add(log);
    }

    private void logActivity(Long adminId, String adminName, String actionType,
                              String targetType, Long targetId,
                              String oldValue, String newValue, String description, String ipAddress) {
        AdminActivityLog log = new AdminActivityLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        adminActivityLogRepository.save(log);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
