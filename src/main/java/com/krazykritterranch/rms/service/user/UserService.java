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

    // ========== BASIC CRUD OPERATIONS ==========

    /**
     * Find user by ID with tenant security checks
     */
    public User findById(Long id) {
        if (id == null) {
            return null;
        }

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

    /**
     * Get all users (admin only)
     */
    @SecurityAnnotations.RequireAdmin
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by account with tenant security
     */
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

    /**
     * Save user with proper password encoding and security checks
     */
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

        // Set audit fields
        if (user.getId() == null) {
            user.setCreatedByUserId(tenantContext.getCurrentUserId());
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedByUserId(tenantContext.getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update user using DTO with security checks
     */
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
        existingUser.setUpdatedByUserId(tenantContext.getCurrentUserId());

        // Update type-specific fields based on user type
        updateUserTypeSpecificFields(existingUser, updateDto);

        return userRepository.save(existingUser);
    }

    /**
     * Soft delete user with security checks
     */
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
            if (user.getPrimaryAccount() == null ||
                    !user.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                throw new SecurityException("Cannot delete user from different account");
            }

            // Check if this would violate account user limits
            if (user.getUserType().equals("SUPER_ADMIN") || user.getUserType().equals("SUPPORT_ADMIN")) {
                throw new SecurityException("Cannot delete system administrators");
            }
        }

        // Soft delete - set inactive instead of hard delete
        user.softDelete(tenantContext.getCurrentUserId(), "Deleted by user");
        userRepository.save(user);
    }

    // ========== AUTHENTICATION AND USER LOOKUP ==========

    /**
     * Find user by username with roles (for authentication)
     */
    public User findByUsername(String username) {
        return userRepository.findByUsernameWithRoles(username).orElse(null);
    }

    /**
     * Find user by email with roles (for authentication)
     */
    public User findByEmail(String email) {
        return userRepository.findByEmailWithRoles(email).orElse(null);
    }

    /**
     * Find user by login (username or email)
     */
    public User findByLogin(String login) {
        // Try to find by username first, then by email
        User user = findByUsername(login);
        if (user == null) {
            user = findByEmail(login);
        }
        return user;
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if customer number exists for AccountUser entities
     */
    public boolean existsByCustomerNumber(String customerNumber) {
        if (customerNumber == null || customerNumber.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByCustomerNumber(customerNumber.trim());
    }

    // ========== ACCOUNT MANAGEMENT ==========

    /**
     * Check if account can add more users
     */
    @SecurityAnnotations.RequireAccountAccess
    public boolean canAddUserToAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .map(account -> {
                    long currentUserCount = userRepository.countByAccountId(accountId);
                    return currentUserCount < account.getMaxUsers();
                })
                .orElse(false);
    }

    /**
     * Get account user statistics
     */
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

    // ========== ROLE MANAGEMENT ==========

    /**
     * Assign role to user (admin only)
     */
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

    /**
     * Remove role from user (admin only)
     */
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

    // ========== USER REACTIVATION ==========

    /**
     * Reactivate soft-deleted user
     */
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

        user.reactivate(tenantContext.getCurrentUserId());
        return userRepository.save(user);
    }

    // ========== PASSWORD MANAGEMENT ==========

    /**
     * Update user password with security checks
     */
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
            if (user.getPrimaryAccount() == null ||
                    !user.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                throw new SecurityException("Cannot update password for user in different account");
            }

            // Account users cannot change system admin passwords
            if ((user.getUserType().equals("SUPER_ADMIN") || user.getUserType().equals("SUPPORT_ADMIN")) &&
                    !tenantContext.getCurrentUserId().equals(userId)) {
                throw new SecurityException("Cannot change administrator password");
            }
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedByUserId(tenantContext.getCurrentUserId());
        userRepository.save(user);
    }

    // ========== VETERINARIAN ACCESS CHECKS ==========

    /**
     * Check if veterinarian has access to specific account
     */
    public boolean hasVetPermissionForAccount(Long accountId) {
        if (!tenantContext.isVeterinarian()) {
            return false;
        }
        return vetPermissionRepository
                .findActivePermissionByVetAndAccount(tenantContext.getCurrentUserId(), accountId)
                .isPresent();
    }

    // ========== HELPER METHODS ==========

    /**
     * Check if password was changed during update
     */
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

    /**
     * Update user type-specific fields based on discriminator
     */
    private void updateUserTypeSpecificFields(User user, UserUpdateDTO updateDto) {
        switch (user.getUserType()) {
            case "ACCOUNT_USER":
                if (user instanceof AccountUser) {
                    updateAccountUserFields((AccountUser) user, updateDto);
                }
                break;

            case "SUPER_ADMIN":
                if (user instanceof SuperAdministrator) {
                    updateSuperAdministratorFields((SuperAdministrator) user, updateDto);
                }
                break;

            case "SUPPORT_ADMIN":
                if (user instanceof SupportAdministrator) {
                    updateSupportAdministratorFields((SupportAdministrator) user, updateDto);
                }
                break;

            case "VETERINARIAN":
                if (user instanceof Veterinarian) {
                    updateVeterinarianFields((Veterinarian) user, updateDto);
                }
                break;

            default:
                // Unknown user type, skip type-specific updates
                break;
        }
    }

    /**
     * Update AccountUser-specific fields
     */
    private void updateAccountUserFields(AccountUser accountUser, UserUpdateDTO updateDto) {
        if (updateDto.getCustomerNumber() != null && !updateDto.getCustomerNumber().trim().isEmpty()) {
            // Only admins can change customer numbers, or if it's empty/null currently
            if (tenantContext.isAdmin() || accountUser.getCustomerNumber() == null ||
                    accountUser.getCustomerNumber().trim().isEmpty()) {
                accountUser.setCustomerNumber(updateDto.getCustomerNumber().trim());
            }
        }

        if (updateDto.getEmployeeId() != null) {
            accountUser.setEmployeeId(updateDto.getEmployeeId().trim());
        }
    }

    /**
     * Update SuperAdministrator-specific fields
     */
    private void updateSuperAdministratorFields(SuperAdministrator admin, UserUpdateDTO updateDto) {
        // Only allow other admins to update admin-specific fields
        if (!tenantContext.isAdmin()) {
            return; // Skip admin field updates if current user is not admin
        }

        if (updateDto.getDepartment() != null) {
            admin.setDepartment(updateDto.getDepartment().trim());
        }

        // Note: SuperAdministrator doesn't have accessLevel field like the old Administrator
        // Specific admin fields would need to be added to DTO if needed
    }

    /**
     * Update SupportAdministrator-specific fields
     */
    private void updateSupportAdministratorFields(SupportAdministrator supportAdmin, UserUpdateDTO updateDto) {
        // Only allow other admins to update admin-specific fields
        if (!tenantContext.isAdmin()) {
            return; // Skip admin field updates if current user is not admin
        }

        if (updateDto.getDepartment() != null) {
            supportAdmin.setDepartment(updateDto.getDepartment().trim());
        }

        // Note: SupportAdministrator has different fields than old Administrator
        // Would need DTO updates for support-specific fields
    }

    /**
     * Update Veterinarian-specific fields
     */
    private void updateVeterinarianFields(Veterinarian vet, UserUpdateDTO updateDto) {
        // Veterinarians can update their own profile, admins can update any vet profile
        if (tenantContext.isVeterinarian()) {
            Long currentUserId = tenantContext.getCurrentUserId();
            if (!currentUserId.equals(vet.getId())) {
                throw new SecurityException("Veterinarians can only update their own profile");
            }
        }

        // Update veterinarian-specific fields using the correct DTO field names
        if (updateDto.getLicenseNumber() != null && !updateDto.getLicenseNumber().trim().isEmpty()) {
            vet.setVetLicenseNumber(updateDto.getLicenseNumber().trim());
        }

        if (updateDto.getSpecialization() != null) {
            vet.setSpecialization(updateDto.getSpecialization().trim());
        }

        if (updateDto.getClinicName() != null) {
            vet.setPracticeName(updateDto.getClinicName().trim());
        }

        if (updateDto.getClinicAddress() != null) {
            vet.setPracticeAddress(updateDto.getClinicAddress().trim());
        }

        if (updateDto.getYearsExperience() != null) {
            vet.setYearsExperience(updateDto.getYearsExperience());
        }
    }
}