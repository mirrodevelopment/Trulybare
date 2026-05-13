package com.trulybare.controller;

import com.trulybare.dto.CartDto;
import com.trulybare.dto.CartItemDto;
import com.trulybare.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Principal principal,
            HttpSession session,
            jakarta.servlet.http.HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        // Require login to add items to cart
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            cartService.addToCart(session, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Product added to cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        // Redirect back to the page the user came from (product page, home, etc.)
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank() && !referer.contains("/cart")) {
            return "redirect:" + referer;
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update/{productId}")
    public String updateCart(@PathVariable Long productId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.updateCartItem(session, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, productId);
        redirectAttributes.addFlashAttribute("success", "Product removed from cart!");
        return "redirect:/cart";
    }

    @GetMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clearCart(session);
        redirectAttributes.addFlashAttribute("success", "Cart cleared!");
        return "redirect:/cart";
    }

    @PostMapping("/cart/sync")
    @ResponseBody
    public ResponseEntity<Void> syncCart(@RequestBody List<CartItemDto> items, HttpSession session) {
        cartService.syncCart(session, items);
        return ResponseEntity.ok().build();
    }
}