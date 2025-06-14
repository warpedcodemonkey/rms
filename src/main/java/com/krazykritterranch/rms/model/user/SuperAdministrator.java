package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * SuperAdministrator represents the highest level of users in the RMS system.
 *
 * SuperAdministrators are platform owners/employees with complete system access.
 * All business logic and validation is handled in service layers.
 *
 * Business Rules (handled in services):
 * - SuperAdministrators are system-level users (no account association)
 * - Only SuperAdministrators can create other SuperAdministrators
 * - At least one SuperAdministrator must exist in the system at all times
 * - SuperAdministrators bypass all multi-tenant restrictions for support purposes
 */
@Entity
@DiscriminatorValue("SUPER_ADMIN")
public class SuperAdministrator extends User {

    @Column(name = "employee_id", length = 50)
    @Size(max = 50, message = "Employee ID cannot exceed 50 characters")
    private String employeeId;

    @Column(name = "department", length = 100)
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "security_clearance_level")
    @Enumerated(EnumType.STRING)
    private SecurityClearanceLevel securityClearanceLevel = SecurityClearanceLevel.STANDARD;

    @Column(name = "can_create_super_admins")
    private Boolean canCreateSuperAdmins = true;

    @Column(name = "can_modify_system_settings")
    private Boolean canModifySystemSettings = true;

    @Column(name = "can_access_billing")
    private Boolean canAccessBilling = true;

    @Column(name = "emergency_contact_required")
    private Boolean emergencyContactRequired = true;

    @Column(name = "two_factor_required")
    private Boolean twoFactorRequired = true;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes = 30;

    @Column(name = "last_security_review")
    private LocalDateTime lastSecurityReview;

    @Column(name = "next_security_review_due")
    private LocalDateTime nextSecurityReviewDue;

    /**
     * Security clearance levels for SuperAdministrators
     */
    public enum SecurityClearanceLevel {
        STANDARD("Standard", "Standard administrative access"),
        ELEVATED("Elevated", "Elevated access including sensitive operations"),
        MAXIMUM("Maximum", "Maximum access including system architecture changes");

        private final String displayName;
        private final String description;

        SecurityClearanceLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    // Constructors
    public SuperAdministrator() {
        super();
    }

    public SuperAdministrator(String email, String password, String firstName, String lastName) {
        super(email, password, firstName, lastName);
    }

    // Abstract Method Implementations
    @Override
    public UserLevel getUserLevel() {
        return UserLevel.SUPER_ADMIN;
    }

    @Override
    public String getUserType() {
        return "SUPER_ADMIN";
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public SecurityClearanceLevel getSecurityClearanceLevel() {
        return securityClearanceLevel;
    }

    public void setSecurityClearanceLevel(SecurityClearanceLevel securityClearanceLevel) {
        this.securityClearanceLevel = securityClearanceLevel;
    }

    public Boolean getCanCreateSuperAdmins() {
        return canCreateSuperAdmins;
    }

    public void setCanCreateSuperAdmins(Boolean canCreateSuperAdmins) {
        this.canCreateSuperAdmins = canCreateSuperAdmins;
    }

    public Boolean getCanModifySystemSettings() {
        return canModifySystemSettings;
    }

    public void setCanModifySystemSettings(Boolean canModifySystemSettings) {
        this.canModifySystemSettings = canModifySystemSettings;
    }

    public Boolean getCanAccessBilling() {
        return canAccessBilling;
    }

    public void setCanAccessBilling(Boolean canAccessBilling) {
        this.canAccessBilling = canAccessBilling;
    }

    public Boolean getEmergencyContactRequired() {
        return emergencyContactRequired;
    }

    public void setEmergencyContactRequired(Boolean emergencyContactRequired) {
        this.emergencyContactRequired = emergencyContactRequired;
    }

    public Boolean getTwoFactorRequired() {
        return twoFactorRequired;
    }

    public void setTwoFactorRequired(Boolean twoFactorRequired) {
        this.twoFactorRequired = twoFactorRequired;
    }

    public Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public LocalDateTime getLastSecurityReview() {
        return lastSecurityReview;
    }

    public void setLastSecurityReview(LocalDateTime lastSecurityReview) {
        this.lastSecurityReview = lastSecurityReview;
    }

    public LocalDateTime getNextSecurityReviewDue() {
        return nextSecurityReviewDue;
    }

    public void setNextSecurityReviewDue(LocalDateTime nextSecurityReviewDue) {
        this.nextSecurityReviewDue = nextSecurityReviewDue;
    }

    // toString for debugging
    @Override
    public String toString() {
        return "SuperAdministrator{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", securityClearanceLevel=" + securityClearanceLevel +
                ", isActive=" + getIsActive() +
                '}';
    }
}