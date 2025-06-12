package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;


@Entity
@DiscriminatorValue("VETERINARIAN")
public class Veterinarian extends User {

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "clinic_name")
    private String clinicName;

    @Column(name = "clinic_address")
    private String clinicAddress;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    // Constructors
    public Veterinarian() {
        super();
    }

    public Veterinarian(String username, String email, String password, String firstName, String lastName, String licenseNumber) {
        super(username, email, password, firstName, lastName);
        this.licenseNumber = licenseNumber;
    }

    @Override
    public String getUserType() {
        return "VETERINARIAN";
    }

    // Getters and Setters
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
}