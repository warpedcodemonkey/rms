package com.krazykritterranch.rms.controller.user;

import com.krazykritterranch.rms.controller.user.dto.UserCreationDTO;
import com.krazykritterranch.rms.controller.user.dto.UserUpdateDTO;
import com.krazykritterranch.rms.controller.user.dto.UserResponseDTO;
import com.krazykritterranch.rms.controller.user.dto.LoginRequest;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.AccountUser;
import com.krazykritterranch.rms.service.common.AccountService;
import com.krazykritterranch.rms.service.user.UserFactory;
import com.krazykritterranch.rms.service.user.UserService;
import com.krazykritterranch.rms.service.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TenantContext tenantContext;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private AccountService accountService;

    // ========== BASIC CRUD ENDPOINTS ==========

    /**
     * GET /api/user - Get all users (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPPORT_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponseDTO> userDTOs = users.stream()
                    .map(UserResponseDTO::fromUser)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * GET /api/user/account/{accountId} - Get users by account
     */
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPPORT_ADMIN') or hasRole('ACCOUNT_USER')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByAccount(@PathVariable Long accountId) {
        try {
            List<User> users = userService.getUsersByAccount(accountId);
            List<UserResponseDTO> userDTOs = users.stream()
                    .map(UserResponseDTO::fromUser)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        } catch (Exception e) {
            System.out.println("Error getting users by account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * GET /api/user/me - Get current authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                if (user != null) {
                    UserResponseDTO responseDTO = UserResponseDTO.fromUser(user);
                    return ResponseEntity.ok(responseDTO);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            System.out.println("Error getting current user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/user/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            if (user != null) {
                UserResponseDTO responseDTO = UserResponseDTO.fromUser(user);
                return ResponseEntity.ok(responseDTO);
            }
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.out.println("Error getting user by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/user - Create new user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreationDTO userDto) {
        try {
            // Restrict user types based on current user context
            if (tenantContext.isAccountUser()) {
                // Account users can only create ACCOUNT_USER users
                if (!"ACCOUNT_USER".equals(userDto.getUserType())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Collections.singletonMap("error", "Account users can only create account users"));
                }
            }

            // Validate user input
            Map<String, String> validationErrors = validateUserCreation(userDto);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("validationErrors", validationErrors));
            }

            // Create user based on type
            User user = userFactory.createUser(userDto);

            // Handle account assignment based on user type and context
            if ("ACCOUNT_USER".equals(userDto.getUserType())) {
                if (tenantContext.isAdmin()) {
                    // Admin creating account user - no account assignment needed for now
                    // Account user will be assigned to account when account is created
                } else if (tenantContext.isAccountUser()) {
                    // Account user creating another user in their account
                    Long accountId = tenantContext.getCurrentAccountId();

                    // Check if account can add more users
                    if (!userService.canAddUserToAccount(accountId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Collections.singletonMap("error", "Account has reached maximum user limit"));
                    }

                    // Set the account
                    accountService.findById(accountId).ifPresent(user::setPrimaryAccount);
                }
            }
            // SuperAdministrator, SupportAdministrator, and Veterinarian are system users (no primary account)

            User savedUser = userService.saveUser(user);
            UserResponseDTO responseDTO = UserResponseDTO.fromUser(savedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to create user"));
        }
    }

    /**
     * PUT /api/user/{id} - Update existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDto) {
        try {
            // Validate that the user exists and can be updated by the current user
            User existingUser = userService.findById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            // Security check: ensure current user can update this user
            if (!canUserBeUpdatedByCurrentUser(existingUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Access denied: You cannot update this user"));
            }

            // Validate update input
            Map<String, String> validationErrors = validateUserUpdate(id, userUpdateDto, existingUser);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("validationErrors", validationErrors));
            }

            // Update the user
            User updatedUser = userService.updateUserWithDto(id, userUpdateDto);
            UserResponseDTO responseDTO = UserResponseDTO.fromUser(updatedUser);

            return ResponseEntity.ok(responseDTO);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to update user"));
        }
    }

    /**
     * DELETE /api/user/{id} - Soft delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // Validate that the user exists and can be deleted by the current user
            User existingUser = userService.findById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            // Security check: ensure current user can delete this user
            if (!canUserBeDeletedByCurrentUser(existingUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Access denied: You cannot delete this user"));
            }

            // Perform soft delete
            userService.deleteUser(id);

            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to delete user"));
        }
    }

    // ========== AUTHENTICATION ENDPOINTS ==========

    /**
     * POST /api/user/login - User authentication
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = userService.findByLogin(loginRequest.getUsername());
            if (user != null) {
                UserResponseDTO responseDTO = UserResponseDTO.fromUser(user);

                Map<String, Object> response = new HashMap<>();
                response.put("user", responseDTO);
                response.put("message", "Login successful");

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Authentication failed"));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Login failed"));
        }
    }

    // ========== USER MANAGEMENT ENDPOINTS ==========

    /**
     * POST /api/user/{id}/reactivate - Reactivate soft-deleted user
     */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPPORT_ADMIN') or hasRole('ACCOUNT_USER')")
    public ResponseEntity<?> reactivateUser(@PathVariable Long id) {
        try {
            User reactivatedUser = userService.reactivateUser(id);
            UserResponseDTO responseDTO = UserResponseDTO.fromUser(reactivatedUser);

            return ResponseEntity.ok(responseDTO);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error reactivating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to reactivate user"));
        }
    }

    /**
     * PUT /api/user/{id}/password - Update user password
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> passwordData) {
        try {
            String newPassword = passwordData.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "New password is required"));
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Password must be at least 6 characters"));
            }

            userService.updateUserPassword(id, newPassword);

            return ResponseEntity.ok(Collections.singletonMap("message", "Password updated successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to update password"));
        }
    }

    /**
     * POST /api/user/{userId}/roles/{roleName} - Assign role to user
     */
    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> assignRole(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            userService.assignRole(userId, roleName);
            return ResponseEntity.ok(Collections.singletonMap("message", "Role assigned successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error assigning role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to assign role"));
        }
    }

    /**
     * DELETE /api/user/{userId}/roles/{roleName} - Remove role from user
     */
    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> removeRole(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            userService.removeRole(userId, roleName);
            return ResponseEntity.ok(Collections.singletonMap("message", "Role removed successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error removing role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to remove role"));
        }
    }

    // ========== ACCOUNT STATISTICS ENDPOINTS ==========

    /**
     * GET /api/user/account/{accountId}/stats - Get account user statistics
     */
    @GetMapping("/account/{accountId}/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPPORT_ADMIN') or hasRole('ACCOUNT_USER')")
    public ResponseEntity<?> getAccountUserStats(@PathVariable Long accountId) {
        try {
            Map<String, Object> stats = userService.getAccountUserStats(accountId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.out.println("Error getting account user stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to get account statistics"));
        }
    }

    // ========== VALIDATION HELPER METHODS ==========

    /**
     * Validate user creation input
     */
    private Map<String, String> validateUserCreation(UserCreationDTO userDto) {
        Map<String, String> errors = new HashMap<>();

        // Check if username already exists
        if (userService.existsByUsername(userDto.getUsername())) {
            errors.put("username", "Username already exists");
        }

        // Check if email already exists
        if (userService.existsByEmail(userDto.getEmail())) {
            errors.put("email", "Email already exists");
        }

        // Check customer number for AccountUser type
        if ("ACCOUNT_USER".equals(userDto.getUserType()) &&
                userDto.getCustomerNumber() != null &&
                !userDto.getCustomerNumber().trim().isEmpty()) {

            if (userService.existsByCustomerNumber(userDto.getCustomerNumber().trim())) {
                errors.put("customerNumber", "Customer number already exists");
            }
        }

        return errors;
    }

    /**
     * Validate user update input
     */
    private Map<String, String> validateUserUpdate(Long userId, UserUpdateDTO updateDto, User existingUser) {
        Map<String, String> errors = new HashMap<>();

        // Check for username conflicts (only if username is being changed)
        if (updateDto.getUsername() != null &&
                !updateDto.getUsername().equals(existingUser.getUsername()) &&
                userService.existsByUsername(updateDto.getUsername())) {
            errors.put("username", "Username already exists");
        }

        // Check for email conflicts (only if email is being changed)
        if (updateDto.getEmail() != null &&
                !updateDto.getEmail().equals(existingUser.getEmail()) &&
                userService.existsByEmail(updateDto.getEmail())) {
            errors.put("email", "Email already exists");
        }

        // Check for customer number conflicts for AccountUser
        if ("ACCOUNT_USER".equals(existingUser.getUserType()) &&
                updateDto.getCustomerNumber() != null &&
                updateDto.getCustomerNumber().trim().length() > 0) {

            AccountUser accountUser = (AccountUser) existingUser;
            if (!updateDto.getCustomerNumber().trim().equals(accountUser.getCustomerNumber()) &&
                    userService.existsByCustomerNumber(updateDto.getCustomerNumber().trim())) {
                errors.put("customerNumber", "Customer number already exists");
            }
        }

        return errors;
    }

    // ========== SECURITY HELPER METHODS ==========

    /**
     * Check if user can be updated by current user
     */
    private boolean canUserBeUpdatedByCurrentUser(User targetUser) {
        if (tenantContext.isAdmin()) {
            return true; // Admins can update any user
        }

        if (tenantContext.isAccountUser()) {
            // Account users can update users in their own account
            if (targetUser.getPrimaryAccount() != null &&
                    targetUser.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                return true;
            }

            // Users can always update their own profile
            if (targetUser.getId().equals(tenantContext.getCurrentUserId())) {
                return true;
            }
        }

        if (tenantContext.isVeterinarian()) {
            // Vets can update users in accounts they have access to
            if (targetUser.getPrimaryAccount() != null) {
                return userService.hasVetPermissionForAccount(targetUser.getPrimaryAccount().getId());
            }

            // Vets can update their own profile
            if (targetUser.getId().equals(tenantContext.getCurrentUserId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if user can be deleted by current user
     */
    private boolean canUserBeDeletedByCurrentUser(User targetUser) {
        // Veterinarians cannot delete users
        if (tenantContext.isVeterinarian()) {
            return false;
        }

        if (tenantContext.isAdmin()) {
            // Admins can delete any user except themselves
            return !targetUser.getId().equals(tenantContext.getCurrentUserId());
        }

        if (tenantContext.isAccountUser()) {
            // Account users can delete users in their own account
            if (targetUser.getPrimaryAccount() != null &&
                    targetUser.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {

                // Cannot delete system administrators
                if (targetUser.getUserType().equals("SUPER_ADMIN") ||
                        targetUser.getUserType().equals("SUPPORT_ADMIN")) {
                    return false;
                }

                // Cannot delete themselves
                if (targetUser.getId().equals(tenantContext.getCurrentUserId())) {
                    return false;
                }

                return true;
            }
        }

        return false;
    }
}