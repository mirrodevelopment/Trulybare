package com.trulybare.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String redirectUrl = "/login?error=true";
        
        if (request.getRequestURI().contains("secure-admin-login-process")) {
            redirectUrl = "/secure-admin-login?error=true";
        }

        if (exception instanceof DisabledException) {
            if (request.getRequestURI().contains("secure-admin-login-process")) {
                redirectUrl = "/secure-admin-login?error=suspended";
            } else {
                redirectUrl = "/login?error=suspended";
            }
        }
        
        setDefaultFailureUrl(redirectUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}
