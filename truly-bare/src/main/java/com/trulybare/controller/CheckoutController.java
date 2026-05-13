package com.trulybare.controller;

import com.trulybare.dto.CartDto;
import com.trulybare.entity.Order;
import com.trulybare.entity.User;
import com.trulybare.service.CartService;
import com.trulybare.service.OrderService;
import com.trulybare.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;
import com.razorpay.Utils;
import org.json.JSONObject;

import java.security.Principal;

@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/checkout")
    public String showCheckout(Principal principal, HttpSession session, Model model) {
        if (principal == null) {
            return "redirect:/login?next=/checkout";
        }

        CartDto cart = cartService.getCart(session);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("cart", cart);
        model.addAttribute("addresses", user.getAddresses());

        return "checkout";
    }

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam(required = false) Long addressId,
                           @RequestParam(required = false) String paymentMethod,
                           @RequestParam(required = false, defaultValue = "0") Integer pointsToUse,
                           @RequestParam(required = false) String customAddress,
                           @RequestParam(required = false) String razorpayPaymentId,
                           @RequestParam(required = false) String razorpayOrderId,
                           @RequestParam(required = false) String razorpaySignature,
                           Principal principal,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        // Require either a saved address or a filled custom address
        boolean usingCustom = (addressId != null && addressId == -1L);
        if (!usingCustom && addressId == null) {
            redirectAttributes.addFlashAttribute("error", "Please select a shipping address.");
            return "redirect:/checkout";
        }
        if (usingCustom && (customAddress == null || customAddress.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "Please fill in the delivery address.");
            return "redirect:/checkout";
        }

        if ("ONLINE".equals(paymentMethod)) {
            if (razorpayPaymentId == null || razorpayOrderId == null || razorpaySignature == null) {
                redirectAttributes.addFlashAttribute("error", "Payment details missing.");
                return "redirect:/checkout";
            }
            try {
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", razorpayOrderId);
                options.put("razorpay_payment_id", razorpayPaymentId);
                options.put("razorpay_signature", razorpaySignature);
                boolean isValid = Utils.verifyPaymentSignature(options, razorpaySecret);
                if (!isValid) {
                    redirectAttributes.addFlashAttribute("error", "Payment verification failed.");
                    return "redirect:/checkout";
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Payment verification error: " + e.getMessage());
                return "redirect:/checkout";
            }
        }

        try {
            User user = userService.findByEmail(principal.getName());
            Order order = orderService.placeOrder(session, user, usingCustom ? null : addressId,
                    usingCustom ? customAddress : null, paymentMethod, pointsToUse, 
                    razorpayPaymentId, razorpayOrderId, razorpaySignature);
            cartService.clearCart(session);
            return "redirect:/checkout/success/" + order.getOrderId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/checkout/success/{orderId}")
    public String orderSuccess(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "order-success";
    }
}
