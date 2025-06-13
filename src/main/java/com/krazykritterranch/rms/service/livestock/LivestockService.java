package com.krazykritterranch.rms.service.livestock;

import com.krazykritterranch.rms.model.livestock.Livestock;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import com.krazykritterranch.rms.repositories.livestock.LivestockRepository;
import com.krazykritterranch.rms.service.security.SecurityAnnotations;
import com.krazykritterranch.rms.service.security.SecurityService;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LivestockService {

    @Autowired
    private LivestockRepository livestockRepository;

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private SecurityService securityService;

    public List<Livestock> getAllLivestock() {
        if (tenantContext.isAdmin()) {
            return livestockRepository.findAll();
        } else if (tenantContext.isAccountUser()) {
            return livestockRepository.findByAccountId(tenantContext.getCurrentAccountId());
        } else if (tenantContext.isVeterinarian()) {
            return livestockRepository.findByVeterinarianAccess(tenantContext.getCurrentUserId());
        }
        throw new SecurityException("Access denied");
    }

    public Optional<Livestock> findById(Long id) {
        Optional<Livestock> livestock = livestockRepository.findById(id);
        if (livestock.isEmpty()) {
            return Optional.empty();
        }

        // Security check
        if (!canAccessLivestock(livestock.get())) {
            throw new SecurityException("Access denied");
        }

        return livestock;
    }

    public Livestock saveLivestock(Livestock livestock) {
        // Security check for editing
        if (livestock.getId() != null && !canEditLivestock(livestock)) {
            throw new SecurityException("Edit access denied");
        }

        // Set account for new livestock
        if (livestock.getId() == null && tenantContext.isAccountUser()) {
            // The account should be set by the controller or through the livestock entity
            // livestock.setAccount(accountRepository.findById(tenantContext.getCurrentAccountId()).orElse(null));
        }

        return livestockRepository.save(livestock);
    }

    private boolean canAccessLivestock(Livestock livestock) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        if (tenantContext.isAccountUser()) {
            return livestock.getAccount() != null &&
                    livestock.getAccount().getId().equals(tenantContext.getCurrentAccountId());
        }

        if (tenantContext.isVeterinarian()) {
            return livestock.getAccount() != null &&
                    securityService.hasVetPermission(livestock.getAccount().getId(), VetPermissionType.VIEW_LIVESTOCK);
        }

        return false;
    }

    private boolean canEditLivestock(Livestock livestock) {
        if (tenantContext.isAdmin() || tenantContext.isAccountUser()) {
            return canAccessLivestock(livestock);
        }

        if (tenantContext.isVeterinarian()) {
            return livestock.getAccount() != null &&
                    securityService.hasVetPermission(livestock.getAccount().getId(), VetPermissionType.EDIT_LIVESTOCK);
        }

        return false;
    }
}