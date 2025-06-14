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
}