package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.model.user.Role;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.repositories.user.RoleRepository;
import com.krazykritterranch.rms.repositories.user.UserRepository;
import com.krazykritterranch.rms.service.security.SecurityAnnotations;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.krazykritterranch.rms.repositories.common.AccountRepository;

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

    public User updateUser(Long id, User updatedUser) {
        User existingUser = findById(id); // This already includes security checks
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Update fields (don't update password here unless specifically requested)
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());

        // Only allow admin users to change account assignments
        if (tenantContext.isAdmin() && updatedUser.getPrimaryAccount() != null) {
            existingUser.setPrimaryAccount(updatedUser.getPrimaryAccount());
        }

        return userRepository.save(existingUser);
    }

    public void deactivateUser(Long id) {
        User user = findById(id); // This already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setIsActive(false);
        userRepository.save(user);
    }

    public void reactivateUser(Long id) {
        User user = findById(id); // This already includes security checks
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        user.setIsActive(true);
        userRepository.save(user);
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

    private boolean hasVetPermissionForAccount(Long accountId) {
        // This would need to check the VetPermissionRepository
        // For now, returning true as a placeholder
        // TODO: Implement proper vet permission checking
        return true;
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


}