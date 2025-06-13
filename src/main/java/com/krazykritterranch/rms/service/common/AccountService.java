package com.krazykritterranch.rms.service.common;

import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.common.AccountStatus;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.VetPermission;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import com.krazykritterranch.rms.model.user.Veterinarian;
import com.krazykritterranch.rms.repositories.common.AccountRepository;
import com.krazykritterranch.rms.repositories.user.VetPermissionRepository;
import com.krazykritterranch.rms.service.security.SecurityAnnotations;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VetPermissionRepository vetPermissionRepository;

    @Autowired
    private TenantContext tenantContext;

    @SecurityAnnotations.RequireAccountAccess
    public Optional<Account> findById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    public List<Account> findAllForAdmin() {
        // Only admins can see all accounts
        if (!tenantContext.isAdmin()) {
            throw new SecurityException("Access denied");
        }
        return accountRepository.findAll();
    }

    public Account createAccount(Account account, User masterUser) {
        account.setMasterUser(masterUser);
        account.setSignupDate(new java.sql.Date(System.currentTimeMillis()));
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);

        // Set the master user's primary account
        masterUser.setPrimaryAccount(savedAccount);

        return savedAccount;
    }

    @SecurityAnnotations.RequireAccountAccess
    public boolean canAddUser(Long accountId) {
        Optional<Account> account = accountRepository.findById(accountId);
        return account.isPresent() && account.get().canAddUser();
    }

    @SecurityAnnotations.RequireAccountAccess
    public VetPermission grantVetAccess(Long accountId, Veterinarian veterinarian,
                                        Set<VetPermissionType> permissions,
                                        LocalDateTime expiresAt) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Check if permission already exists
        Optional<VetPermission> existing = vetPermissionRepository
                .findActivePermissionByVetAndAccount(veterinarian.getId(), accountId);

        VetPermission vetPermission;
        if (existing.isPresent()) {
            // Update existing permission
            vetPermission = existing.get();
            vetPermission.setPermissions(permissions);
            vetPermission.setExpiresAt(expiresAt);
        } else {
            // Create new permission
            vetPermission = new VetPermission(account, veterinarian, tenantContext.getCurrentUserId());
            vetPermission.setPermissions(permissions);
            vetPermission.setExpiresAt(expiresAt);
        }

        return vetPermissionRepository.save(vetPermission);
    }

    @SecurityAnnotations.RequireAccountAccess
    public void revokeVetAccess(Long accountId, Long veterinarianId) {
        Optional<VetPermission> permission = vetPermissionRepository
                .findActivePermissionByVetAndAccount(veterinarianId, accountId);

        if (permission.isPresent()) {
            permission.get().setIsActive(false);
            vetPermissionRepository.save(permission.get());
        }
    }

    @SecurityAnnotations.RequireAccountAccess
    public List<VetPermission> getVetPermissions(Long accountId) {
        return vetPermissionRepository.findPermissionsByAccount(accountId);
    }

    public List<Account> getAccountsForVet(Long veterinarianId) {
        List<VetPermission> permissions = vetPermissionRepository
                .findActivePermissionsByVet(veterinarianId, LocalDateTime.now());

        return permissions.stream()
                .map(VetPermission::getAccount)
                .toList();
    }

    @SecurityAnnotations.RequireAdmin
    public void suspendAccount(Long accountId, String reason) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setStatus(AccountStatus.SUSPENDED);
        accountRepository.save(account);
    }

    @SecurityAnnotations.RequireAdmin
    public void reactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    // Cleanup expired permissions (scheduled job)
    public void cleanupExpiredPermissions() {
        List<VetPermission> expiredPermissions = vetPermissionRepository
                .findExpiredPermissions(LocalDateTime.now());

        expiredPermissions.forEach(permission -> {
            permission.setIsActive(false);
            vetPermissionRepository.save(permission);
        });
    }
}