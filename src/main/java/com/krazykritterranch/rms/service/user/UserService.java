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

    // Tenant-aware methods with proper filtering
    public List<User> getAllUsers() {
        if (tenantContext.isAdmin()) {
            // Administrators can see all users across all accounts
            return userRepository.findAll();
        } else if (tenantContext.isAccountUser()) {
            // Customer users can only see users within their own account
            return userRepository.findByAccountId(tenantContext.getCurrentAccountId());
        } else if (tenantContext.isVeterinarian()) {
            // Veterinarians can see users from accounts they have access to
            return userRepository.findByVeterinarianAccess(tenantContext.getCurrentUserId());
        } else {
            throw new SecurityException("Access denied: Unable to determine user context");
        }
    }

    @SecurityAnnotations.RequireAccountAccess
    public List<User> getUsersByAccount(Long accountId) {
        return userRepository.findByAccountId(accountId);
    }

    @SecurityAnnotations.RequireAdmin
    public List<User> getAllAdministrators() {
        return userRepository.findAdministrators();
    }

    public List<User> getAllVeterinarians() {
        return userRepository.findVeterinarians();
    }

    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        }

        User foundUser = user.get();

        // Security check based on tenant context
        if (tenantContext.isAdmin()) {
            return foundUser; // Admins can see any user
        } else if (tenantContext.isAccountUser()) {
            // Account users can only see users in their account
            if (foundUser.getPrimaryAccount() != null &&
                    foundUser.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                return foundUser;
            }
        } else if (tenantContext.isVeterinarian()) {
            // Vets can see users in accounts they have access to
            if (foundUser.getPrimaryAccount() != null) {
                // Check if veterinarian has access to this user's account
                return hasVetPermissionForAccount(foundUser.getPrimaryAccount().getId()) ? foundUser : null;
            }
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

        // Update common fields (only if provided in DTO)
        if (updateDto.getUsername() != null && !updateDto.getUsername().trim().isEmpty()) {
            existingUser.setUsername(updateDto.getUsername().trim());
        }

        if (updateDto.getEmail() != null && !updateDto.getEmail().trim().isEmpty()) {
            existingUser.setEmail(updateDto.getEmail().trim());
        }

        if (updateDto.getFirstName() != null) {
            existingUser.setFirstName(updateDto.getFirstName().trim());
        }

        if (updateDto.getLastName() != null) {
            existingUser.setLastName(updateDto.getLastName().trim());
        }

        if (updateDto.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updateDto.getPhoneNumber().trim());
        }

        // Only admins or the user themselves can change active status
        if (updateDto.getIsActive() != null &&
                (tenantContext.isAdmin() || existingUser.getId().equals(tenantContext.getCurrentUserId()))) {
            existingUser.setIsActive(updateDto.getIsActive());
        }

        // Handle password change if provided
        if (updateDto.getNewPassword() != null && !updateDto.getNewPassword().trim().isEmpty()) {
            // Users can change their own password, or admins can change any password
            if (tenantContext.isAdmin() || existingUser.getId().equals(tenantContext.getCurrentUserId())) {
                existingUser.setPassword(passwordEncoder.encode(updateDto.getNewPassword()));
            } else {
                throw new SecurityException("You can only change your own password");
            }
        }

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

    public void assignRole(Long userId, String roleName) {
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
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.getRoles().removeIf(role -> role.getName().equals(roleName));
        userRepository.save(user);
    }

    // Helper methods
    private boolean isPasswordChanged(User user) {
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


    // Helper method for updating Customer-specific fields
    private void updateCustomerFields(Customer customer, UserUpdateDTO updateDto) {
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
    private void updateAdministratorFields(Administrator admin, UserUpdateDTO updateDto) {
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
    private void updateVeterinarianFields(Veterinarian vet, UserUpdateDTO updateDto) {
        if (updateDto.getLicenseNumber() != null) {
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

    // Add this method to UserService.java if it doesn't exist
    public boolean hasVetPermissionForAccount(Long accountId) {
        if (!tenantContext.isVeterinarian()) {
            return false;
        }

        // This should check the VetPermission table
        // Implementation depends on your existing vet permission logic
        // For now, returning false as placeholder
        return false; // TODO: Implement actual vet permission check
    }


    /**
     * Validates if a user can be deleted by checking for associated data
     */
    public String validateUserDeletion(Long userId) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user has livestock data (prevents deletion)
        if (hasLivestockData(userId)) {
            return "Cannot delete user: User has associated livestock data. Please transfer or remove livestock first.";
        }

        // Check if user has veterinary permissions granted to others
        if (hasVetPermissions(userId)) {
            return "Cannot delete user: User has veterinary permissions. Please revoke permissions first.";
        }

        // Check if user has pending veterinary appointments
        if (hasPendingAppointments(userId)) {
            return "Cannot delete user: User has pending appointments. Please complete or cancel appointments first.";
        }

        // Check if user is the primary account owner
        if (isPrimaryAccountOwner(userId)) {
            return "Cannot delete user: User is the primary account owner. Please transfer ownership first.";
        }

        // Check if this is the last active admin (prevent system lockout)
        if (isLastActiveAdmin(userId)) {
            return "Cannot delete user: This is the last active administrator. System must have at least one active admin.";
        }

        return null; // No validation errors
    }

    /**
     * Gets information about what would be affected by deleting this user
     */
    public Map<String, Object> getUserDeletionInfo(Long userId) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("username", user.getUsername());
        info.put("userType", user.getUserType());
        info.put("isActive", user.getIsActive());
        info.put("canDelete", validateUserDeletion(userId) == null);
        info.put("validationError", validateUserDeletion(userId));

        // Count associated data
        Map<String, Integer> associatedData = new HashMap<>();
        associatedData.put("livestockCount", getLivestockCount(userId));
        associatedData.put("vetPermissionsCount", getVetPermissionCount(userId));
        associatedData.put("appointmentsCount", getPendingAppointmentsCount(userId));

        info.put("associatedData", associatedData);

        // Deletion recommendations
        if (validateUserDeletion(userId) != null) {
            info.put("recommendations", getDeletionRecommendations(userId));
        }

        return info;
    }

    /**
     * Permanently deletes a user and all associated data
     */
    @Transactional
    public void permanentlyDeleteUser(Long userId) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Final validation before deletion
        String validationError = validateUserDeletion(userId);
        if (validationError != null) {
            throw new IllegalStateException("Cannot delete user: " + validationError);
        }

        try {
            // Remove user from roles
            user.getRoles().clear();
            userRepository.save(user);

            // Remove any veterinary permissions related to this user
            removeAllVetPermissions(userId);

            // Remove user from any shared access permissions
            removeSharedAccessPermissions(userId);

            // Archive any audit logs (don't delete for compliance)
            archiveUserAuditLogs(userId);

            // Finally delete the user record
            userRepository.deleteById(userId);

            System.out.println("User permanently deleted: " + user.getUsername() + " (ID: " + userId + ")");

        } catch (Exception e) {
            System.err.println("Error during permanent user deletion: " + e.getMessage());
            throw new RuntimeException("Failed to permanently delete user", e);
        }
    }

    /**
     * Enhanced deactivate user with better logging
     */

    public void deactivateUser(Long id) {
        User user = findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("User is already inactive");
        }

        user.setIsActive(false);
        user.setDeactivatedAt(LocalDateTime.now());
        user.setDeactivatedBy(tenantContext.getCurrentUserId());

        userRepository.save(user);

        System.out.println("User deactivated: " + user.getUsername() + " (ID: " + id + ")");
    }

    /**
     * Enhanced reactivate user with validation
     */

    public void reactivateUser(Long id) {
        User user = findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (user.getIsActive()) {
            throw new IllegalArgumentException("User is already active");
        }

        user.setIsActive(true);
        user.setDeactivatedAt(null);
        user.setDeactivatedBy(null);
        user.setReactivatedAt(LocalDateTime.now());
        user.setReactivatedBy(tenantContext.getCurrentUserId());

        userRepository.save(user);

        System.out.println("User reactivated: " + user.getUsername() + " (ID: " + id + ")");
    }

    /**
     * Logs user deletion for audit purposes
     */
    public void logUserDeletion(Long userId, String reason, Long deletedBy) {
        try {
            User user = findById(userId);
            if (user != null) {
                // Create audit log entry
                Map<String, Object> auditData = new HashMap<>();
                auditData.put("action", "USER_PERMANENT_DELETE");
                auditData.put("deletedUserId", userId);
                auditData.put("deletedUsername", user.getUsername());
                auditData.put("deletedUserType", user.getUserType());
                auditData.put("reason", reason);
                auditData.put("deletedBy", deletedBy);
                auditData.put("deletedAt", LocalDateTime.now());

                // Log to system (you can implement proper audit logging here)
                System.out.println("AUDIT: User deletion logged: " + auditData);

                // You could save this to an audit_log table if you have one
                // auditLogService.log(auditData);
            }
        } catch (Exception e) {
            System.err.println("Failed to log user deletion: " + e.getMessage());
            // Don't fail the deletion if logging fails
        }
    }

// Helper methods for validation

    private boolean hasVetPermissions(Long userId) {
        // Check if user is a veterinarian with permissions or has granted permissions to vets
        if (tenantContext.isVeterinarian() && userId.equals(tenantContext.getCurrentUserId())) {
            return vetPermissionRepository.findActivePermissionsByVet(userId, LocalDateTime.now()).size() > 0;
        }
        return false;
    }

    private int getVetPermissionCount(Long userId) {
        // Count vet permissions related to this user
        return (int) vetPermissionRepository.findActivePermissionsByVet(userId, LocalDateTime.now()).size();
    }

    private void removeAllVetPermissions(Long userId) {
        // Remove vet permissions where this user is involved as a veterinarian
        List<VetPermission> permissions = vetPermissionRepository.findActivePermissionsByVet(userId, LocalDateTime.now());
        for (VetPermission permission : permissions) {
            permission.setIsActive(false);
            vetPermissionRepository.save(permission);
        }
    }



    private boolean isPrimaryAccountOwner(Long userId) {
        // Check if user is the primary owner of an account
        if (tenantContext.getCurrentAccountId() != null) {
            return accountRepository.findById(tenantContext.getCurrentAccountId())
                    .map(account -> account.getMasterUser() != null && account.getMasterUser().getId().equals(userId))
                    .orElse(false);
        }
        return false;
    }



    private int getLivestockCount(Long userId) {
        // Return count of livestock associated with user
        return 0; // Placeholder
    }

    private int getPendingAppointmentsCount(Long userId) {
        // Return count of pending appointments
        return 0; // Placeholder
    }

    private java.util.List<String> getDeletionRecommendations(Long userId) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        if (hasLivestockData(userId)) {
            recommendations.add("Transfer or archive livestock data before deletion");
        }

        if (hasVetPermissions(userId)) {
            recommendations.add("Revoke all veterinary permissions before deletion");
        }

        if (hasPendingAppointments(userId)) {
            recommendations.add("Complete or cancel all pending appointments");
        }

        if (isPrimaryAccountOwner(userId)) {
            recommendations.add("Transfer account ownership to another user");
        }

        return recommendations;
    }

    private boolean hasLivestockData(Long userId) {
        // TODO: Implement livestock data check
        // This should check if user has any livestock records
        return false; // Placeholder
    }

    private boolean hasPendingAppointments(Long userId) {
        // TODO: Implement pending appointments check
        // This should check if user has any pending vet appointments
        return false; // Placeholder
    }

    private boolean isLastActiveAdmin(Long userId) {
        // Check if this is the last active administrator
        long activeAdminCount = userRepository.countActiveAdministrators();
        User user = findById(userId);
        return user != null && user.getUserType().equals("ADMINISTRATOR") && activeAdminCount <= 1;
    }

    private void removeSharedAccessPermissions(Long userId) {
        // Remove any shared access permissions this user might have granted
        // TODO: Implement when shared access feature is built
    }

    private void archiveUserAuditLogs(Long userId) {
        // Archive audit logs for compliance
        // TODO: Implement when audit logging system is built
    }



}