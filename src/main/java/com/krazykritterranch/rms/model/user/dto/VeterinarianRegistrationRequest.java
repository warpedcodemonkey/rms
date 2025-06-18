package com.krazykritterranch.rms.model.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for veterinarian registration requests.
 */
public class VeterinarianRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    private String licenseNumber;

    @NotBlank(message = "License state is required")
    @Size(max = 50, message = "License state cannot exceed 50 characters")
    private String licenseState;

    @NotBlank(message = "Practice name is required")
    @Size(max = 200, message = "Practice name cannot exceed 200 characters")
    private String practiceName;

    @Size(max = 500, message = "Practice address cannot exceed 500 characters")
    private String practiceAddress;

    @Pattern(regexp = "^[\\+]?[1-9][\\d]{0,15}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Practice phone cannot exceed 20 characters")
    private String practicePhone;

    @Size(max = 200, message = "Specialization cannot exceed 200 characters")
    private String specialization;

    // Default constructor
    public VeterinarianRegistrationRequest() {
    }

    // Constructor with required parameters
    public VeterinarianRegistrationRequest(String email, String password, String firstName, String lastName,
                                           String licenseNumber, String licenseState, String practiceName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
        this.licenseState = licenseState;
        this.practiceName = practiceName;
    }

    // Constructor with all parameters
    public VeterinarianRegistrationRequest(String email, String password, String firstName, String lastName,
                                           String licenseNumber, String licenseState, String practiceName,
                                           String practiceAddress, String practicePhone, String specialization) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
        this.licenseState = licenseState;
        this.practiceName = practiceName;
        this.practiceAddress = practiceAddress;
        this.practicePhone = practicePhone;
        this.specialization = specialization;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
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

    @Override
    public String toString() {
        return "VeterinarianRegistrationRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", licenseState='" + licenseState + '\'' +
                ", practiceName='" + practiceName + '\'' +
                ", practiceAddress='" + practiceAddress + '\'' +
                ", practicePhone='" + practicePhone + '\'' +
                ", specialization='" + specialization + '\'' +
                '}';
    }
}