package com.trulybare.service.impl;

import com.trulybare.dto.CartDto;
import com.trulybare.dto.CartItemDto;
import com.trulybare.entity.Product;
import com.trulybare.service.CartService;
import com.trulybare.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_SESSION_KEY = "cart";

    @Autowired
    private ProductService productService;

    @Override
    public CartDto getCart(HttpSession session) {
        Map<Long, Integer> cartMap = getCartMap(session);

        CartDto cartDto = new CartDto();

        cartDto.setItems(
                cartMap.entrySet().stream()
                        .map(entry -> {
                            Product product = productService.getProductById(entry.getKey());
                            CartItemDto item = new CartItemDto();
                            item.setProductId(product.getProductId());
                            item.setProductName(product.getName());
                            item.setProductImage(product.getImageUrl());
                            item.setPrice(product.getPrice());
                            item.setQuantity(entry.getValue());
                            item.setMaxStock(product.getStockQuantity());
                            return item;
                        })
                        .collect(Collectors.toList())
        );

        cartDto.calculateTotals();
        return cartDto;
    }

    @Override
    public void addToCart(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getCartMap(session);

        // Validate product exists and has stock
        Product product = productService.getProductById(productId);
        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not available");
        }

        int currentQty = cart.getOrDefault(productId, 0);
        int newQty = currentQty + quantity;

        if (newQty > product.getStockQuantity()) {
            throw new RuntimeException("Only " + product.getStockQuantity() + " items available");
        }

        cart.put(productId, newQty);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @Override
    public void updateCartItem(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getCartMap(session);

        if (!cart.containsKey(productId)) {
            throw new RuntimeException("Product not in cart");
        }

        Product product = productService.getProductById(productId);

        if (quantity <= 0) {
            cart.remove(productId);
        } else if (quantity > product.getStockQuantity()) {
            throw new RuntimeException("Only " + product.getStockQuantity() + " items available");
        } else {
            cart.put(productId, quantity);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @Override
    public void removeFromCart(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCartMap(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @Override
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    @Override
    public int getCartItemCount(HttpSession session) {
        Map<Long, Integer> cart = getCartMap(session);
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public BigDecimal calculateSubtotal(HttpSession session) {
        Map<Long, Integer> cart = getCartMap(session);

        return cart.entrySet().stream()
                .map(entry -> {
                    Product product = productService.getProductById(entry.getKey());
                    return product.getPrice().multiply(new BigDecimal(entry.getValue()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateTotal(HttpSession session) {
        BigDecimal subtotal = calculateSubtotal(session);
        BigDecimal shipping = subtotal.compareTo(new BigDecimal("500")) > 0
                ? BigDecimal.ZERO
                : new BigDecimal("50");
        return subtotal.add(shipping);
    }

    @Override
    public boolean isProductInCart(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCartMap(session);
        return cart.containsKey(productId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, Integer> getCartMap(HttpSession session) {
        Object cartObj = session.getAttribute(CART_SESSION_KEY);
        if (cartObj == null) {
            return new HashMap<>();
        }
        return (Map<Long, Integer>) cartObj;
    }

    @Override
    public void syncCart(HttpSession session, java.util.List<CartItemDto> items) {
        Map<Long, Integer> cart = new HashMap<>();
        if (items != null) {
            for (CartItemDto item : items) {
                if (item.getProductId() != null && item.getQuantity() > 0) {
                    cart.put(item.getProductId(), item.getQuantity());
                }
            }
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @Override
    public void validateCartForCheckout(HttpSession session) {
        Map<Long, Integer> cart = getCartMap(session);

        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productService.getProductById(entry.getKey());

            if (!product.getIsActive()) {
                throw new RuntimeException(product.getName() + " is no longer available");
            }

            if (entry.getValue() > product.getStockQuantity()) {
                throw new RuntimeException("Only " + product.getStockQuantity() + " units of " + product.getName() + " available");
            }
        }
    }
}