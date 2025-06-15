package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.model.user.*;
import com.krazykritterranch.rms.repositories.user.RoleRepository;
import com.krazykritterranch.rms.repositories.user.UserRepository;
import com.krazykritterranch.rms.repositories.user.VetPermissionRepository;
import com.krazykritterranch.rms.repositories.user.VeterinarianRepository;
import com.krazykritterranch.rms.service.security.SecurityAnnotations;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.krazykritterranch.rms.repositories.common.AccountRepository;
import com.krazykritterranch.rms.controller.user.dto.UserUpdateDTO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.*;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private VetPermissionRepository vetPermissionRepository;

    // Public methods (no tenant restriction) - updated to use eager fetching
    public User findByUsername(String username) {
        return userRepository.findByUsernameWithRoles(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmailWithRoles(email).orElse(null);
    }

    // Add the missing findByLogin method that was referenced in CustomUserDetailsService
    public User findByLogin(String login) {
        // Try to find by username first, then by email
        User user = findByUsername(login);
        if (user == null) {
            user = findByEmail(login);
        }
        return user;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Administrative method - admins only
    @SecurityAnnotations.RequireAdmin
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Account-scoped user access
    @SecurityAnnotations.RequireAccountAccess
    public List<User> getUsersByAccount(Long accountId) {
        if (tenantContext.isAdmin()) {
            return userRepository.findByAccountId(accountId);
        } else if (tenantContext.isAccountUser()) {
            if (!accountId.equals(tenantContext.getCurrentAccountId())) {
                throw new SecurityException("Access denied");
            }
            return userRepository.findByAccountId(accountId);
        } else if (tenantContext.isVeterinarian()) {
            // Veterinarians cannot access user management data for other users
            throw new SecurityException("Veterinarians cannot access user management data");
        }
        throw new SecurityException("Access denied");
    }

    // Enhanced findById with tenant security
    public User findById(Long id) {
        if (tenantContext.isAdmin()) {
            return userRepository.findById(id).orElse(null);
        } else if (tenantContext.isAccountUser()) {
            User foundUser = userRepository.findById(id).orElse(null);
            if (foundUser != null && foundUser.getPrimaryAccount() != null &&
                    foundUser.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                return foundUser;
            }
        } else if (tenantContext.isVeterinarian()) {
            // Veterinarians can only access their OWN user record
            Long currentUserId = tenantContext.getCurrentUserId();
            if (currentUserId != null && currentUserId.equals(id)) {
                return userRepository.findById(id).orElse(null);
            }
            throw new SecurityException("Veterinarians can only access their own user profile");
        }
        throw new SecurityException("Access denied");
    }

    public User saveUser(User user) {
        // Encode password if it's a new user or password is being changed
        if (user.getId() == null || isPasswordChanged(user)) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Security check for account assignment
        if (user.getPrimaryAccount() != null && tenantContext.isAccountUser()) {
            if (!user.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                throw new SecurityException("Cannot assign user to different account");
            }
        }

        return userRepository.save(user);
    }

    @SecurityAnnotations.RequireAccountAccess
    public boolean canAddUserToAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .map(account -> {
                    long currentUserCount = userRepository.countByAccountId(accountId);
                    return currentUserCount < account.getMaxUsers();
                })
                .orElse(false);
    }

    public User updateUserWithDto(Long id, UserUpdateDTO updateDto) {
        User existingUser = findById(id); // This already includes security checks
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Additional security check: allow veterinarians to update their own profile only
        if (tenantContext.isVeterinarian()) {
            Long currentUserId = tenantContext.getCurrentUserId();
            if (!currentUserId.equals(id)) {
                throw new SecurityException("Veterinarians can only update their own profile");
            }
        }

        // Update common fields (only if provided in DTO)
        if (updateDto.getUsername() != null && !updateDto.getUsername().trim().isEmpty()) {
            existingUser.setUsername(updateDto.getUsername().trim());
        }

        if (updateDto.getEmail() != null && !updateDto.getEmail().trim().isEmpty()) {
            existingUser.setEmail(updateDto.getEmail().trim());
        }

        if (updateDto.getFirstName() != null && !updateDto.getFirstName().trim().isEmpty()) {
            existingUser.setFirstName(updateDto.getFirstName().trim());
        }

        if (updateDto.getLastName() != null && !updateDto.getLastName().trim().isEmpty()) {
            existingUser.setLastName(updateDto.getLastName().trim());
        }

        if (updateDto.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updateDto.getPhoneNumber().trim());
        }

        // Handle password updates separately with proper encoding
        if (updateDto.getNewPassword() != null && !updateDto.getNewPassword().trim().isEmpty()) {
            // Allow users to change their own password, and admins to change any password
            if (tenantContext.isAdmin() || existingUser.getId().equals(tenantContext.getCurrentUserId())) {
                existingUser.setPassword(passwordEncoder.encode(updateDto.getNewPassword().trim()));
            } else {
                throw new SecurityException("You can only change your own password");
            }
        }

        // Only admins or the user themselves can change active status
        if (updateDto.getIsActive() != null &&
                (tenantContext.isAdmin() || existingUser.getId().equals(tenantContext.getCurrentUserId()))) {
            existingUser.setIsActive(updateDto.getIsActive());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        // Update type-specific fields based on user type
        switch (existingUser.getUserType()) {
            case "CUSTOMER":
                if (existingUser instanceof Customer) {
                    Customer customer = (Customer) existingUser;
                    updateCustomerFields(customer, updateDto);
                }
                break;

            case "ADMINISTRATOR":
                if (existingUser instanceof Administrator) {
                    Administrator admin = (Administrator) existingUser;
                    updateAdministratorFields(admin, updateDto);
                }
                break;

            case "VETERINARIAN":
                if (existingUser instanceof Veterinarian) {
                    Veterinarian vet = (Veterinarian) existingUser;
                    updateVeterinarianFields(vet, updateDto);
                }
                break;
        }

        return userRepository.save(existingUser);
    }

    // Delete user method - admins and account users only
    @SecurityAnnotations.RequireAccountAccess
    public void deleteUser(Long userId) {
        // Only admins and account users can delete users, NOT veterinarians
        if (tenantContext.isVeterinarian()) {
            throw new SecurityException("Veterinarians cannot modify other user accounts");
        }

        User user = findById(userId); // This already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Additional business logic checks
        if (tenantContext.isAccountUser()) {
            // Account users can only delete users from their own account
            if (!user.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                throw new SecurityException("Cannot delete user from different account");
            }

            // Check if this would violate account user limits
            if (user.getUserType().equals("ADMINISTRATOR")) {
                throw new SecurityException("Cannot delete account administrator");
            }
        }

        // Soft delete - set inactive instead of hard delete
        user.setIsActive(false);
        user.setDeactivatedAt(LocalDateTime.now());
        user.setDeactivatedBy(tenantContext.getCurrentUserId());
        userRepository.save(user);
    }

    // Reactivate user method
    @SecurityAnnotations.RequireAccountAccess
    public User reactivateUser(Long userId) {
        // Only admins and account users can reactivate users, NOT veterinarians
        if (tenantContext.isVeterinarian()) {
            throw new SecurityException("Veterinarians cannot modify other user accounts");
        }

        User user = findById(userId); // This already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (tenantContext.isAccountUser()) {
            // Check account user limits before reactivating
            Long accountId = tenantContext.getCurrentAccountId();
            if (!canAddUserToAccount(accountId)) {
                throw new IllegalArgumentException("Account has reached maximum user limit");
            }
        }

        user.setIsActive(true);
        user.setReactivatedAt(LocalDateTime.now());
        user.setReactivatedBy(tenantContext.getCurrentUserId());
        user.setDeactivatedAt(null);
        user.setDeactivatedBy(null);

        return userRepository.save(user);
    }

    // Update password method with security
    public void updateUserPassword(Long userId, String newPassword) {
        User user = findById(userId); // This already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Allow veterinarians to update their own password
        if (tenantContext.isVeterinarian()) {
            Long currentUserId = tenantContext.getCurrentUserId();
            if (!currentUserId.equals(userId)) {
                throw new SecurityException("Veterinarians can only change their own password");
            }
        }

        // Additional security checks for non-veterinarians
        if (tenantContext.isAccountUser()) {
            // Account users can only update passwords in their own account
            if (!user.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                throw new SecurityException("Cannot update password for user in different account");
            }

            // Account users cannot change admin passwords
            if (user.getUserType().equals("ADMINISTRATOR") &&
                    !tenantContext.getCurrentUserId().equals(userId)) {
                throw new SecurityException("Cannot change administrator password");
            }
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void assignRole(Long userId, String roleName) {
        // Only admins can assign roles
        if (!tenantContext.isAdmin()) {
            throw new SecurityException("Only administrators can assign roles");
        }

        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Optional<Role> role = roleRepository.findByName(roleName);
        if (role.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }

        user.getRoles().add(role.get());
        userRepository.save(user);
    }

    public void removeRole(Long userId, String roleName) {
        // Only admins can remove roles
        if (!tenantContext.isAdmin()) {
            throw new SecurityException("Only administrators can remove roles");
        }

        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.getRoles().removeIf(role -> role.getName().equals(roleName));
        userRepository.save(user);
    }

    public Map<String, Object> getAccountUserStats(Long accountId) {
        return accountRepository.findById(accountId)
                .map(account -> {
                    long currentUserCount = userRepository.countByAccountId(accountId);
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("currentUsers", currentUserCount);
                    stats.put("maxUsers", account.getMaxUsers());
                    stats.put("canAddUser", currentUserCount < account.getMaxUsers());
                    return stats;
                })
                .orElse(Collections.emptyMap());
    }

    // Method expected by UserController for user deletion checking
    public Map<String, Object> getUserDeletionInfo(Long userId) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Map<String, Object> deletionInfo = new HashMap<>();
        deletionInfo.put("canDelete", true);
        deletionInfo.put("hasActiveData", false); // Could check for livestock, contracts, etc.
        deletionInfo.put("warnings", Collections.emptyList());

        // Add logic here to check if user has dependent data
        // For example: active livestock, contracts, etc.

        return deletionInfo;
    }

    // Check if veterinarian has access to specific account - helper method
    public boolean hasVetPermissionForAccount(Long accountId) {
        if (!tenantContext.isVeterinarian()) {
            return false;
        }
        return vetPermissionRepository
                .findActivePermissionByVetAndAccount(tenantContext.getCurrentUserId(), accountId)
                .isPresent();
    }

    // Helper methods
    public boolean isPasswordChanged(User user) {
        if (user.getId() == null) {
            return true; // New user
        }

        User existingUser = userRepository.findById(user.getId()).orElse(null);
        if (existingUser == null) {
            return true;
        }

        // Check if the password is different (not encoded)
        return !passwordEncoder.matches(user.getPassword(), existingUser.getPassword());
    }

    // Helper method for updating Customer-specific fields
    public void updateCustomerFields(Customer customer, UserUpdateDTO updateDto) {
        if (updateDto.getCustomerNumber() != null && !updateDto.getCustomerNumber().trim().isEmpty()) {
            // Only admins can change customer numbers, or if it's empty/null currently
            if (tenantContext.isAdmin() || customer.getCustomerNumber() == null || customer.getCustomerNumber().trim().isEmpty()) {
                customer.setCustomerNumber(updateDto.getCustomerNumber().trim());
            }
        }

        if (updateDto.getEmergencyContact() != null) {
            customer.setEmergencyContact(updateDto.getEmergencyContact().trim());
        }

        if (updateDto.getEmergencyPhone() != null) {
            customer.setEmergencyPhone(updateDto.getEmergencyPhone().trim());
        }
    }

    // Helper method for updating Administrator-specific fields
    public void updateAdministratorFields(Administrator admin, UserUpdateDTO updateDto) {
        // Only allow other admins to update admin-specific fields
        if (!tenantContext.isAdmin()) {
            return; // Skip admin field updates if current user is not admin
        }

        if (updateDto.getDepartment() != null) {
            admin.setDepartment(updateDto.getDepartment().trim());
        }

        if (updateDto.getAccessLevel() != null) {
            admin.setAccessLevel(updateDto.getAccessLevel());
        }
    }

    // Helper method for updating Veterinarian-specific fields
    public void updateVeterinarianFields(Veterinarian vet, UserUpdateDTO updateDto) {
        // Veterinarians can update their own profile, admins can update any vet profile
        if (tenantContext.isVeterinarian()) {
            Long currentUserId = tenantContext.getCurrentUserId();
            if (!currentUserId.equals(vet.getId())) {
                throw new SecurityException("Veterinarians can only update their own profile");
            }
        }

        // Update veterinarian-specific fields using the correct DTO field names
        if (updateDto.getLicenseNumber() != null && !updateDto.getLicenseNumber().trim().isEmpty()) {
            vet.setLicenseNumber(updateDto.getLicenseNumber().trim());
        }

        if (updateDto.getSpecialization() != null) {
            vet.setSpecialization(updateDto.getSpecialization().trim());
        }

        if (updateDto.getClinicName() != null) {
            vet.setClinicName(updateDto.getClinicName().trim());
        }

        if (updateDto.getClinicAddress() != null) {
            vet.setClinicAddress(updateDto.getClinicAddress().trim());
        }

        if (updateDto.getYearsExperience() != null) {
            vet.setYearsExperience(updateDto.getYearsExperience());
        }
    }




    // Add this method to UserService.java
    public String validateUserDeletion(Long userId) {
        User user = findById(userId); // Already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user is trying to delete themselves
        if (userId.equals(tenantContext.getCurrentUserId())) {
            return "You cannot delete your own account";
        }

        // Check if this is the last administrator in the system
        if (user.getUserType().equals("ADMINISTRATOR")) {
            long adminCount = userRepository.countActiveAdministrators();
            if (adminCount <= 1) {
                return "Cannot delete the last administrator in the system";
            }
        }

        // Check if this user has dependent data that prevents deletion
        // According to the technical specifications, we need to check for:
        // - Active livestock records
        // - Active contracts
        // - Active veterinary permissions (for vets)

        // For now, return null (no validation errors) but this should be expanded
        // based on the actual business logic requirements

        // TODO: Add checks for:
        // - Livestock records created/managed by this user
        // - Contracts associated with this user
        // - Veterinary permissions if this is a veterinarian
        // - Any other business-critical data associations

        return null; // No validation errors
    }

    public void permanentlyDeleteUser(Long userId) {
        User user = findById(userId); // Already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Security check: only administrators can permanently delete users
        if (!tenantContext.isAdmin()) {
            throw new SecurityException("Only administrators can permanently delete users");
        }

        // Prevent administrators from deleting themselves
        if (userId.equals(tenantContext.getCurrentUserId())) {
            throw new IllegalArgumentException("You cannot permanently delete your own account");
        }

        // Check if this is the last administrator in the system
        if (user.getUserType().equals("ADMINISTRATOR")) {
            long adminCount = userRepository.countActiveAdministrators();
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Cannot delete the last administrator in the system");
            }
        }

        // Additional validation: ensure this action is safe
        String validationError = validateUserDeletion(userId);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Before permanent deletion, we should clean up any dependent data
        // According to the technical specifications, this could include:
        // - Removing veterinary permissions if this is a veterinarian
        // - Handling livestock records created by this user
        // - Managing contract associations

        // Handle veterinarian-specific cleanup
        if (user instanceof Veterinarian) {
            // Remove all veterinary permissions for this vet
            vetPermissionRepository.findActivePermissionsByVet(userId, LocalDateTime.now())
                    .forEach(permission -> {
                        permission.setIsActive(false);
                        vetPermissionRepository.save(permission);
                    });
        }

        // TODO: Add cleanup for other dependent data:
        // - Update livestock records where this user is recorded as creator/modifier
        // - Handle contract associations
        // - Clean up any audit logs or references

        // Perform the permanent deletion
        userRepository.deleteById(userId);
    }

    public void deactivateUser(Long userId) {
        User user = findById(userId); // Already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Security check: only admins and account users can deactivate users, NOT veterinarians
        if (tenantContext.isVeterinarian()) {
            throw new SecurityException("Veterinarians cannot deactivate user accounts");
        }

        // Additional business logic checks for account users
        if (tenantContext.isAccountUser()) {
            // Account users can only deactivate users from their own account
            if (user.getPrimaryAccount() == null ||
                    !user.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                throw new SecurityException("Cannot deactivate user from different account");
            }

            // Account users cannot deactivate administrators
            if (user.getUserType().equals("ADMINISTRATOR")) {
                throw new SecurityException("Account users cannot deactivate administrators");
            }
        }

        // Prevent users from deactivating themselves
        if (userId.equals(tenantContext.getCurrentUserId())) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }

        // Check if user is already inactive
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("User is already inactive");
        }

        // Perform soft delete - set inactive and track deactivation details
        user.setIsActive(false);
        user.setDeactivatedAt(LocalDateTime.now());
        user.setDeactivatedBy(tenantContext.getCurrentUserId());

        // Clear reactivation fields if they were previously set
        user.setReactivatedAt(null);
        user.setReactivatedBy(null);

        userRepository.save(user);
    }

    /**
     * Check if a customer number already exists for AccountUser entities
     * @param customerNumber the customer number to check
     * @return true if the customer number exists
     */
    public boolean existsByCustomerNumber(String customerNumber) {
        if (customerNumber == null || customerNumber.trim().isEmpty()) {
            return false;
        }

        // Use a query to check if any AccountUser has this customer number
        return userRepository.existsByCustomerNumber(customerNumber.trim());
    }

    public void logUserDeletion(Long deletedUserId, String reason, Long deletedByUserId) {
        // This method logs user deletion for audit purposes
        // According to the technical specifications, we need comprehensive audit trails

        User deletedUser = userRepository.findById(deletedUserId).orElse(null);
        User deletedByUser = userRepository.findById(deletedByUserId).orElse(null);

        // For now, we'll use simple logging - in production this should be enhanced
        // to use a proper audit logging system as mentioned in Task 17

        String logMessage = String.format(
                "USER DELETION AUDIT: User %s (ID: %d, Type: %s) permanently deleted by %s (ID: %d) at %s. Reason: %s",
                deletedUser != null ? deletedUser.getUsername() : "Unknown",
                deletedUserId,
                deletedUser != null ? deletedUser.getUserType() : "Unknown",
                deletedByUser != null ? deletedByUser.getUsername() : "Unknown",
                deletedByUserId,
                LocalDateTime.now().toString(),
                reason != null ? reason : "No reason provided"
        );

        // Log to application logs
        System.out.println(logMessage);

        // TODO: In production, this should write to:
        // 1. A dedicated audit log table (AuditLog entity as mentioned in Task 17)
        // 2. External audit system
        // 3. Compliance logging system

        // The audit log should include:
        // - Timestamp
        // - Action performed (USER_DELETION)
        // - User who performed the action
        // - Target user affected
        // - Reason provided
        // - Account context (for multi-tenant audit trails)
        // - IP address and session information
    }
}