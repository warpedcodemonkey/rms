package com.krazykritterranch.rms.service.security;

import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.VetPermission;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import com.krazykritterranch.rms.repositories.user.VetPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private VetPermissionRepository vetPermissionRepository;

    public boolean canAccessAccount(Long accountId) {
        // Admin can access any account
        if (tenantContext.isAdmin()) {
            return true;
        }

        // Account users can only access their own account
        if (tenantContext.isAccountUser()) {
            return accountId.equals(tenantContext.getCurrentAccountId());
        }

        // Veterinarians can access accounts they have permission for
        if (tenantContext.isVeterinarian()) {
            return hasVetPermissionForAccount(tenantContext.getCurrentUserId(), accountId);
        }

        return false;
    }

    public boolean canAccessLivestock(Long livestockId) {
        // This would need to check the livestock's account
        // Implementation depends on how you want to structure this
        return true; // Placeholder
    }

    public boolean hasVetPermission(Long accountId, VetPermissionType permissionType) {
        if (!tenantContext.isVeterinarian()) {
            return false;
        }

        Optional<VetPermission> permission = vetPermissionRepository
                .findActivePermissionByVetAndAccount(tenantContext.getCurrentUserId(), accountId);

        return permission.isPresent() && permission.get().hasPermission(permissionType);
    }

    private boolean hasVetPermissionForAccount(Long vetId, Long accountId) {
        return vetPermissionRepository
                .findActivePermissionByVetAndAccount(vetId, accountId)
                .isPresent();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}