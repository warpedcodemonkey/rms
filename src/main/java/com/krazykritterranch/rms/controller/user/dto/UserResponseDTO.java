package com.krazykritterranch.rms.controller.user.dto;

import com.krazykritterranch.rms.model.user.User;
import java.time.LocalDateTime;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String userType;
    private Long accountId;
    private String accountName;

    // Customer-specific fields
    private String customerNumber;
    private String emergencyContact;
    private String emergencyPhone;

    // Administrator-specific fields
    private String department;
    private Integer accessLevel;

    // Veterinarian-specific fields
    private String licenseNumber;
    private String specialization;
    private String clinicName;

    // Constructor
    public UserResponseDTO() {}

    public static UserResponseDTO fromUser(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setUserType(user.getUserType());

        // Set account info if user has an account
        if (user.getPrimaryAccount() != null) {
            dto.setAccountId(user.getPrimaryAccount().getId());
            dto.setAccountName(user.getPrimaryAccount().getFarmName());
        }

        // Set type-specific fields based on user type
        switch (user.getUserType()) {
            case "CUSTOMER":
                if (user instanceof com.krazykritterranch.rms.model.user.Customer) {
                    com.krazykritterranch.rms.model.user.Customer customer =
                            (com.krazykritterranch.rms.model.user.Customer) user;
                    dto.setCustomerNumber(customer.getCustomerNumber());
                    dto.setEmergencyContact(customer.getEmergencyContact());
                    dto.setEmergencyPhone(customer.getEmergencyPhone());
                }
                break;
            case "ADMINISTRATOR":
                if (user instanceof com.krazykritterranch.rms.model.user.Administrator) {
                    com.krazykritterranch.rms.model.user.Administrator admin =
                            (com.krazykritterranch.rms.model.user.Administrator) user;
                    dto.setDepartment(admin.getDepartment());
                    dto.setAccessLevel(admin.getAccessLevel());
                }
                break;
            case "VETERINARIAN":
                if (user instanceof com.krazykritterranch.rms.model.user.Veterinarian) {
                    com.krazykritterranch.rms.model.user.Veterinarian vet =
                            (com.krazykritterranch.rms.model.user.Veterinarian) user;
                    dto.setLicenseNumber(vet.getLicenseNumber());
                    dto.setSpecialization(vet.getSpecialization());
                    dto.setClinicName(vet.getClinicName());
                }
                break;
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    // Customer fields
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }

    // Administrator fields
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getAccessLevel() { return accessLevel; }
    public void setAccessLevel(Integer accessLevel) { this.accessLevel = accessLevel; }

    // Veterinarian fields
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }
}