package com.trulybare.service.impl;

import com.trulybare.dto.CartDto;
import com.trulybare.dto.CartItemDto;
import com.trulybare.entity.*;
import com.trulybare.enums.OrderStatus;
import com.trulybare.enums.PaymentStatus;
import com.trulybare.repository.AddressRepository;
import com.trulybare.repository.OrderRepository;
import com.trulybare.repository.PaymentRepository;
import com.trulybare.repository.ProductRepository;
import com.trulybare.service.CartService;
import com.trulybare.service.OrderService;
import com.trulybare.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Order placeOrder(HttpSession session, User user, Long addressId, String customAddress, String paymentMethod, Integer pointsToUse, String razorpayPaymentId, String razorpayOrderId, String razorpaySignature) {
        CartDto cartDto = cartService.getCart(session);
        if (cartDto.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order with empty cart.");
        }

        // Resolve shipping address string
        String shippingAddressStr;
        if (customAddress != null && !customAddress.isBlank()) {
            // Custom address typed by user
            shippingAddressStr = customAddress;
        } else {
            if (addressId == null) {
                throw new IllegalArgumentException("Address ID cannot be null.");
            }
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found."));
            shippingAddressStr = address.getAddressLine1() + ", " + address.getCity() + ", " + address.getState() + " - " + address.getPincode();
        }

        Order order = new Order();
        order.setUser(user);
        
        java.math.BigDecimal total = cartDto.getTotal();
        java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
        
        // Handle Loyalty Points Redemption
        if (pointsToUse != null && pointsToUse > 0) {
            int availablePoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
            if (pointsToUse > availablePoints) {
                throw new RuntimeException("Insufficient loyalty points.");
            }
            
            // 1 point = 1 INR conversion
            discount = java.math.BigDecimal.valueOf(pointsToUse);
            
            // Ensure discount doesn't exceed order total
            if (discount.compareTo(total) > 0) {
                discount = total;
                pointsToUse = total.intValue();
            }
            
            total = total.subtract(discount);
            user.setLoyaltyPoints(availablePoints - pointsToUse);
            order.setPointsUsed(pointsToUse);
            order.setDiscountAmount(discount);
        }

        order.setTotalAmount(total);
        order.setSubtotal(cartDto.getSubtotal());
        order.setShippingFees(cartDto.getShipping());
        order.setShippingAddress(shippingAddressStr);

        order.setStatus(OrderStatus.PENDING);
        if ("ONLINE".equals(paymentMethod)) {
            order.setPaymentStatus(PaymentStatus.SUCCESS);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        
        // Convert cart items to order items
        order.setOrderItems(cartDto.getItems().stream().map(itemDto -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);

            Long productId = itemDto.getProductId();
            if (productId == null) {
                throw new RuntimeException("Cart item is missing a product ID.");
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(itemDto.getPrice());
            return orderItem;
        }).collect(Collectors.toList()));

        Order savedOrder = orderRepository.save(order);

        // Save Payment details
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setAmount(total);
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");
        
        if ("ONLINE".equals(paymentMethod)) {
            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setRazorpaySignature(razorpaySignature);
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }
        
        paymentRepository.save(payment);
        savedOrder.setPayment(payment); // Attach to order object for immediate use in current transaction
        
        // Update User Loyalty Stats
        updateUserLoyalty(user, savedOrder);
        
        // Clear the cart after successfully placing the order
        cartService.clearCart(session);
        
        return savedOrder;
    }

    private void updateUserLoyalty(User user, Order order) {
        if (user == null || order == null) return;

        // Increment Order Count
        int newTotalOrders = (user.getTotalOrders() != null ? user.getTotalOrders() : 0) + 1;
        user.setTotalOrders(newTotalOrders);

        // Update Status Tier
        String oldStatus = user.getCustomerStatus() != null ? user.getCustomerStatus() : "Bronze";
        String newStatus = "Bronze";
        if (newTotalOrders >= 100) newStatus = "Elite";
        else if (newTotalOrders >= 50) newStatus = "Platinum";
        else if (newTotalOrders >= 25) newStatus = "Gold";
        else if (newTotalOrders >= 10) newStatus = "Silver";
        user.setCustomerStatus(newStatus);

        // Tier Level Up Bonus
        boolean leveledUp = !newStatus.equals(oldStatus) && !"Bronze".equals(newStatus);
        int currentPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
        int pointsEarned = leveledUp ? 25 : 0; // 25 Points Bonus for reaching next status

        // First order bonus
        if (user.getFirstOrderCompleted() == null || !user.getFirstOrderCompleted()) {
            pointsEarned += 50;
            user.setFirstOrderCompleted(true);
        }

        // Percentage based on status
        double rate = 0.02; // Bronze/Silver 2%
        if ("Elite".equals(newStatus)) rate = 0.05;
        else if ("Platinum".equals(newStatus)) rate = 0.03;
        else if ("Gold".equals(newStatus)) rate = 0.025;

        if (order.getTotalAmount() != null) {
            pointsEarned += order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(rate)).intValue();
        }

        user.setLoyaltyPoints(currentPoints + pointsEarned);
        userRepository.save(user);
    }

    @Override
    public Order getOrderById(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null.");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found."));
    }
}
