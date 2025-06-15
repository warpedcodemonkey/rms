package com.krazykritterranch.rms.controller.user;

import com.krazykritterranch.rms.controller.user.dto.UserCreationDTO;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.AccountUser; // Add this import
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

    // REMOVED: CustomerRepository - this was causing compilation error
    // Customer numbers are handled in AccountUser entity

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
                // Account users can only create ACCOUNT_USER users
                if (!"ACCOUNT_USER".equals(userDto.getUserType())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Collections.singletonMap("error", "Account users can only create account users"));
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

            // FIXED: Check customer number for AccountUser type using proper entity
            if ("ACCOUNT_USER".equals(userDto.getUserType()) &&
                    userDto.getCustomerNumber() != null &&
                    !userDto.getCustomerNumber().trim().isEmpty()) {

                // Check if customer number already exists for AccountUser entities
                if (userService.existsByCustomerNumber(userDto.getCustomerNumber().trim())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Collections.singletonMap("error", "Customer number already exists"));
                }
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

            // FIXED: Check for customer number conflicts using proper method
            if ("ACCOUNT_USER".equals(existingUser.getUserType()) &&
                    userUpdateDto.getCustomerNumber() != null &&
                    userUpdateDto.getCustomerNumber().trim().length() > 0) {

                AccountUser accountUser = (AccountUser) existingUser;
                if (!userUpdateDto.getCustomerNumber().trim().equals(accountUser.getCustomerNumber()) &&
                        userService.existsByCustomerNumber(userUpdateDto.getCustomerNumber().trim())) {
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

    // Rest of the methods remain the same...
    // (continuing with existing methods for brevity)

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

    // ... (rest of existing methods remain the same)
}