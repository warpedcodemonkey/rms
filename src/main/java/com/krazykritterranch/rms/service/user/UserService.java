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

import java.util.List;
import java.util.Optional;

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

    // Public methods (no tenant restriction)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
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

    // Tenant-aware methods
    public List<User> getAllUsers() {
//        if (tenantContext.isAdmin()) {
//            return userRepository.findAll();
//        } else if (tenantContext.isAccountUser()) {
//            return userRepository.findByAccountId(tenantContext.getCurrentAccountId());
//        } else {
//            throw new SecurityException("Access denied");
//        }
        return userRepository.findAll();
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

        // Security check
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
            // This would need additional logic to check vet permissions
            return foundUser; // Simplified for now
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
        long currentUserCount = userRepository.countActiveUsersByAccount(accountId);
        return currentUserCount < 5; // Max 5 users per account
    }

    public void deactivateUser(Long userId) {
        User user = findById(userId); // This includes security checks
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void reactivateUser(Long userId) {
        User user = findById(userId); // This includes security checks
        user.setIsActive(true);
        userRepository.save(user);
    }

    private boolean isPasswordChanged(User user) {
        if (user.getId() == null) return true;

        User existingUser = userRepository.findById(user.getId()).orElse(null);
        if (existingUser == null) return true;

        return !existingUser.getPassword().equals(user.getPassword());
    }

    // Role management
    public void assignRole(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        user.getRoles().add(role);
        userRepository.save(user);
    }

    public void removeRole(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        user.getRoles().remove(role);
        userRepository.save(user);
    }
}