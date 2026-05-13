package com.trulybare.config;

import com.trulybare.entity.User;
import com.trulybare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=========================================");
        System.out.println("🔍 Login attempt for: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        System.out.println("✅ User found: " + user.getEmail());
        System.out.println("✅ Full name: " + user.getFullName());
        System.out.println("✅ Is active: " + user.getIsActive());
        System.out.println("✅ Role: " + user.getRole());
        System.out.println("✅ Password hash length: " + user.getPassword().length());
        System.out.println("=========================================");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}