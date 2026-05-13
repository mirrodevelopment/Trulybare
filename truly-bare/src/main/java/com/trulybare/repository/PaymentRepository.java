package com.trulybare.repository;

import com.trulybare.entity.Order;
import com.trulybare.entity.Payment;
import com.trulybare.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by order
    Optional<Payment> findByOrder(Order order);

    // Find payment by Razorpay order ID
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    // Find payment by Razorpay payment ID
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    // Find all payments by status (for admin)
    List<Payment> findByStatus(PaymentStatus status);

    // Find successful payments
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
}