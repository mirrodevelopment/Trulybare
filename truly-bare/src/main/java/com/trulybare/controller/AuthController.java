package com.trulybare.controller;

import com.trulybare.dto.LoginRequest;
import com.trulybare.dto.RegisterRequest;
import com.trulybare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Display login page
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "next", required = false) String next,
                        Model model) {
        if ("true".equals(error)) {
            model.addAttribute("error", "Invalid email or password!");
        } else if ("suspended".equals(error)) {
            model.addAttribute("error", "Your account has been suspended. Please contact support at support@trulybare.com for assistance.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (next != null) {
            model.addAttribute("next", next);
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    // Display registration page
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    // Process registration
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {

        System.out.println("========================================");
        System.out.println("📝 Registration attempt: " + request.getEmail());
        System.out.println("📝 Full name: " + request.getFullName());
        System.out.println("📝 Phone: " + request.getPhone());
        System.out.println("📝 Password match: " + request.isPasswordMatching());
        System.out.println("📝 Terms accepted: " + request.getTermsAccepted());

        // Check validation errors
        if (result.hasErrors()) {
            System.out.println("❌ VALIDATION ERRORS:");
            result.getAllErrors().forEach(error -> {
                System.out.println("  - " + error.getDefaultMessage());
            });
            System.out.println("========================================");
            return "register";
        }

        // Check password match
        if (!request.isPasswordMatching()) {
            System.out.println("❌ Passwords do not match!");
            result.rejectValue("confirmPassword", "error.registerRequest", "Passwords do not match!");
            return "register";
        }

        // Check terms accepted
        if (request.getTermsAccepted() == null || !request.getTermsAccepted()) {
            System.out.println("❌ Terms not accepted!");
            result.rejectValue("termsAccepted", "error.registerRequest", "You must accept the terms and conditions!");
            return "register";
        }

        try {
            System.out.println("✅ Validation passed! Attempting to save user...");

            // Register user
            userService.registerUser(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword()
            );

            // Auto-login after registration
            autoLogin(request.getEmail(), request.getPassword());

            System.out.println("✅ User saved and auto-logged in successfully!");

            redirectAttributes.addFlashAttribute("success", "Registration successful! Welcome to Truly Bare.");
            return "redirect:/";

        } catch (RuntimeException e) {
            System.out.println("❌ ERROR saving user: " + e.getMessage());
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg != null && msg.toLowerCase().contains("phone")) {
                result.rejectValue("phone", "error.registerRequest", msg);
            } else if (msg != null) {
                result.rejectValue("email", "error.registerRequest", msg);
            } else {
                result.rejectValue("email", "error.registerRequest", "An unexpected error occurred.");
            }
            return "register";
        } finally {
            System.out.println("========================================");
        }
    }

    // Logout (Spring Security handles this)
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }

    // Hidden admin login page — NOT linked anywhere in public UI
    @GetMapping("/secure-admin-login")
    public String adminLoginPage(@RequestParam(value = "error", required = false) String error,
                                 Model model) {
        if ("true".equals(error)) {
            model.addAttribute("error", "Access denied. Check your credentials.");
        } else if ("suspended".equals(error)) {
            model.addAttribute("error", "This admin account is suspended. Contact system administrator.");
        }
        return "admin/admin-login";
    }

    // 403 Forbidden
    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }

    // Auto-login after registration
    private void autoLogin(String email, String password) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}