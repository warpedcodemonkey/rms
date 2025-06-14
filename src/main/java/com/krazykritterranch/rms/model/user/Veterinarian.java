package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("VETERINARIAN")
public class Veterinarian extends User {

    @Column(name = "vet_license_number", unique = true)
    private String vetLicenseNumber;

    @Column(name = "practice_name")
    private String practiceName;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "license_state")
    private String licenseState;

    @Column(name = "practice_address")
    private String practiceAddress;

    @Column(name = "practice_phone")
    private String practicePhone;

    // Vets can be granted access to multiple accounts
    @OneToMany(mappedBy = "veterinarian", cascade = CascadeType.ALL)
    private Set<VetPermission> accountPermissions = new HashSet<>();

    public Veterinarian() {
        super();
    }

    public Veterinarian(String username, String email, String password, String firstName, String lastName, String vetLicenseNumber) {
        super();
        setUsername(username);
        setEmail(email);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPrimaryAccount(null); // Vets don't belong to a specific account
        this.vetLicenseNumber = vetLicenseNumber;
    }

    @Override
    public UserLevel getUserLevel() {
        return UserLevel.VETERINARIAN;
    }

    @Override
    public String getUserTypeString() {
        return "VETERINARIAN";
    }

    // Veterinarian-specific methods
    public boolean canAccessAccount(Long accountId) {
        return accountPermissions.stream()
                .anyMatch(permission -> permission.getAccount().getId().equals(accountId)
                        && permission.getIsActive());
    }

    public VetPermission getPermissionForAccount(Long accountId) {
        return accountPermissions.stream()
                .filter(permission -> permission.getAccount().getId().equals(accountId))
                .findFirst()
                .orElse(null);
    }

    // Veterinarian fields
    public String getVetLicenseNumber() { return vetLicenseNumber; }
    public void setVetLicenseNumber(String vetLicenseNumber) { this.vetLicenseNumber = vetLicenseNumber; }

    public String getPracticeName() { return practiceName; }
    public void setPracticeName(String practiceName) { this.practiceName = practiceName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLicenseState() { return licenseState; }
    public void setLicenseState(String licenseState) { this.licenseState = licenseState; }

    public Set<VetPermission> getAccountPermissions() { return accountPermissions; }
    public void setAccountPermissions(Set<VetPermission> accountPermissions) { this.accountPermissions = accountPermissions; }
}