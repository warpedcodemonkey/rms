package com.krazykritterranch.rms.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMINISTRATOR")
public class Administrator extends User {

    @Column(name = "department")
    private String department;

    @Column(name = "access_level")
    private Integer accessLevel;

    // Constructors
    public Administrator() {
        super();
    }

    public Administrator(String username, String email, String password, String firstName, String lastName) {
        super(username, email, password, firstName, lastName);
    }

    @Override
    public String getUserType() {
        return "ADMINISTRATOR";
    }

    // Getters and Setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getAccessLevel() { return accessLevel; }
    public void setAccessLevel(Integer accessLevel) { this.accessLevel = accessLevel; }
}