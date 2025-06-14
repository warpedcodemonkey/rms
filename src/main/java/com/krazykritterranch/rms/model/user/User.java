package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.Email;
import com.krazykritterranch.rms.model.common.Phone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Base User entity for the Ranch Management System.
 *
 * Implements Spring Security UserDetails for authentication.
 * Uses single-table inheritance with discriminator column for user types.
 * Supports soft delete (never hard delete users to preserve data integrity).
 * Email address is always the username for all user types.
 *
 * User Hierarchy:
 * 1. SuperAdministrator - Platform owners (system-level)
 * 2. SupportAdministrator - Customer service (system-level)
 * 3. AccountUser - Account-level users with role-based permissions
 * 4. Veterinarian - External professionals with cross-account access
 *
 * Account Administration:
 * - AccountUser with isPrimaryAccountUser=true = account owner/administrator
 * - Can create up to 4 additional AccountUsers (5 total per account)
 * - Can assign roles and permissions to other account users
 * - Can grant veterinarian access to account
 *
 * Multi-Contact Support:
 * - Multiple email addresses
 * - Multiple phone numbers
 * - Multiple addresses
 *
 * Security Features:
 * - Role-based access control (RBAC)
 * - Custom permissions in addition to role permissions
 * - Account-based multi-tenancy
 * - Comprehensive audit trail
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // Authentication & Identity (email is always the username)
    @Column(unique = true, nullable = false, length = 255)
    @NotBlank(message = "Username is required")
    private String username; // Always equals email address

    @Column(unique = true, nullable = false, length = 255)
    @NotBlank(message = "Email is required")
    private String email; // Primary authentication identifier

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    private String password; // Encrypted password hash

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // Professional Information
    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    @Column(name = "timezone", length = 50)
    private String timezone = "America/New_York";

    // Emergency Contact Information
    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;

    // Notification Preferences
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "sms_notifications")
    private Boolean smsNotifications = false;

    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    // Account Association (NULL for system users, REQUIRED for account users)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_account_id")
    private Account primaryAccount;

    // Account Administration Flag
    @Column(name = "is_primary_account_user")
    private Boolean isPrimaryAccountUser = false;

    // Role-Based Security
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Custom Permissions (in addition to role permissions)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_custom_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> customPermissions = new HashSet<>();

    // Multi-Contact Support
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_emails",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "email_id")
    )
    private List<Email> additionalEmails = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_phones",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "phone_id")
    )
    private List<Phone> phoneNumbers = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_addresses",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "address_id")
    )
    private List<Address> addresses = new ArrayList<>();

    // Soft Delete Implementation (NEVER hard delete users)
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "end_date")
    private LocalDateTime endDate; // Soft delete timestamp

    @Column(name = "end_reason", length = 500)
    private String endReason; // Reason for deactivation

    @Column(name = "ended_by_user_id")
    private Long endedByUserId; // Who performed the soft delete

    @Column(name = "reactivated_date")
    private LocalDateTime reactivatedDate;

    @Column(name = "reactivated_by_user_id")
    private Long reactivatedByUserId;

    // Audit Fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    // Abstract Methods (implemented by concrete user types)
    public abstract UserLevel getUserLevel();

    /**
     * Get the discriminator value for this user type.
     * Used by JPA for single-table inheritance.
     */
    public abstract String getUserType();

    // Constructors
    protected User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected User(String email, String password, String firstName, String lastName) {
        this();
        this.email = email;
        this.username = email; // Username always equals email
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Business Logic Methods

    /**
     * Validates account association based on user level requirements.
     * Called automatically on persist/update.
     */
    public void validateAccountAssociation() {
        UserLevel level = getUserLevel();
        boolean hasAccount = (primaryAccount != null);

        if (!level.validateAccountAssignment(hasAccount)) {
            if (level.requiresAccount() && !hasAccount) {
                throw new IllegalStateException(
                        level.getDisplayName() + " must be associated with an account");
            }
            if (level.isSystemUser() && hasAccount) {
                throw new IllegalStateException(
                        level.getDisplayName() + " cannot be associated with an account");
            }
        }
    }

    /**
     * Determines if this user belongs to the specified account.
     */
    public boolean belongsToAccount(Long accountId) {
        return primaryAccount != null && primaryAccount.getId().equals(accountId);
    }

    /**
     * Determines if this user can manage another user based on user levels and roles.
     */
    public boolean canManageUser(User otherUser) {
        // System users can manage based on hierarchy
        if (this.getUserLevel().isSystemUser()) {
            return this.getUserLevel().canManage(otherUser.getUserLevel());
        }

        // Account admins can manage other users in their account
        if (this.isAccountAdmin() && otherUser.isAccountUser()) {
            return this.belongsToAccount(otherUser.getPrimaryAccount().getId());
        }

        return false;
    }

    /**
     * Soft delete this user (never hard delete to preserve data integrity).
     */
    public void softDelete(Long deletedByUserId, String reason) {
        this.endDate = LocalDateTime.now();
        this.endReason = reason;
        this.endedByUserId = deletedByUserId;
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
        this.updatedByUserId = deletedByUserId;
    }

    /**
     * Reactivate a soft-deleted user.
     */
    public void reactivate(Long reactivatedByUserId) {
        this.reactivatedDate = LocalDateTime.now();
        this.reactivatedByUserId = reactivatedByUserId;
        this.endDate = null;
        this.endReason = null;
        this.endedByUserId = null;
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
        this.updatedByUserId = reactivatedByUserId;
    }

    /**
     * Update the username when email changes (they must always match).
     */
    public void setEmail(String email) {
        this.email = email;
        this.username = email; // Keep username synchronized with email
    }

    /**
     * Get full name for display purposes.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get display name with user type.
     */
    public String getDisplayNameWithType() {
        return getFullName() + " (" + getUserLevel().getDisplayName() + ")";
    }

    /**
     * Check if user has a specific permission (from roles or custom permissions).
     */
    public boolean hasPermission(String permissionName) {
        // Check role permissions
        for (Role role : roles) {
            if (role.hasPermission(permissionName)) {
                return true;
            }
        }

        // Check custom permissions
        return customPermissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    // JPA Lifecycle Methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        validateAccountAssociation();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateAccountAssociation();
    }

    // Spring Security UserDetails Implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role-based authorities
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Add permissions from roles
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
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null && isActive;
    }

    public Boolean getIsPrimaryAccountUser() { return isPrimaryAccountUser; }
    public void setIsPrimaryAccountUser(Boolean isPrimaryAccountUser) {
        this.isPrimaryAccountUser = isPrimaryAccountUser;
    }    /**
     * Set this user as the primary account user (account administrator).
     * Only one user per account should have this flag set to true.
     */
    public void setPrimaryAccountUserFlag(Boolean isPrimary) {
        this.isPrimaryAccountUser = isPrimary;
    }

    /**
     * Check if this user is the primary account user (account owner/administrator).
     */
    public boolean isPrimaryAccountUser() {
        return isPrimaryAccountUser != null && isPrimaryAccountUser;
    }    /**
     * Check if this user can grant veterinarian access to their account.
     * Only account admins can grant veterinarian access.
     */
    public boolean canGrantVeterinarianAccess() {
        return isAccountAdmin();
    }

    /**
     * Check if this user can modify account settings.
     * Only account admins can modify account-level settings.
     */
    public boolean canModifyAccountSettings() {
        return isAccountAdmin();
    }    /**
     * Check if this user has account administrator privileges.
     * Account admin rights are determined by having the "ACCOUNT_ADMIN" role.
     */
    public boolean isAccountAdmin() {
        return isAccountUser() && hasRole("ACCOUNT_ADMIN");
    }

    /**
     * Check if this user can create other users in their account.
     * Only account admins can create additional users.
     */
    public boolean canCreateAccountUsers() {
        return isAccountAdmin();
    }

    /**
     * Check if user is an account-level user (belongs to a customer account).
     */
    public boolean isAccountUser() {
        return getUserLevel() == UserLevel.ACCOUNT_USER;
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public void setUsername(String username) {
        this.username = username;
        // Note: Email should be set separately to maintain sync
    }

    public String getEmail() { return email; }
    // setEmail method above handles username sync

    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getEmergencyContactRelationship() { return emergencyContactRelationship; }
    public void setEmergencyContactRelationship(String emergencyContactRelationship) { this.emergencyContactRelationship = emergencyContactRelationship; }

    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public Boolean getSmsNotifications() { return smsNotifications; }
    public void setSmsNotifications(Boolean smsNotifications) { this.smsNotifications = smsNotifications; }

    public Boolean getPushNotifications() { return pushNotifications; }
    public void setPushNotifications(Boolean pushNotifications) { this.pushNotifications = pushNotifications; }

    public Account getPrimaryAccount() { return primaryAccount; }
    public void setPrimaryAccount(Account primaryAccount) { this.primaryAccount = primaryAccount; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Set<Permission> getCustomPermissions() { return customPermissions; }
    public void setCustomPermissions(Set<Permission> customPermissions) { this.customPermissions = customPermissions; }

    public List<Email> getAdditionalEmails() { return additionalEmails; }
    public void setAdditionalEmails(List<Email> additionalEmails) { this.additionalEmails = additionalEmails; }

    public List<Phone> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<Phone> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getEndReason() { return endReason; }
    public void setEndReason(String endReason) { this.endReason = endReason; }

    public Long getEndedByUserId() { return endedByUserId; }
    public void setEndedByUserId(Long endedByUserId) { this.endedByUserId = endedByUserId; }

    public LocalDateTime getReactivatedDate() { return reactivatedDate; }
    public void setReactivatedDate(LocalDateTime reactivatedDate) { this.reactivatedDate = reactivatedDate; }

    public Long getReactivatedByUserId() { return reactivatedByUserId; }
    public void setReactivatedByUserId(Long reactivatedByUserId) { this.reactivatedByUserId = reactivatedByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Long getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(Long updatedByUserId) { this.updatedByUserId = updatedByUserId; }

    // toString, equals, and hashCode
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", userLevel=" + getUserLevel() +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}