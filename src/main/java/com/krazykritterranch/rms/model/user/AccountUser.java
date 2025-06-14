package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.common.Account;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("ACCOUNT_USER")
public class AccountUser extends User {

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "created_by_customer_id")
    private Long createdByCustomerId; // Which customer created this user

    @Column(name = "permission_restrictions")
    private String permissionRestrictions; // JSON of specific restrictions

    public AccountUser() {
        super();
    }

    public AccountUser(String username, String email, String password, String firstName, String lastName,
                       Account account, Long createdByCustomerId) {
        super();
        setUsername(username);
        setEmail(email);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPrimaryAccount(account); // Account user MUST have an account
        this.createdByCustomerId = createdByCustomerId;
        this.hireDate = LocalDateTime.now();
    }

    @Override
    public UserLevel getUserLevel() {
        return UserLevel.ACCOUNT_USER;
    }

    @Override
    public String getUserTypeString() {
        return "ACCOUNT_USER";
    }

    // Account user specific methods
    public boolean wasCreatedBy(Long customerId) {
        return createdByCustomerId != null && createdByCustomerId.equals(customerId);
    }

    // Account user fields
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public LocalDateTime getHireDate() { return hireDate; }
    public void setHireDate(LocalDateTime hireDate) { this.hireDate = hireDate; }

    public Long getCreatedByCustomerId() { return createdByCustomerId; }
    public void setCreatedByCustomerId(Long createdByCustomerId) { this.createdByCustomerId = createdByCustomerId; }

    public String getPermissionRestrictions() { return permissionRestrictions; }
    public void setPermissionRestrictions(String permissionRestrictions) { this.permissionRestrictions = permissionRestrictions; }
}