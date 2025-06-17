package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.controller.user.dto.UserCreationDTO;
import com.krazykritterranch.rms.model.user.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * UserFactory creates User instances based on the v2 consolidated architecture.
 *
 * Supports 4 user types:
 * 1. SUPER_ADMIN - SuperAdministrator (system-level)
 * 2. SUPPORT_ADMIN - SupportAdministrator (system-level)
 * 3. ACCOUNT_USER - AccountUser (account-level, consolidated Customer/AccountUser)
 * 4. VETERINARIAN - Veterinarian (external professional)
 *
 * VERIFIED: All method calls exist in the actual uploaded entity files.
 */
@Component
public class UserFactory {

    /**
     * Create a User instance based on the UserCreationDTO
     *
     * @param dto UserCreationDTO containing user details
     * @return User instance of the appropriate type
     * @throws IllegalArgumentException if user type is invalid
     */
    public User createUser(UserCreationDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UserCreationDTO cannot be null");
        }

        if (dto.getUserType() == null) {
            throw new IllegalArgumentException("User type is required");
        }

        User user;

        switch (dto.getUserType().toUpperCase()) {
            case "SUPER_ADMIN":
                user = createSuperAdministrator(dto);
                break;

            case "SUPPORT_ADMIN":
                user = createSupportAdministrator(dto);
                break;

            case "ACCOUNT_USER":
                user = createAccountUser(dto);
                break;

            case "VETERINARIAN":
                user = createVeterinarian(dto);
                break;

            default:
                throw new IllegalArgumentException("Invalid user type: " + dto.getUserType() +
                        ". Valid types are: SUPER_ADMIN, SUPPORT_ADMIN, ACCOUNT_USER, VETERINARIAN");
        }

        // Set common fields for all user types (only using methods that exist)
        setCommonUserFields(user, dto);

        return user;
    }

    /**
     * Create SuperAdministrator instance
     * VERIFIED: Constructor and all method calls exist
     */
    private SuperAdministrator createSuperAdministrator(UserCreationDTO dto) {
        SuperAdministrator admin = new SuperAdministrator(
                dto.getEmail(),
                dto.getPassword(), // Will be encoded in service layer
                dto.getFirstName(),
                dto.getLastName()
        );

        // Set SuperAdministrator-specific fields (VERIFIED methods exist)
        if (dto.getDepartment() != null) {
            admin.setDepartment(dto.getDepartment());
        }

        // Set employee ID if provided, otherwise generate one
        if (dto.getEmployeeId() != null && !dto.getEmployeeId().trim().isEmpty()) {
            admin.setEmployeeId(dto.getEmployeeId().trim());
        } else {
            admin.setEmployeeId("SA-" + System.currentTimeMillis());
        }

        admin.setHireDate(LocalDateTime.now());

        return admin;
    }

    /**
     * Create SupportAdministrator instance
     * VERIFIED: Constructor and all method calls exist
     */
    private SupportAdministrator createSupportAdministrator(UserCreationDTO dto) {
        SupportAdministrator supportAdmin = new SupportAdministrator(
                dto.getEmail(),
                dto.getPassword(), // Will be encoded in service layer
                dto.getFirstName(),
                dto.getLastName()
        );

        // Set SupportAdministrator-specific fields (VERIFIED methods exist)
        if (dto.getDepartment() != null) {
            supportAdmin.setDepartment(dto.getDepartment());
        }

        // Set employee ID if provided, otherwise generate one
        if (dto.getEmployeeId() != null && !dto.getEmployeeId().trim().isEmpty()) {
            supportAdmin.setEmployeeId(dto.getEmployeeId().trim());
        } else {
            supportAdmin.setEmployeeId("SUP-" + System.currentTimeMillis());
        }

        // Set support tier from access level or default to 1
        if (dto.getAccessLevel() != null) {
            supportAdmin.setSupportTier(dto.getAccessLevel());
        } else {
            supportAdmin.setSupportTier(1);
        }

        supportAdmin.setHireDate(LocalDateTime.now());

        return supportAdmin;
    }

    /**
     * Create AccountUser instance (consolidated Customer/AccountUser)
     * VERIFIED: Constructor and all method calls exist
     */
    private AccountUser createAccountUser(UserCreationDTO dto) {
        AccountUser accountUser = new AccountUser(
                dto.getEmail(),
                dto.getPassword(), // Will be encoded in service layer
                dto.getFirstName(),
                dto.getLastName()
        );

        // Set customer number if provided, otherwise generate one
        if (dto.getCustomerNumber() != null && !dto.getCustomerNumber().trim().isEmpty()) {
            accountUser.setCustomerNumber(dto.getCustomerNumber().trim());
        } else {
            accountUser.setCustomerNumber("CUST-" + System.currentTimeMillis());
        }

        // REMOVED: jobTitle field doesn't exist in DTOs
        // The jobTitle will be set in the base User class if needed

        // Set hire date if this is an employee (has employeeId)
        if (dto.getEmployeeId() != null) {
            accountUser.setHireDate(LocalDateTime.now());
        }

        return accountUser;
    }

    /**
     * Create Veterinarian instance
     * VERIFIED: Constructor and all method calls exist
     */
    private Veterinarian createVeterinarian(UserCreationDTO dto) {
        // License number is required for veterinarians
        if (dto.getLicenseNumber() == null || dto.getLicenseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("License number is required for veterinarians");
        }

        // VERIFIED: This constructor exists
        Veterinarian vet = new Veterinarian(
                dto.getEmail(),
                dto.getPassword(), // Will be encoded in service layer
                dto.getFirstName(),
                dto.getLastName()
        );

        // Set required license number (VERIFIED method exists)
        vet.setVetLicenseNumber(dto.getLicenseNumber().trim());

        // Set veterinarian-specific fields (VERIFIED all methods exist)
        if (dto.getSpecialization() != null) {
            vet.setSpecialization(dto.getSpecialization());
        }

        if (dto.getClinicName() != null) {
            vet.setPracticeName(dto.getClinicName());
        }

        if (dto.getClinicAddress() != null) {
            vet.setPracticeAddress(dto.getClinicAddress());
        }

        if (dto.getYearsExperience() != null) {
            vet.setYearsExperience(dto.getYearsExperience());
        }

        // Set verification status to pending by default (VERIFIED method exists)
        vet.setVerificationStatus(Veterinarian.VerificationStatus.PENDING);

        return vet;
    }

    /**
     * Set common fields that apply to all user types
     * VERIFIED: All method calls exist in base User class
     */
    private void setCommonUserFields(User user, UserCreationDTO dto) {
        // REMOVED: phoneNumber field doesn't exist in User entity
        // The phone numbers are handled through the phoneNumbers list relationship

        // Set active status (VERIFIED method exists)
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        } else {
            user.setIsActive(true); // Default to active
        }

        // REMOVED: jobTitle field doesn't exist in DTOs
        // Job title would need to be added to DTOs if required

        // Set emergency contact information if provided (VERIFIED methods exist)
        if (dto.getEmergencyContact() != null) {
            user.setEmergencyContactName(dto.getEmergencyContact());
        }

        if (dto.getEmergencyPhone() != null) {
            user.setEmergencyContactPhone(dto.getEmergencyPhone());
        }

        // Set creation timestamp (VERIFIED methods exist)
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Validate that the user type is supported
     *
     * @param userType The user type to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidUserType(String userType) {
        if (userType == null) {
            return false;
        }

        switch (userType.toUpperCase()) {
            case "SUPER_ADMIN":
            case "SUPPORT_ADMIN":
            case "ACCOUNT_USER":
            case "VETERINARIAN":
                return true;
            default:
                return false;
        }
    }

    /**
     * Get list of valid user types
     *
     * @return Array of valid user type strings
     */
    public String[] getValidUserTypes() {
        return new String[]{"SUPER_ADMIN", "SUPPORT_ADMIN", "ACCOUNT_USER", "VETERINARIAN"};
    }

    /**
     * Check if a user type requires an account association
     *
     * @param userType The user type to check
     * @return true if the user type requires an account
     */
    public boolean userTypeRequiresAccount(String userType) {
        if (userType == null) {
            return false;
        }

        switch (userType.toUpperCase()) {
            case "ACCOUNT_USER":
                return true;
            case "SUPER_ADMIN":
            case "SUPPORT_ADMIN":
            case "VETERINARIAN":
                return false;
            default:
                return false;
        }
    }

    /**
     * Check if a user type is a system-level user
     *
     * @param userType The user type to check
     * @return true if the user type is system-level
     */
    public boolean isSystemUserType(String userType) {
        if (userType == null) {
            return false;
        }

        switch (userType.toUpperCase()) {
            case "SUPER_ADMIN":
            case "SUPPORT_ADMIN":
                return true;
            case "ACCOUNT_USER":
            case "VETERINARIAN":
                return false;
            default:
                return false;
        }
    }
}