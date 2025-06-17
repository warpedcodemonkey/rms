package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Base User entity for the Ranch Management System.
 *
 * Implements Spring Security UserDetails for authentication.
 * Uses single-table inheritance with discriminator column for user types.
 * Supports soft delete (never hard delete users to preserve data integrity).
 * Email address is always the username for all user types.
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

    // Spring Security UserDetails Implementation
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

    // Standard Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.username = email; // Keep username synchronized with email
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }

    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public Boolean getPushNotifications() {
        return pushNotifications;
    }

    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public Long getEndedByUserId() {
        return endedByUserId;
    }

    public void setEndedByUserId(Long endedByUserId) {
        this.endedByUserId = endedByUserId;
    }

    public LocalDateTime getReactivatedDate() {
        return reactivatedDate;
    }

    public void setReactivatedDate(LocalDateTime reactivatedDate) {
        this.reactivatedDate = reactivatedDate;
    }

    public Long getReactivatedByUserId() {
        return reactivatedByUserId;
    }

    public void setReactivatedByUserId(Long reactivatedByUserId) {
        this.reactivatedByUserId = reactivatedByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("username='" + username + "'")
                .add("email='" + email + "'")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("isActive=" + isActive)
                .add("createdAt=" + createdAt)
                .toString();
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