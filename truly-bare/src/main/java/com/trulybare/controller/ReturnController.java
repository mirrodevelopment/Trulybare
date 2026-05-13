package com.trulybare.controller;

import com.trulybare.entity.ReturnRequest;
import com.trulybare.entity.User;
import com.trulybare.enums.ReturnStatus;
import com.trulybare.repository.OrderRepository;
import com.trulybare.service.ReturnService;
import com.trulybare.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/return")
public class ReturnController {

    private final ReturnService returnService;
    private final UserService userService;
    private final OrderRepository orderRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ─────────────────────────────────────────────
    // RETURN FORM PAGE
    // ─────────────────────────────────────────────

    @GetMapping("/new/{orderId}")
    public String showReturnForm(@PathVariable Long orderId,
                                 Principal principal, Model model,
                                 RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        if (orderId == null) {
            ra.addFlashAttribute("errorMessage", "Invalid order ID.");
            return "redirect:/order-history";
        }
        var order = orderRepository.findById(orderId).orElse(null);

        if (order == null || !order.getUser().getUserId().equals(user.getUserId())) {
            ra.addFlashAttribute("errorMessage", "Order not found.");
            return "redirect:/order-history";
        }
        if (!order.isReturnable()) {
            ra.addFlashAttribute("errorMessage", "This order is not eligible for return.");
            return "redirect:/order-history";
        }
        if (returnService.hasReturnForOrder(orderId, user)) {
            ra.addFlashAttribute("errorMessage", "A return request already exists for this order.");
            return "redirect:/return/track";
        }

        model.addAttribute("order", order);
        model.addAttribute("user", user);
        model.addAttribute("reasons", List.of("Damaged", "Wrong Item", "Size Issue", "Quality Issue", "Other"));
        return "return-request";
    }

    // ─────────────────────────────────────────────
    // SUBMIT RETURN FORM
    // ─────────────────────────────────────────────

    @PostMapping("/submit")
    public String submitReturn(@RequestParam Long orderId,
                               @RequestParam String reason,
                               @RequestParam String returnType,
                               @RequestParam(required = false) String description,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               Principal principal,
                               RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            User user = userService.findByEmail(principal.getName());
            ReturnRequest rr = returnService.submitReturn(user, orderId, reason, returnType,
                    description, images, uploadDir);
            ra.addFlashAttribute("successMessage", "Return request submitted successfully!");
            return "redirect:/return/chat/" + rr.getId();
        } catch (Exception e) {
            log.error("Return submission failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Could not submit return: " + e.getMessage());
            return "redirect:/return/new/" + orderId;
        }
    }

    // ─────────────────────────────────────────────
    // CHAT PAGE
    // ─────────────────────────────────────────────

    @GetMapping("/chat/{returnId}")
    public String chatPage(@PathVariable Long returnId,
                           Principal principal, Model model,
                           RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        var rr = returnService.getById(returnId).orElse(null);

        if (rr == null || !rr.getUser().getUserId().equals(user.getUserId())) {
            ra.addFlashAttribute("errorMessage", "Return request not found.");
            return "redirect:/order-history";
        }

        model.addAttribute("returnRequest", rr);
        model.addAttribute("user", user);
        model.addAttribute("returnId", returnId);
        return "return-chat";
    }

    // ─────────────────────────────────────────────
    // SEND CHAT MESSAGE (User)
    // ─────────────────────────────────────────────

    @PostMapping("/chat/{returnId}/send")
    public String sendMessage(@PathVariable Long returnId,
                              @RequestParam(required = false) String message,
                              @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                              Principal principal,
                              RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            User user = userService.findByEmail(principal.getName());
            if ((message == null || message.isBlank()) && (attachment == null || attachment.isEmpty())) {
                ra.addFlashAttribute("errorMessage", "Please enter a message.");
                return "redirect:/return/chat/" + returnId;
            }
            returnService.sendUserMessage(returnId, user, message, attachment, uploadDir);
        } catch (Exception e) {
            log.error("Chat send failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Could not send message: " + e.getMessage());
        }
        return "redirect:/return/chat/" + returnId;
    }

    // ─────────────────────────────────────────────
    // MY RETURNS (TRACKING PAGE)
    // ─────────────────────────────────────────────

    @GetMapping("/track")
    public String myReturns(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        model.addAttribute("returns", returnService.getUserReturns(user));
        model.addAttribute("user", user);
        return "my-returns";
    }
}
