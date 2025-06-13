package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vet_permissions")
@AttributeOverride(name = "id", column = @Column(name = "vet_permission_id"))
public class VetPermission extends BaseVO {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private Veterinarian veterinarian;

    @Column(name = "granted_by_user_id")
    private Long grantedByUserId;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Specific permissions for this vet on this account
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vet_permission_details",
            joinColumns = @JoinColumn(name = "vet_permission_id"))
    @Enumerated(EnumType.STRING)
    private Set<VetPermissionType> permissions = new HashSet<>();

    // Constructors
    public VetPermission() {}

    public VetPermission(Account account, Veterinarian veterinarian, Long grantedByUserId) {
        this.account = account;
        this.veterinarian = veterinarian;
        this.grantedByUserId = grantedByUserId;
        this.grantedAt = LocalDateTime.now();
        this.isActive = true;
    }

    public boolean hasPermission(VetPermissionType permissionType) {
        return isActive && permissions.contains(permissionType);
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // Getters and Setters
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Veterinarian getVeterinarian() { return veterinarian; }
    public void setVeterinarian(Veterinarian veterinarian) { this.veterinarian = veterinarian; }

    public Long getGrantedByUserId() { return grantedByUserId; }
    public void setGrantedByUserId(Long grantedByUserId) { this.grantedByUserId = grantedByUserId; }

    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Set<VetPermissionType> getPermissions() { return permissions; }
    public void setPermissions(Set<VetPermissionType> permissions) { this.permissions = permissions; }
}