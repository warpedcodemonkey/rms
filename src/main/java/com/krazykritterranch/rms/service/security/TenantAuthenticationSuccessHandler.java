package com.krazykritterranch.rms.service.security;

import com.krazykritterranch.rms.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class TenantAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private TenantContext tenantContext;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();

            // Set tenant context
            tenantContext.setCurrentUserId(user.getId());
            tenantContext.setUserType(user.getUserType());

            if (user.getPrimaryAccount() != null) {
                tenantContext.setCurrentAccountId(user.getPrimaryAccount().getId());
            }

            // Update last login
            user.setLastLogin(LocalDateTime.now());

            // Redirect based on user type
            String redirectUrl = determineTargetUrl(user);
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("/");
        }
    }

    private String determineTargetUrl(User user) {
        if (user.isAdministrator()) {
            return "/admin/dashboard";
        } else if (user.isVeterinarian()) {
            return "/vet/dashboard";
        } else {
            return "/dashboard";
        }
    }
}