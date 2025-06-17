package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Farm User represents users associated with specific farm/ranch accounts.
 *
 * These users are tied to a specific farm account and have permissions
 * managed through separate Role and Permission entities.
 */
@Entity
@DiscriminatorValue("FARM")
public class FarmUser extends User {

    @Column(name = "account_id", nullable = false)
    private Long accountId; // Reference to the farm/ranch account

    @Column(name = "is_primary_user", nullable = false)
    private Boolean isPrimaryUser = false; // Designates the primary/root user for the account

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId; // Who invited this user to the account

    // Constructors
    public FarmUser() {
        super();
    }

    public FarmUser(String email, String password, String firstName, String lastName, Long accountId) {
        super(email, password, firstName, lastName);
        this.accountId = accountId;
    }

    public FarmUser(String email, String password, String firstName, String lastName, Long accountId, Boolean isPrimaryUser) {
        super(email, password, firstName, lastName);
        this.accountId = accountId;
        this.isPrimaryUser = isPrimaryUser;
    }

    @Override
    public String getUserType() {
        return "FARM";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Basic authority for farm users
        // Actual permissions will be managed through Role/Permission entities
        return List.of(new SimpleGrantedAuthority("ROLE_FARM_USER"));
    }

    // Getters and Setters
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Boolean getIsPrimaryUser() {
        return isPrimaryUser;
    }

    public void setIsPrimaryUser(Boolean isPrimaryUser) {
        this.isPrimaryUser = isPrimaryUser;
    }

    public Long getInvitedByUserId() {
        return invitedByUserId;
    }

    public void setInvitedByUserId(Long invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }

    @Override
    public String toString() {
        return "FarmUser{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", accountId=" + accountId +
                ", isPrimaryUser=" + isPrimaryUser +
                ", invitedByUserId=" + invitedByUserId +
                ", isActive=" + getIsActive() +
                '}';
    }
}