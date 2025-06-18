package com.krazykritterranch.rms.controller.api;

import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.FarmUser;
import com.krazykritterranch.rms.model.user.PlatformUser;
import com.krazykritterranch.rms.model.user.Veterinarian;
import com.krazykritterranch.rms.model.user.dto.LoginRequest;
import com.krazykritterranch.rms.model.user.dto.FarmUserRegistrationRequest;
import com.krazykritterranch.rms.model.user.dto.VeterinarianRegistrationRequest;
import com.krazykritterranch.rms.service.user.UserService;
import com.krazykritterranch.rms.service.user.FarmUserService;
import com.krazykritterranch.rms.service.user.PlatformUserService;
import com.krazykritterranch.rms.service.user.VeterinarianService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations.
 *
 * Handles user registration, login, and authentication for all user types.
 * Part of Phase 1.1 - Enhanced User System & Authentication API implementation.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    private final UserService userService;
    private final FarmUserService farmUserService;
    private final PlatformUserService platformUserService;
    private final VeterinarianService veterinarianService;

    @Autowired
    public AuthenticationController(UserService userService,
                                    FarmUserService farmUserService,
                                    PlatformUserService platformUserService,
                                    VeterinarianService veterinarianService) {
        this.userService = userService;
        this.farmUserService = farmUserService;
        this.platformUserService = platformUserService;
        this.veterinarianService = veterinarianService;
    }

    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createValidationErrorResponse(bindingResult));
            }

            // Find user by email
            User user = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new AuthenticationException("Invalid credentials") {});

            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Account is deactivated"));
            }

            // Update last login timestamp
            userService.updateLastLogin(user.getId());

            // Create response with user information
            Map<String, Object> response = createSuccessResponse("Login successful");
            response.put("user", createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Login failed"));
        }
    }

    /**
     * Farm user registration endpoint
     * POST /api/auth/register/farm
     */
    @PostMapping("/register/farm")
    public ResponseEntity<Map<String, Object>> registerFarmUser(@Valid @RequestBody FarmUserRegistrationRequest request, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createValidationErrorResponse(bindingResult));
            }

            // Create primary farm user (account owner)
            FarmUser farmUser = farmUserService.createFarmUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getAccountId(),
                    true, // Primary user
                    null  // Self-registration
            );

            Map<String, Object> response = createSuccessResponse("Farm user registered successfully");
            response.put("user", createUserResponse(farmUser));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Registration failed"));
        }
    }

    /**
     * Veterinarian registration endpoint
     * POST /api/auth/register/veterinarian
     */
    @PostMapping("/register/veterinarian")
    public ResponseEntity<Map<String, Object>> registerVeterinarian(@Valid @RequestBody VeterinarianRegistrationRequest request, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createValidationErrorResponse(bindingResult));
            }

            Veterinarian veterinarian = veterinarianService.registerVeterinarian(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getLicenseNumber(),
                    request.getLicenseState(),
                    request.getPracticeName(),
                    request.getPracticeAddress(),
                    request.getPracticePhone(),
                    request.getSpecialization()
            );

            Map<String, Object> response = createSuccessResponse("Veterinarian registered successfully. Awaiting verification.");
            response.put("user", createUserResponse(veterinarian));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Registration failed"));
        }
    }

    /**
     * Get current user profile
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Map<String, Object> response = createSuccessResponse("Profile retrieved successfully");
            response.put("user", createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve profile"));
        }
    }

    /**
     * Logout endpoint
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // In a stateless API, logout is mainly handled client-side
        // This endpoint exists for consistency and future session management
        return ResponseEntity.ok(createSuccessResponse("Logout successful"));
    }

    // Helper Methods
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Validation failed");
        response.put("timestamp", LocalDateTime.now());

        Map<String, String> validationErrors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        response.put("validationErrors", validationErrors);

        return response;
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("userType", user.getUserType());
        userResponse.put("isActive", user.getIsActive());
        userResponse.put("createdAt", user.getCreatedAt());

        // Add type-specific information
        if (user instanceof FarmUser farmUser) {
            userResponse.put("accountId", farmUser.getAccountId());
            userResponse.put("isPrimaryUser", farmUser.getIsPrimaryUser());
        } else if (user instanceof PlatformUser platformUser) {
            userResponse.put("platformRole", platformUser.getPlatformRole());
        } else if (user instanceof Veterinarian veterinarian) {
            userResponse.put("isVerified", veterinarian.getIsVerified());
            userResponse.put("licenseState", veterinarian.getLicenseState());
            userResponse.put("practiceName", veterinarian.getPracticeName());
        }

        return userResponse;
    }
}