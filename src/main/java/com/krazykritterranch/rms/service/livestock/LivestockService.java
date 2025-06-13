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
            // Set the account ID (you'll need to add this field to Livestock entity)
            // livestock.setAccountId(tenantContext.getCurrentAccountId());
        }

        return livestockRepository.save(livestock);
    }

    private boolean canAccessLivestock(Livestock livestock) {
        if (tenantContext.isAdmin()) {
            return true;
        }

        // You'll need to add account relationship to Livestock entity
        // For now, simplified logic
        return true;
    }

    private boolean canEditLivestock(Livestock livestock) {
        if (tenantContext.isAdmin() || tenantContext.isAccountUser()) {
            return canAccessLivestock(livestock);
        }

        if (tenantContext.isVeterinarian()) {
            // Check if vet has edit permission
            // Long accountId = livestock.getAccountId(); // You'll need to add this
            // return securityService.hasVetPermission(accountId, VetPermissionType.EDIT_LIVESTOCK);
        }

        return false;
    }
}