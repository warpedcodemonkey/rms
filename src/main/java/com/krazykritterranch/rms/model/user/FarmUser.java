package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Farm User represents users associated with specific farm/ranch accounts.
 *
 * These users are tied to a specific farm account and have permissions
 * within that account's scope only.
 *
 * Farm User Types:
 * - MASTER_USER: Account owner with full permissions for their farm
 * - SUB_USER: Limited permissions based on assigned roles
 */
@Entity
@DiscriminatorValue("FARM")
public class FarmUser extends User {

    /**
     * Farm user access levels within their account
     */
    public enum FarmRole {
        MASTER_USER,    // Account owner/administrator
        SUB_USER        // Regular farm user with configurable permissions
    }

    @Column(name = "farm_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private FarmRole farmRole;

    @Column(name = "account_id", nullable = false)
    private Long accountId; // Reference to the farm/ranch account

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId; // Who invited this user to the account

    @Column(name = "can_manage_livestock")
    private Boolean canManageLivestock = true;

    @Column(name = "can_manage_feed")
    private Boolean canManageFeed = true;

    @Column(name = "can_manage_medical")
    private Boolean canManageMedical = false; // More sensitive, default false

    @Column(name = "can_manage_vendors")
    private Boolean canManageVendors = false;

    @Column(name = "can_manage_users")
    private Boolean canManageUsers = false; // Only master users by default

    @Column(name = "can_view_reports")
    private Boolean canViewReports = true;

    @Column(name = "can_export_data")
    private Boolean canExportData = false;

    // Constructors
    public FarmUser() {
        super();
        this.farmRole = FarmRole.SUB_USER; // Default to least privileged
    }

    public FarmUser(String email, String password, String firstName, String lastName,
                    Long accountId, FarmRole farmRole) {
        super(email, password, firstName, lastName);
        this.accountId = accountId;
        this.farmRole = farmRole;

        // Set default permissions based on role
        if (farmRole == FarmRole.MASTER_USER) {
            setMasterUserPermissions();
        }
    }

    @Override
    public String getUserType() {
        return "FARM";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Base farm user authority
        authorities.add(new SimpleGrantedAuthority("ROLE_FARM_USER"));

        // Role-based authorities
        if (farmRole == FarmRole.MASTER_USER) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MASTER_USER"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUB_USER"));
        }

        // Permission-based authorities for fine-grained access control
        if (canManageLivestock) {
            authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_LIVESTOCK"));
        }
        if (canManageFeed) {
            authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_FEED"));
        }
        if (canManageMedical) {
            authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_MEDICAL"));
        }
        if (canManageVendors) {
            authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_VENDORS"));
        }
        if (canManageUsers) {
            authorities.add(new SimpleGrantedAuthority("PERM_MANAGE_USERS"));
        }
        if (canViewReports) {
            authorities.add(new SimpleGrantedAuthority("PERM_VIEW_REPORTS"));
        }
        if (canExportData) {
            authorities.add(new SimpleGrantedAuthority("PERM_EXPORT_DATA"));
        }

        return authorities;
    }

    /**
     * Set permissions appropriate for a master user (account owner)
     */
    private void setMasterUserPermissions() {
        this.canManageLivestock = true;
        this.canManageFeed = true;
        this.canManageMedical = true;
        this.canManageVendors = true;
        this.canManageUsers = true;
        this.canViewReports = true;
        this.canExportData = true;
    }

    /**
     * Check if this user is the master user (account owner)
     */
    public boolean isMasterUser() {
        return farmRole == FarmRole.MASTER_USER;
    }

    /**
     * Check if this user can invite other users to the account
     */
    public boolean canInviteUsers() {
        return canManageUsers;
    }

    /**
     * Check if this user has any administrative permissions
     */
    public boolean hasAdminPermissions() {
        return canManageUsers || canManageVendors || canExportData;
    }

    /**
     * Promote user to master user status (typically when transferring ownership)
     */
    public void promoteToMasterUser() {
        this.farmRole = FarmRole.MASTER_USER;
        setMasterUserPermissions();
    }

    /**
     * Reset permissions to sub-user defaults
     */
    public void resetToSubUserPermissions() {
        this.farmRole = FarmRole.SUB_USER;
        this.canManageLivestock = true;
        this.canManageFeed = true;
        this.canManageMedical = false;
        this.canManageVendors = false;
        this.canManageUsers = false;
        this.canViewReports = true;
        this.canExportData = false;
    }

    // Getters and Setters
    public FarmRole getFarmRole() {
        return farmRole;
    }

    public void setFarmRole(FarmRole farmRole) {
        this.farmRole = farmRole;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getInvitedByUserId() {
        return invitedByUserId;
    }

    public void setInvitedByUserId(Long invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }

    public Boolean getCanManageLivestock() {
        return canManageLivestock;
    }

    public void setCanManageLivestock(Boolean canManageLivestock) {
        this.canManageLivestock = canManageLivestock;
    }

    public Boolean getCanManageFeed() {
        return canManageFeed;
    }

    public void setCanManageFeed(Boolean canManageFeed) {
        this.canManageFeed = canManageFeed;
    }

    public Boolean getCanManageMedical() {
        return canManageMedical;
    }

    public void setCanManageMedical(Boolean canManageMedical) {
        this.canManageMedical = canManageMedical;
    }

    public Boolean getCanManageVendors() {
        return canManageVendors;
    }

    public void setCanManageVendors(Boolean canManageVendors) {
        this.canManageVendors = canManageVendors;
    }

    public Boolean getCanManageUsers() {
        return canManageUsers;
    }

    public void setCanManageUsers(Boolean canManageUsers) {
        this.canManageUsers = canManageUsers;
    }

    public Boolean getCanViewReports() {
        return canViewReports;
    }

    public void setCanViewReports(Boolean canViewReports) {
        this.canViewReports = canViewReports;
    }

    public Boolean getCanExportData() {
        return canExportData;
    }

    public void setCanExportData(Boolean canExportData) {
        this.canExportData = canExportData;
    }

    @Override
    public String toString() {
        return "FarmUser{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", farmRole=" + farmRole +
                ", accountId=" + accountId +
                ", isActive=" + getIsActive() +
                '}';
    }
}