package com.trulybare.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.trulybare.dto.CartDto;
import com.trulybare.entity.User;
import com.trulybare.service.CartService;
import com.trulybare.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency}")
    private String currency;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createRazorpayOrder(@RequestParam(required = false, defaultValue = "0") Integer pointsToUse,
                                                 Principal principal,
                                                 HttpSession session) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            CartDto cartDto = cartService.getCart(session);
            if (cartDto.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }

            User user = userService.findByEmail(principal.getName());

            java.math.BigDecimal total = cartDto.getTotal();
            java.math.BigDecimal discount = java.math.BigDecimal.ZERO;

            if (pointsToUse != null && pointsToUse > 0) {
                int availablePoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
                if (pointsToUse > availablePoints) {
                    return ResponseEntity.badRequest().body("Insufficient loyalty points");
                }

                discount = java.math.BigDecimal.valueOf(pointsToUse);
                if (discount.compareTo(total) > 0) {
                    discount = total;
                }
                total = total.subtract(discount);
            }

            // Convert total to paise (multiply by 100)
            int amountInPaise = total.multiply(new java.math.BigDecimal("100")).intValue();

            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", razorpayOrder.get("id"));
            response.put("amount", amountInPaise);
            response.put("currency", currency);
            response.put("keyId", keyId);
            response.put("userEmail", user.getEmail());
            response.put("userContact", user.getPhone() != null ? user.getPhone() : "9999999999");
            response.put("userName", user.getFullName());

            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Razorpay error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating order: " + e.getMessage());
        }
    }
}
