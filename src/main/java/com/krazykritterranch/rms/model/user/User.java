// =======================
// UPDATED BASE USER ENTITY
// =======================

package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.common.Account;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // For Customers, this should be their email

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Account association - NULL for system users, REQUIRED for account users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_account_id", nullable = true)
    private Account primaryAccount;

    // Role-based security
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Custom permissions (in addition to role permissions)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_custom_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> customPermissions = new HashSet<>();

    // Abstract methods
    public abstract UserLevel getUserLevel();
    public abstract String getUserTypeString();

    // Validation and security methods
    public boolean requiresAccount() {
        return getUserLevel().requiresAccount();
    }

    public boolean isSystemUser() {
        return getUserLevel().isSystemUser();
    }

    public boolean isAccountUser() {
        return getUserLevel() == UserLevel.CUSTOMER || getUserLevel() == UserLevel.ACCOUNT_USER;
    }

    public boolean belongsToAccount(Long accountId) {
        return primaryAccount != null && primaryAccount.getId().equals(accountId);
    }

    public boolean canManageUser(User otherUser) {
        return this.getUserLevel().canManage(otherUser.getUserLevel());
    }

    // Account validation
    public void validateAccountAssociation() {
        if (requiresAccount() && primaryAccount == null) {
            throw new IllegalStateException(getUserLevel().getDisplayName() + " must be associated with an account");
        }
        if (isSystemUser() && primaryAccount != null) {
            throw new IllegalStateException(getUserLevel().getDisplayName() + " cannot be associated with an account");
        }
    }

    @PrePersist
    @PreUpdate
    protected void validate() {
        validateAccountAssociation();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role-based authorities
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority("PERM_" + permission.getName()));
            }
        }

        // Add custom permissions
        for (Permission permission : customPermissions) {
            authorities.add(new SimpleGrantedAuthority("PERM_" + permission.getName()));
        }

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return isActive; }

    // Standard getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Account getPrimaryAccount() { return primaryAccount; }
    public void setPrimaryAccount(Account primaryAccount) { this.primaryAccount = primaryAccount; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Set<Permission> getCustomPermissions() { return customPermissions; }
    public void setCustomPermissions(Set<Permission> customPermissions) { this.customPermissions = customPermissions; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}