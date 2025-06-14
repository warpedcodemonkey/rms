package com.krazykritterranch.rms.controller.user;

import com.krazykritterranch.rms.controller.user.dto.UserCreationDTO;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.service.common.AccountService;
import com.krazykritterranch.rms.service.user.UserFactory;
import com.krazykritterranch.rms.service.user.UserService;
import com.krazykritterranch.rms.service.security.TenantContext;
import com.krazykritterranch.rms.controller.user.dto.LoginRequest;
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
import com.krazykritterranch.rms.controller.user.dto.UserResponseDTO;

import java.util.stream.Collectors;
import com.krazykritterranch.rms.controller.user.dto.UserUpdateDTO;

import com.krazykritterranch.rms.controller.user.dto.UserDeleteRequest;

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

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponseDTO> userDTOs = users.stream()
                    .map(UserResponseDTO::fromUser)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user != null) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return user != null ?
                    ResponseEntity.ok(user) :
                    ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.out.println("Error getting user by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreationDTO userDto) {
        try {
            // Restrict user types based on current user context
            if (tenantContext.isAccountUser()) {
                // Account users can only create CUSTOMER users
                if (!"CUSTOMER".equals(userDto.getUserType())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Collections.singletonMap("error", "Account users can only create customer users"));
                }
            }

            // Check if user already exists
            if (userService.existsByUsername(userDto.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", "Username already exists"));
            }

            if (userService.existsByEmail(userDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", "Email already exists"));
            }

            if ("CUSTOMER".equals(userDto.getUserType()) &&
                    userDto.getCustomerNumber() != null &&
                    !userDto.getCustomerNumber().trim().isEmpty()) {

                // Check if customer number already exists
                if (customerRepository.existsByCustomerNumber(userDto.getCustomerNumber().trim())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Collections.singletonMap("error", "Customer number already exists"));
                }
            }

            // Create user based on type
            User user = userFactory.createUser(userDto);

            // Handle account assignment based on user type and context
            if ("CUSTOMER".equals(userDto.getUserType())) {
                if (tenantContext.isAdmin()) {
                    // Admin creating customer - no account assignment needed for now
                    // Customer will be assigned to account when account is created
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
            // Administrators and Veterinarians are system users (no primary account)

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


    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDto) {
        try {
            // Validate that the user can be updated by the current user
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

            // Check for username conflicts (only if username is being changed)
            if (userUpdateDto.getUsername() != null &&
                    !userUpdateDto.getUsername().equals(existingUser.getUsername()) &&
                    userService.existsByUsername(userUpdateDto.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", "Username already exists"));
            }

            // Check for email conflicts (only if email is being changed)
            if (userUpdateDto.getEmail() != null &&
                    !userUpdateDto.getEmail().equals(existingUser.getEmail()) &&
                    userService.existsByEmail(userUpdateDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", "Email already exists"));
            }

            // Check for customer number conflicts (only for customers and if being changed)
            if ("CUSTOMER".equals(existingUser.getUserType()) &&
                    userUpdateDto.getCustomerNumber() != null &&
                    userUpdateDto.getCustomerNumber().trim().length() > 0) {

                Customer customer = (Customer) existingUser;
                if (!userUpdateDto.getCustomerNumber().trim().equals(customer.getCustomerNumber()) &&
                        customerRepository.existsByCustomerNumber(userUpdateDto.getCustomerNumber().trim())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Collections.singletonMap("error", "Customer number already exists"));
                }
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
                // This would need to check vet permissions - placeholder for now
                return userService.hasVetPermissionForAccount(targetUser.getPrimaryAccount().getId());
            }

            // Vets can update their own profile
            if (targetUser.getId().equals(tenantContext.getCurrentUserId())) {
                return true;
            }
        }

        return false;
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean permanent) {
        try {
            // Get the user to be deleted
            User userToDelete = userService.findById(id);
            if (userToDelete == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            // Security check: ensure current user can delete this user
            if (!canUserBeDeletedByCurrentUser(userToDelete)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Access denied: You cannot delete this user"));
            }

            // Prevent users from deleting themselves
            if (userToDelete.getId().equals(tenantContext.getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "You cannot delete your own account"));
            }

            // Check if user has associated data that prevents deletion
            String validationError = userService.validateUserDeletion(id);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", validationError));
            }

            if (permanent) {
                // Hard delete - only allowed for admins
                if (!tenantContext.isAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Collections.singletonMap("error", "Only administrators can permanently delete users"));
                }
                userService.permanentlyDeleteUser(id);
            } else {
                // Soft delete (deactivate)
                userService.deactivateUser(id);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", permanent ? "User permanently deleted" : "User deactivated");
            response.put("permanent", permanent);

            return ResponseEntity.ok(response);

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

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> permanentlyDeleteUser(@PathVariable Long id, @Valid @RequestBody UserDeleteRequest deleteRequest) {
        try {
            // Get the user to be deleted
            User userToDelete = userService.findById(id);
            if (userToDelete == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            // Verify confirmation
            if (!deleteRequest.isConfirmed()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Deletion must be confirmed"));
            }

            // Verify reason is provided for permanent deletion
            if (deleteRequest.getReason() == null || deleteRequest.getReason().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Reason is required for permanent deletion"));
            }

            // Prevent admin from deleting themselves
            if (userToDelete.getId().equals(tenantContext.getCurrentUserId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "You cannot delete your own account"));
            }

            // Check if user has associated data that prevents deletion
            String validationError = userService.validateUserDeletion(id);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("error", validationError));
            }

            // Log the deletion for audit purposes
            userService.logUserDeletion(id, deleteRequest.getReason(), tenantContext.getCurrentUserId());

            // Perform permanent deletion
            userService.permanentlyDeleteUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User permanently deleted");
            response.put("deletedUserId", id);
            response.put("reason", deleteRequest.getReason());

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error permanently deleting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to permanently delete user"));
        }
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable Long id) {
        try {
            // Get the user to be reactivated
            User userToReactivate = userService.findById(id);
            if (userToReactivate == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            // Security check: ensure current user can reactivate this user
            if (!canUserBeReactivatedByCurrentUser(userToReactivate)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Access denied: You cannot reactivate this user"));
            }

            // Check if user is already active
            if (userToReactivate.getIsActive()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "User is already active"));
            }

            // Check account limits if reactivating account user
            if ("CUSTOMER".equals(userToReactivate.getUserType()) &&
                    userToReactivate.getPrimaryAccount() != null) {
                if (!userService.canAddUserToAccount(userToReactivate.getPrimaryAccount().getId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Collections.singletonMap("error", "Account has reached maximum user limit"));
                }
            }

            userService.reactivateUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User reactivated successfully");
            response.put("userId", id);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error reactivating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to reactivate user"));
        }
    }

    @GetMapping("/{id}/deletion-check")
    public ResponseEntity<?> checkUserDeletion(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            // Security check
            if (!canUserBeDeletedByCurrentUser(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Access denied"));
            }

            Map<String, Object> deletionInfo = userService.getUserDeletionInfo(id);
            return ResponseEntity.ok(deletionInfo);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access denied"));
        } catch (Exception e) {
            System.out.println("Error checking user deletion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to check user deletion status"));
        }
    }

    // Helper methods for UserController
    private boolean canUserBeDeletedByCurrentUser(User targetUser) {
        if (tenantContext.isAdmin()) {
            return true; // Admins can delete any user (except themselves)
        }

        if (tenantContext.isAccountUser()) {
            // Account users can delete users in their own account (but not themselves)
            if (targetUser.getPrimaryAccount() != null &&
                    targetUser.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId()) &&
                    !targetUser.getId().equals(tenantContext.getCurrentUserId())) {
                return true;
            }
        }

        // Vets cannot delete users
        return false;
    }

    private boolean canUserBeReactivatedByCurrentUser(User targetUser) {
        if (tenantContext.isAdmin()) {
            return true; // Admins can reactivate any user
        }

        if (tenantContext.isAccountUser()) {
            // Account users can reactivate users in their own account
            if (targetUser.getPrimaryAccount() != null &&
                    targetUser.getPrimaryAccount().getId().equals(tenantContext.getCurrentAccountId())) {
                return true;
            }
        }

        // Vets cannot reactivate users
        return false;
    }

    // Role management
    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRole(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            userService.assignRole(userId, roleName);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.out.println("Error assigning role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRole(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            userService.removeRole(userId, roleName);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.out.println("Error removing role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Check if account can add more users
    @GetMapping("/account/{accountId}/can-add-user")
    @PreAuthorize("@securityService.canAccessAccount(#accountId)")
    public ResponseEntity<Boolean> canAddUserToAccount(@PathVariable Long accountId) {
        try {
            boolean canAdd = userService.canAddUserToAccount(accountId);
            return ResponseEntity.ok(canAdd);
        } catch (Exception e) {
            System.out.println("Error checking user limit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            System.out.println("Login attempt for username: " + loginRequest.getUsername());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = userService.findByUsername(loginRequest.getUsername());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "User not found"));
            }

            // Set tenant context for this session
            tenantContext.setCurrentUserId(user.getId());
            tenantContext.setUserType(user.getUserType());
            if (user.getPrimaryAccount() != null) {
                tenantContext.setCurrentAccountId(user.getPrimaryAccount().getId());
            }

            // Return user info including roles
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("userType", user.getUserType());
            response.put("isAdmin", user.isAdministrator());
            response.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());

            // Add account info if applicable
            if (user.getPrimaryAccount() != null) {
                response.put("accountId", user.getPrimaryAccount().getId());
                response.put("accountName", user.getPrimaryAccount().getFarmName());
            }

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials for username: " + loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Internal server error"));
        }
    }

    @PutMapping("/account/{accountId}/user-limit")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> setAccountUserLimit(@PathVariable Long accountId, @RequestParam Integer maxUsers) {
        try {
            if (maxUsers < 1 || maxUsers > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "User limit must be between 1 and 100"));
            }

            return accountService.findById(accountId)
                    .map(account -> {
                        account.setMaxUsers(maxUsers);
                        accountService.save(account);
                        return ResponseEntity.ok(Collections.singletonMap("message", "User limit updated successfully"));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Collections.singletonMap("error", "Account not found")));

        } catch (Exception e) {
            System.out.println("Error setting user limit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to update user limit"));
        }
    }

}