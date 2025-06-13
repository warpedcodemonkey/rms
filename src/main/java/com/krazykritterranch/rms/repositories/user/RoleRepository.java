package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.controller.common.AccountController;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.user.Permission;
import com.krazykritterranch.rms.model.user.VetPermission;
import com.krazykritterranch.rms.service.common.AccountService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {



    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findByIsSystemPermissionTrue();

    List<Permission> findByIsSystemPermissionFalse();

    List<Permission> findByCategory(String category);

    @Query("SELECT p FROM Permission p WHERE p.name LIKE %:name% OR p.description LIKE %:description%")
    List<Permission> findByNameOrDescriptionContaining(@Param("name") String name, @Param("description") String description);
}hasRole('ADMIN')")
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
        @RequestBody AccountController.VetAccessRequest request) {

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


}