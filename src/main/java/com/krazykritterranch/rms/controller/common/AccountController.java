package com.krazykritterranch.rms.controller.common;

import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.user.VetPermission;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import com.krazykritterranch.rms.model.user.Veterinarian;
import com.krazykritterranch.rms.service.common.AccountService;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.findAllForAdmin());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canAccessAccount(#id)")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return accountService.findById(id)
                .map(account -> ResponseEntity.ok(account))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-account")
    @PreAuthorize("hasRole('ACCOUNT_USER')")
    public ResponseEntity<Account> getMyAccount() {
        Long accountId = tenantContext.getCurrentAccountId();
        if (accountId == null) {
            return ResponseEntity.badRequest().build();
        }

        return accountService.findById(accountId)
                .map(account -> ResponseEntity.ok(account))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account savedAccount = accountService.createAccount(account, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canAccessAccount(#id)")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account account) {
        return accountService.findById(id)
                .map(existingAccount -> {
                    account.setId(existingAccount.getId());
                    // Preserve certain fields that shouldn't be updated via API
                    account.setAccountNumber(existingAccount.getAccountNumber());
                    account.setSignupDate(existingAccount.getSignupDate());
                    Account updatedAccount = accountService.save(account);
                    return ResponseEntity.ok(updatedAccount);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Vet Permission Management
    @PostMapping("/{accountId}/vet-permissions")
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public ResponseEntity<VetPermission> grantVetAccess(
            @PathVariable Long accountId,
            @RequestBody VetAccessRequest request) {

        VetPermission permission = accountService.grantVetAccess(
                accountId,
                request.getVeterinarian(),
                request.getPermissions(),
                request.getExpiresAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @DeleteMapping("/{accountId}/vet-permissions/{vetId}")
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public ResponseEntity<Void> revokeVetAccess(
            @PathVariable Long accountId,
            @PathVariable Long vetId) {

        accountService.revokeVetAccess(accountId, vetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountId}/vet-permissions")
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public ResponseEntity<List<VetPermission>> getVetPermissions(@PathVariable Long accountId) {
        List<VetPermission> permissions = accountService.getVetPermissions(accountId);
        return ResponseEntity.ok(permissions);
    }

    // Admin-only endpoints
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendAccount(@PathVariable Long id, @RequestParam String reason) {
        accountService.suspendAccount(id, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivateAccount(@PathVariable Long id) {
        accountService.reactivateAccount(id);
        return ResponseEntity.ok().build();
    }

    // Vet dashboard endpoint
    @GetMapping("/vet-accessible")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<List<Account>> getVetAccessibleAccounts() {
        List<Account> accounts = accountService.getAccountsForVet(tenantContext.getCurrentUserId());
        return ResponseEntity.ok(accounts);
    }

    // Inner class for vet access requests
    public static class VetAccessRequest {
        private Veterinarian veterinarian;
        private Set<VetPermissionType> permissions;
        private LocalDateTime expiresAt;

        // Getters and setters
        public Veterinarian getVeterinarian() { return veterinarian; }
        public void setVeterinarian(Veterinarian veterinarian) { this.veterinarian = veterinarian; }

        public Set<VetPermissionType> getPermissions() { return permissions; }
        public void setPermissions(Set<VetPermissionType> permissions) { this.permissions = permissions; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }
}