package com.krazykritterranch.rms.service.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class TenantContext {

    private Long currentAccountId;
    private Long currentUserId;
    private String userType;

    public void setCurrentAccountId(Long accountId) {
        this.currentAccountId = accountId;
    }

    public Long getCurrentAccountId() {
        return currentAccountId;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isAdmin() {
        // Fixed to match the actual role name used in your system
        return "ADMINISTRATOR".equals(userType) || "SUPER_ADMIN".equals(userType);
    }

    public boolean isVeterinarian() {
        return "VETERINARIAN".equals(userType);
    }

    public boolean isAccountUser() {
        return currentAccountId != null && !isAdmin() && !isVeterinarian();
    }

    public void clear() {
        this.currentAccountId = null;
        this.currentUserId = null;
        this.userType = null;
    }
}