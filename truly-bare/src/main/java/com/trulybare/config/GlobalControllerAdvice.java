package com.trulybare.config;

import com.trulybare.entity.User;
import com.trulybare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.trulybare.service.AdminService adminService;

    @ModelAttribute
    public void addAttributes(Model model) {
        // Load announcement text (pipe-separated for multiple announcements)
        String raw = adminService.getSetting("announcement_text", "Complimentary shipping on all orders over ₹2,500 · New: The Reverence Collection");
        model.addAttribute("announcementText", raw);

        // Split into a list for the rotating ticker
        java.util.List<String> announcements = new java.util.ArrayList<>();
        if (raw != null && !raw.isBlank()) {
            for (String part : raw.split("\\|")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) announcements.add(trimmed);
            }
        }
        if (announcements.isEmpty()) announcements.add(raw != null ? raw : "");
        model.addAttribute("announcements", announcements);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // If user is suspended, flag it for the UI modal
                if (user.getIsActive() != null && !user.getIsActive()) {
                    model.addAttribute("isAccountSuspended", true);
                }
                
                // Add common user attributes
                model.addAttribute("currentUser", user);
                model.addAttribute("userFullName", user.getFullName());
            }
        }
    }
}
