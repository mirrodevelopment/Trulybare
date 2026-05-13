package com.trulybare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import org.springframework.core.annotation.Order;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomAuthFailureHandler authFailureHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/secure-admin-login", "/secure-admin-login-process", "/admin/logout")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/secure-admin-login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/secure-admin-login")
                .loginProcessingUrl("/secure-admin-login-process")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureHandler(authFailureHandler)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new RegexRequestMatcher("/admin/logout", "GET"))
                .logoutSuccessUrl("/secure-admin-login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"));
            
        return http.build();
    }

    /**
     * Success handler that redirects users back to the page they were trying to
     * access before being prompted to log in. Falls back to /home.
     */
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/home");
        handler.setAlwaysUseDefaultTargetUrl(false);
        return handler;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ── Static assets & fully public pages ──────────────────────────
                .requestMatchers("/", "/home", "/css/**", "/js/**",
                        "/images/**", "/assets/**", "/error", "/legal",
                        "/house-of-vegam").permitAll()
                // ── Auth pages ───────────────────────────────────────────────────
                .requestMatchers("/register", "/login", "/login-process").permitAll()
                // ── Product browsing — public ────────────────────────────────────
                .requestMatchers("/products", "/product/**", "/products/**").permitAll()
                // ── Cart: view page is public; mutations require login ────────────
                .requestMatchers("/cart").permitAll()
                .requestMatchers("/cart/sync").permitAll()
                // ── Public APIs & uploads ────────────────────────────────────────
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // ── Everything else (checkout, review, profile, orders…) needs login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login-process")
                // Redirects back to the originally requested URL after login
                .successHandler(savedRequestSuccessHandler())
                .failureHandler(authFailureHandler)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new RegexRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}