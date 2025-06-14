package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.common.Account;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("SUPPORT_ADMIN")
public class SupportAdministrator extends User {

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "support_tier")
    private Integer supportTier; // 1 = basic, 2 = advanced, etc.

    @Column(name = "department")
    private String department;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "max_account_access_level")
    private String maxAccountAccessLevel; // What level of account issues they can handle

    public SupportAdministrator() {
        super();
    }

    public SupportAdministrator(String username, String email, String password, String firstName, String lastName) {
        super();
        setUsername(username);
        setEmail(email);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPrimaryAccount(null); // Support users have no account
        this.supportTier = 1; // Default to basic support
    }

    @Override
    public UserLevel getUserLevel() {
        return UserLevel.SUPPORT_ADMIN;
    }

    @Override
    public String getUserTypeString() {
        return "SUPPORT_ADMIN";
    }

    // Support-specific methods
    public boolean canAccessAccount(Account account) {
        // Support can access accounts based on their tier and permissions
        return true; // Basic implementation - refine with business rules
    }

    // Support-specific fields
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Integer getSupportTier() { return supportTier; }
    public void setSupportTier(Integer supportTier) { this.supportTier = supportTier; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMaxAccountAccessLevel() { return maxAccountAccessLevel; }
    public void setMaxAccountAccessLevel(String maxAccountAccessLevel) { this.maxAccountAccessLevel = maxAccountAccessLevel; }
}