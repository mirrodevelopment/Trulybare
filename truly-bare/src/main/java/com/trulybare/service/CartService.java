package com.trulybare.service;

import com.trulybare.dto.CartDto;
import com.trulybare.dto.CartItemDto;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CartService {

    // Get current cart from session
    CartDto getCart(HttpSession session);

    // Add product to cart
    void addToCart(HttpSession session, Long productId, int quantity);

    // Update product quantity in cart
    void updateCartItem(HttpSession session, Long productId, int quantity);

    // Remove product from cart
    void removeFromCart(HttpSession session, Long productId);

    // Clear entire cart
    void clearCart(HttpSession session);

    // Get total number of items in cart
    int getCartItemCount(HttpSession session);

    // Calculate cart subtotal
    BigDecimal calculateSubtotal(HttpSession session);

    // Calculate total with shipping
    BigDecimal calculateTotal(HttpSession session);

    // Check if product is in cart
    boolean isProductInCart(HttpSession session, Long productId);

    // Get cart as Map (for internal use)
    Map<Long, Integer> getCartMap(HttpSession session);

    // Sync client-side cart with session
    void syncCart(HttpSession session, List<CartItemDto> items);

    // Convert cart to order items during checkout
    void validateCartForCheckout(HttpSession session);
}