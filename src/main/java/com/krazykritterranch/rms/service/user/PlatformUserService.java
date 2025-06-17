package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.model.user.PlatformUser;
import com.krazykritterranch.rms.repositories.user.PlatformUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for PlatformUser entity operations and business logic.
 *
 * Handles platform user management including Super Admins and Support Admins.
 */
@Service
@Transactional
public class PlatformUserService {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PlatformUserService(PlatformUserRepository platformUserRepository,
                               PasswordEncoder passwordEncoder) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Basic CRUD Operations
    public Optional<PlatformUser> findById(Long id) {
        return platformUserRepository.findById(id);
    }

    public Optional<PlatformUser> findByEmail(String email) {
        return platformUserRepository.findByEmail(email);
    }

    public Optional<PlatformUser> findByUsername(String username) {
        return platformUserRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return platformUserRepository.existsByEmail(email);
    }

    public List<PlatformUser> findAllActive() {
        return platformUserRepository.findByIsActiveTrue();
    }

    public List<PlatformUser> findAllInactive() {
        return platformUserRepository.findByIsActiveFalse();
    }

    // Platform User Creation
    public PlatformUser createPlatformUser(String email, String password, String firstName, String lastName,
                                           PlatformUser.PlatformRole role, Long createdByUserId) {
        validateEmailNotExists(email);
        validateCreatorPermissions(createdByUserId, role);

        PlatformUser platformUser = new PlatformUser(email, encodePassword(password), firstName, lastName, role);
        platformUser.setCreatedByUserId(createdByUserId);
        platformUser.setUpdatedByUserId(createdByUserId);

        return platformUserRepository.save(platformUser);
    }

    // Role-Based Queries
    public List<PlatformUser> findActiveByRole(PlatformUser.PlatformRole role) {
        return platformUserRepository.findActiveByPlatformRole(role);
    }

    public List<PlatformUser> findActiveSuperAdmins() {
        return platformUserRepository.findActiveSuperAdmins();
    }

    public List<PlatformUser> findActiveSupportAdmins() {
        return platformUserRepository.findActiveSupportAdmins();
    }

    // Platform User Management
    public PlatformUser updatePlatformRole(Long userId, PlatformUser.PlatformRole newRole, Long updatedByUserId) {
        PlatformUser platformUser = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Platform user not found: " + userId));

        validateRoleChangePermissions(updatedByUserId, newRole);

        platformUser.setPlatformRole(newRole);
        platformUser.setUpdatedAt(LocalDateTime.now());
        platformUser.setUpdatedByUserId(updatedByUserId);

        return platformUserRepository.save(platformUser);
    }

    public PlatformUser deactivatePlatformUser(Long userId, Long deactivatedByUserId, String reason) {
        PlatformUser platformUser = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Platform user not found: " + userId));

        validateDeactivationPermissions(deactivatedByUserId, platformUser);

        platformUser.setIsActive(false);
        platformUser.setEndDate(LocalDateTime.now());
        platformUser.setEndReason(reason);
        platformUser.setEndedByUserId(deactivatedByUserId);
        platformUser.setUpdatedAt(LocalDateTime.now());
        platformUser.setUpdatedByUserId(deactivatedByUserId);

        return platformUserRepository.save(platformUser);
    }

    public PlatformUser reactivatePlatformUser(Long userId, Long reactivatedByUserId) {
        PlatformUser platformUser = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Platform user not found: " + userId));

        validateReactivationPermissions(reactivatedByUserId);

        platformUser.setIsActive(true);
        platformUser.setReactivatedDate(LocalDateTime.now());
        platformUser.setReactivatedByUserId(reactivatedByUserId);
        platformUser.setEndDate(null);
        platformUser.setEndReason(null);
        platformUser.setEndedByUserId(null);
        platformUser.setUpdatedAt(LocalDateTime.now());
        platformUser.setUpdatedByUserId(reactivatedByUserId);

        return platformUserRepository.save(platformUser);
    }

    // Search and Analytics
    public List<PlatformUser> searchActivePlatformUsers(String searchTerm) {
        return platformUserRepository.searchActivePlatformUsers(searchTerm);
    }

    public List<PlatformUser> findCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return platformUserRepository.findCreatedBetween(startDate, endDate);
    }

    public List<PlatformUser> findLoggedInSince(LocalDateTime since) {
        return platformUserRepository.findLoggedInSince(since);
    }

    public Long countActiveByRole(PlatformUser.PlatformRole role) {
        return platformUserRepository.countActiveByPlatformRole(role);
    }

    // Permission Validation Methods
    private void validateEmailNotExists(String email) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }

    private void validateCreatorPermissions(Long createdByUserId, PlatformUser.PlatformRole roleToCreate) {
        PlatformUser creator = findById(createdByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found: " + createdByUserId));

        // Only Super Admins can create platform users
        if (!creator.canCreatePlatformUsers()) {
            throw new IllegalArgumentException("Insufficient permissions to create platform users");
        }

        // Only Super Admins can create other Super Admins
        if (roleToCreate == PlatformUser.PlatformRole.SUPER_ADMIN &&
                creator.getPlatformRole() != PlatformUser.PlatformRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only Super Admins can create other Super Admins");
        }
    }

    private void validateRoleChangePermissions(Long updatedByUserId, PlatformUser.PlatformRole newRole) {
        PlatformUser updater = findById(updatedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Updater not found: " + updatedByUserId));

        // Only Super Admins can change roles
        if (updater.getPlatformRole() != PlatformUser.PlatformRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only Super Admins can change platform user roles");
        }
    }

    private void validateDeactivationPermissions(Long deactivatedByUserId, PlatformUser targetUser) {
        PlatformUser deactivator = findById(deactivatedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Deactivator not found: " + deactivatedByUserId));

        // Users cannot deactivate themselves
        if (deactivatedByUserId.equals(targetUser.getId())) {
            throw new IllegalArgumentException("Users cannot deactivate themselves");
        }

        // Only Super Admins can deactivate other platform users
        if (deactivator.getPlatformRole() != PlatformUser.PlatformRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only Super Admins can deactivate platform users");
        }
    }

    private void validateReactivationPermissions(Long reactivatedByUserId) {
        PlatformUser reactivator = findById(reactivatedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reactivator not found: " + reactivatedByUserId));

        // Only Super Admins can reactivate platform users
        if (reactivator.getPlatformRole() != PlatformUser.PlatformRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Only Super Admins can reactivate platform users");
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}