package com.krazykritterranch.rms.service.security;

import com.krazykritterranch.rms.model.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantContextInterceptor implements HandlerInterceptor {

    @Autowired
    private TenantContext tenantContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Only initialize tenant context for API requests if not already set
        if (request.getRequestURI().startsWith("/api/") && tenantContext.getCurrentUserId() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof User) {

                User user = (User) authentication.getPrincipal();

                // Set tenant context
                tenantContext.setCurrentUserId(user.getId());
                tenantContext.setUserType(user.getUserType());

                if (user.getPrimaryAccount() != null) {
                    tenantContext.setCurrentAccountId(user.getPrimaryAccount().getId());
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clear tenant context after request completion for API requests
        if (request.getRequestURI().startsWith("/api/")) {
            tenantContext.clear();
        }
    }
}