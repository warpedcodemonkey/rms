// =======================
// 1. SUPER ADMINISTRATOR
// =======================

package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("SUPER_ADMIN")
public class SuperAdministrator extends User {

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "department")
    private String department;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    public SuperAdministrator() {
        super();
    }

    public SuperAdministrator(String username, String email, String password, String firstName, String lastName) {
        super();
        setUsername(username);
        setEmail(email);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPrimaryAccount(null); // System users have no account
    }

    @Override
    public UserLevel getUserLevel() {
        return UserLevel.SUPER_ADMIN;
    }

    @Override
    public String getUserTypeString() {
        return "SUPER_ADMIN";
    }

    // Employee-specific fields
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getHireDate() { return hireDate; }
    public void setHireDate(LocalDateTime hireDate) { this.hireDate = hireDate; }
}