package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.common.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * AccountUser represents all users who belong to a customer account in the RMS system.
 *
 * This unified entity type handles both:
 * - Account Administrators (isPrimaryAccountUser = true): Account owners who pay for the service
 * - Account Users (isPrimaryAccountUser = false): Employees, family members, etc.
 *
 * The ONLY difference between users is:
 * 1. Their UserLevel (which determines system-wide hierarchy)
 * 2. The isPrimaryAccountUser flag (which determines account ownership)
 * 3. Their assigned Roles and Permissions (handled by Role/Permission system)
 *
 * All functional permissions are handled through Roles and Permissions, NOT individual fields.
 * All business logic is handled in service layers, NOT in this entity.
 */
@Entity
@DiscriminatorValue("ACCOUNT_USER")
public class AccountUser extends User {

    @Column(name = "customer_number", length = 50)
    @Size(max = 50, message = "Customer number cannot exceed 50 characters")
    private String customerNumber;

    @Column(name = "job_title", length = 150)
    @Size(max = 150, message = "Job title cannot exceed 150 characters")
    private String jobTitle;

    @Column(name = "employee_number", length = 50)
    @Size(max = 50, message = "Employee number cannot exceed 50 characters")
    private String employeeNumber;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "created_by_account_user_id")
    private Long createdByAccountUserId;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "total_logins")
    private Long totalLogins = 0L;

    // Constructors
    public AccountUser() {
        super();
    }

    public AccountUser(String email, String password, String firstName, String lastName) {
        super(email, password, firstName, lastName);
    }

    // Abstract Method Implementations
    @Override
    public UserLevel getUserLevel() {
        return UserLevel.ACCOUNT_USER;
    }

    @Override
    public String getUserType() {
        return "ACCOUNT_USER";
    }

    // Getters and Setters
    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public Long getCreatedByAccountUserId() {
        return createdByAccountUserId;
    }

    public void setCreatedByAccountUserId(Long createdByAccountUserId) {
        this.createdByAccountUserId = createdByAccountUserId;
    }

    public LocalDateTime getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public Long getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(Long totalLogins) {
        this.totalLogins = totalLogins;
    }

    // toString for debugging
    @Override
    public String toString() {
        return "AccountUser{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", customerNumber='" + customerNumber + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", isPrimaryAccountUser=" + isPrimaryAccountUser() +
                ", accountId=" + (getPrimaryAccount() != null ? getPrimaryAccount().getId() : "null") +
                ", isActive=" + getIsActive() +
                '}';
    }
}