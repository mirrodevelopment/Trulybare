package com.trulybare.service;

import com.trulybare.entity.Order;
import com.trulybare.entity.User;
import jakarta.servlet.http.HttpSession;

public interface OrderService {
    Order placeOrder(HttpSession session, User user, Long addressId, String customAddress, String paymentMethod, Integer pointsToUse, String razorpayPaymentId, String razorpayOrderId, String razorpaySignature);

    Order getOrderById(Long orderId);
}
