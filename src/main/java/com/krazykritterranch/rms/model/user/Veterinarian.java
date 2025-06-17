package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Veterinarian represents veterinary professionals who can access
 * farm medical records and collaborate on animal health management.
 *
 * Veterinarians are granted access to specific farm accounts and
 * have specialized permissions for medical record management.
 */
@Entity
@DiscriminatorValue("VETERINARIAN")
public class Veterinarian extends User {

    @Column(name = "license_number", length = 50)
    private String licenseNumber; // Professional veterinary license number

    @Column(name = "license_state", length = 50)
    private String licenseState; // State where license is valid

    @Column(name = "practice_name", length = 200)
    private String practiceName; // Veterinary practice/clinic name

    @Column(name = "practice_address", length = 500)
    private String practiceAddress; // Practice physical address

    @Column(name = "practice_phone", length = 20)
    private String practicePhone; // Practice contact number

    @Column(name = "specialization", length = 200)
    private String specialization; // Areas of specialization (e.g., "Large Animal, Equine")

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false; // Professional verification status

    @Column(name = "verified_by_user_id")
    private Long verifiedByUserId; // Platform admin who verified credentials

    // Constructors
    public Veterinarian() {
        super();
    }

    public Veterinarian(String email, String password, String firstName, String lastName) {
        super(email, password, firstName, lastName);
    }

    public Veterinarian(String email, String password, String firstName, String lastName,
                        String licenseNumber, String licenseState, String practiceName) {
        super(email, password, firstName, lastName);
        this.licenseNumber = licenseNumber;
        this.licenseState = licenseState;
        this.practiceName = practiceName;
    }

    @Override
    public String getUserType() {
        return "VETERINARIAN";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Basic authority for veterinarians
        // Actual farm-specific permissions will be managed through Role/Permission entities
        return List.of(new SimpleGrantedAuthority("ROLE_VETERINARIAN"));
    }

    // Getters and Setters
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseState() {
        return licenseState;
    }

    public void setLicenseState(String licenseState) {
        this.licenseState = licenseState;
    }

    public String getPracticeName() {
        return practiceName;
    }

    public void setPracticeName(String practiceName) {
        this.practiceName = practiceName;
    }

    public String getPracticeAddress() {
        return practiceAddress;
    }

    public void setPracticeAddress(String practiceAddress) {
        this.practiceAddress = practiceAddress;
    }

    public String getPracticePhone() {
        return practicePhone;
    }

    public void setPracticePhone(String practicePhone) {
        this.practicePhone = practicePhone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Long getVerifiedByUserId() {
        return verifiedByUserId;
    }

    public void setVerifiedByUserId(Long verifiedByUserId) {
        this.verifiedByUserId = verifiedByUserId;
    }

    @Override
    public String toString() {
        return "Veterinarian{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", licenseState='" + licenseState + '\'' +
                ", practiceName='" + practiceName + '\'' +
                ", isVerified=" + isVerified +
                ", isActive=" + getIsActive() +
                '}';
    }
}