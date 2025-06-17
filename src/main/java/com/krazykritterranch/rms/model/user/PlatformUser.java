package com.krazykritterranch.rms.model.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Platform User represents system administrators and support staff.
 *
 * These users have platform-wide access and can manage multiple farm accounts.
 * They are not associated with any specific farm/ranch.
 *
 * Platform User Types:
 * - SUPER_ADMIN: Full system access, can create other platform users
 * - SUPPORT_ADMIN: Can view and manage customer accounts for support purposes
 */
@Entity
@DiscriminatorValue("PLATFORM")
public class PlatformUser extends User {

    /**
     * Platform user access levels
     */
    public enum PlatformRole {
        SUPER_ADMIN,    // Full system administration
        SUPPORT_ADMIN   // Customer support administration
    }

    private PlatformRole platformRole;

    // Constructors
    public PlatformUser() {
        super();
        this.platformRole = PlatformRole.SUPPORT_ADMIN; // Default to least privileged
    }

    public PlatformUser(String email, String password, String firstName, String lastName, PlatformRole platformRole) {
        super(email, password, firstName, lastName);
        this.platformRole = platformRole;
    }

    @Override
    public String getUserType() {
        return "PLATFORM";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (platformRole) {
            case SUPER_ADMIN -> List.of(
                    new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_SUPPORT_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_PLATFORM_USER")
            );
            case SUPPORT_ADMIN -> List.of(
                    new SimpleGrantedAuthority("ROLE_SUPPORT_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_PLATFORM_USER")
            );
        };
    }

    /**
     * Check if this platform user can create other platform users
     */
    public boolean canCreatePlatformUsers() {
        return platformRole == PlatformRole.SUPER_ADMIN;
    }

    /**
     * Check if this platform user can access all customer accounts
     */
    public boolean canAccessAllAccounts() {
        return platformRole == PlatformRole.SUPER_ADMIN || platformRole == PlatformRole.SUPPORT_ADMIN;
    }

    /**
     * Check if this platform user can modify subscription limits
     */
    public boolean canModifySubscriptions() {
        return platformRole == PlatformRole.SUPER_ADMIN;
    }

    /**
     * Check if this platform user can suspend/unsuspend accounts
     */
    public boolean canSuspendAccounts() {
        return platformRole == PlatformRole.SUPER_ADMIN;
    }

    // Getters and Setters
    public PlatformRole getPlatformRole() {
        return platformRole;
    }

    public void setPlatformRole(PlatformRole platformRole) {
        this.platformRole = platformRole;
    }

    @Override
    public String toString() {
        return "PlatformUser{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", platformRole=" + platformRole +
                ", isActive=" + getIsActive() +
                '}';
    }
}