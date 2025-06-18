package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.model.user.FarmUser;
import com.krazykritterranch.rms.repositories.user.FarmUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for FarmUser entity operations and business logic.
 *
 * Handles farm user management including account ownership, user limits,
 * and invitation tracking.
 */
@Service
@Transactional
public class FarmUserService {

    private final FarmUserRepository farmUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public FarmUserService(FarmUserRepository farmUserRepository,
                           PasswordEncoder passwordEncoder) {
        this.farmUserRepository = farmUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Basic CRUD Operations
    public Optional<FarmUser> findById(Long id) {
        return farmUserRepository.findById(id);
    }

    public Optional<FarmUser> findByEmail(String email) {
        return farmUserRepository.findByEmail(email);
    }

    public Optional<FarmUser> findByUsername(String username) {
        return farmUserRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return farmUserRepository.existsByEmail(email);
    }

    public List<FarmUser> findAllActive() {
        return farmUserRepository.findByIsActiveTrue();
    }

    public List<FarmUser> findAllInactive() {
        return farmUserRepository.findByIsActiveFalse();
    }

    // Farm User Creation
    public FarmUser createFarmUser(String email, String password, String firstName, String lastName,
                                   Long accountId, Boolean isPrimaryUser, Long createdByUserId) {
        validateEmailNotExists(email);
        validateAccountUserLimit(accountId);

        if (isPrimaryUser) {
            validateNoPrimaryUserExists(accountId);
        }

        FarmUser farmUser = new FarmUser(email, encodePassword(password), firstName, lastName, accountId, isPrimaryUser);
        farmUser.setCreatedByUserId(createdByUserId);
        farmUser.setUpdatedByUserId(createdByUserId);

        return farmUserRepository.save(farmUser);
    }

    public FarmUser inviteFarmUser(String email, String password, String firstName, String lastName,
                                   Long accountId, Long invitedByUserId) {
        validateEmailNotExists(email);
        validateAccountUserLimit(accountId);
        validateInviterPermissions(invitedByUserId, accountId);

        FarmUser farmUser = new FarmUser(email, encodePassword(password), firstName, lastName, accountId, false);
        farmUser.setInvitedByUserId(invitedByUserId);
        farmUser.setCreatedByUserId(invitedByUserId);
        farmUser.setUpdatedByUserId(invitedByUserId);

        return farmUserRepository.save(farmUser);
    }

    // Account-Specific Queries
    public List<FarmUser> findActiveFarmUsersByAccount(Long accountId) {
        return farmUserRepository.findActiveFarmUsersByAccountId(accountId);
    }

    public List<FarmUser> findAllFarmUsersByAccount(Long accountId) {
        return farmUserRepository.findAllFarmUsersByAccountId(accountId);
    }

    public Optional<FarmUser> findPrimaryUserByAccount(Long accountId) {
        return farmUserRepository.findPrimaryUserByAccountId(accountId);
    }

    public Long countActiveFarmUsersByAccount(Long accountId) {
        return farmUserRepository.countActiveFarmUsersByAccountId(accountId);
    }

    public Long countAllFarmUsersByAccount(Long accountId) {
        return farmUserRepository.countAllFarmUsersByAccountId(accountId);
    }

    // Primary User Management
    public FarmUser transferPrimaryUserRole(Long currentPrimaryUserId, Long newPrimaryUserId, Long updatedByUserId) {
        FarmUser currentPrimary = findById(currentPrimaryUserId)
                .orElseThrow(() -> new IllegalArgumentException("Current primary user not found: " + currentPrimaryUserId));

        FarmUser newPrimary = findById(newPrimaryUserId)
                .orElseThrow(() -> new IllegalArgumentException("New primary user not found: " + newPrimaryUserId));

        validatePrimaryUserTransfer(currentPrimary, newPrimary, updatedByUserId);

        // Remove primary status from current user
        currentPrimary.setIsPrimaryUser(false);
        currentPrimary.setUpdatedAt(LocalDateTime.now());
        currentPrimary.setUpdatedByUserId(updatedByUserId);
        farmUserRepository.save(currentPrimary);

        // Set primary status on new user
        newPrimary.setIsPrimaryUser(true);
        newPrimary.setUpdatedAt(LocalDateTime.now());
        newPrimary.setUpdatedByUserId(updatedByUserId);

        return farmUserRepository.save(newPrimary);
    }

    public List<FarmUser> findAllActivePrimaryUsers() {
        return farmUserRepository.findAllActivePrimaryUsers();
    }

    // Invitation Management
    public List<FarmUser> findActiveUsersInvitedBy(Long inviterId) {
        return farmUserRepository.findActiveUsersInvitedBy(inviterId);
    }

    public List<FarmUser> findAllUsersInvitedBy(Long inviterId) {
        return farmUserRepository.findAllUsersInvitedBy(inviterId);
    }

    // Farm User Management
    public FarmUser deactivateFarmUser(Long userId, Long deactivatedByUserId, String reason) {
        FarmUser farmUser = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Farm user not found: " + userId));

        validateDeactivationPermissions(deactivatedByUserId, farmUser);

        farmUser.setIsActive(false);
        farmUser.setEndDate(LocalDateTime.now());
        farmUser.setEndReason(reason);
        farmUser.setEndedByUserId(deactivatedByUserId);
        farmUser.setUpdatedAt(LocalDateTime.now());
        farmUser.setUpdatedByUserId(deactivatedByUserId);

        return farmUserRepository.save(farmUser);
    }

    public FarmUser reactivateFarmUser(Long userId, Long reactivatedByUserId) {
        FarmUser farmUser = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Farm user not found: " + userId));

        validateReactivationPermissions(reactivatedByUserId, farmUser.getAccountId());
        validateAccountUserLimit(farmUser.getAccountId());

        farmUser.setIsActive(true);
        farmUser.setReactivatedDate(LocalDateTime.now());
        farmUser.setReactivatedByUserId(reactivatedByUserId);
        farmUser.setEndDate(null);
        farmUser.setEndReason(null);
        farmUser.setEndedByUserId(null);
        farmUser.setUpdatedAt(LocalDateTime.now());
        farmUser.setUpdatedByUserId(reactivatedByUserId);

        return farmUserRepository.save(farmUser);
    }

    // Search and Analytics
    public List<FarmUser> searchActiveFarmUsersByAccount(Long accountId, String searchTerm) {
        return farmUserRepository.searchActiveFarmUsersByAccount(accountId, searchTerm);
    }

    public List<FarmUser> findCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return farmUserRepository.findCreatedBetween(startDate, endDate);
    }

    public List<FarmUser> findCreatedBetweenByAccount(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return farmUserRepository.findCreatedBetweenByAccount(accountId, startDate, endDate);
    }

    public List<FarmUser> findLoggedInSince(LocalDateTime since) {
        return farmUserRepository.findLoggedInSince(since);
    }

    public List<FarmUser> findLoggedInSinceByAccount(Long accountId, LocalDateTime since) {
        return farmUserRepository.findLoggedInSinceByAccount(accountId, since);
    }

    // Validation Methods
    private void validateEmailNotExists(String email) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }

    private void validateAccountUserLimit(Long accountId) {
        long userCount = countActiveFarmUsersByAccount(accountId);
        if (userCount >= 5) {
            throw new IllegalArgumentException("Account has reached the 5-user limit");
        }
    }

    private void validateNoPrimaryUserExists(Long accountId) {
        if (farmUserRepository.existsByAccountIdAndIsPrimaryUserTrue(accountId)) {
            throw new IllegalArgumentException("Account already has a primary user");
        }
    }

    private void validateInviterPermissions(Long inviterId, Long accountId) {
        FarmUser inviter = findById(inviterId)
                .orElseThrow(() -> new IllegalArgumentException("Inviter not found: " + inviterId));

        if (!inviter.getAccountId().equals(accountId)) {
            throw new IllegalArgumentException("User can only invite others to their own account");
        }

        if (!inviter.getIsActive()) {
            throw new IllegalArgumentException("Inactive users cannot invite others");
        }
    }

    private void validatePrimaryUserTransfer(FarmUser currentPrimary, FarmUser newPrimary, Long updatedByUserId) {
        if (!currentPrimary.getIsPrimaryUser()) {
            throw new IllegalArgumentException("Current user is not the primary user");
        }

        if (!currentPrimary.getAccountId().equals(newPrimary.getAccountId())) {
            throw new IllegalArgumentException("Users must be in the same account");
        }

        if (!newPrimary.getIsActive()) {
            throw new IllegalArgumentException("Cannot transfer primary role to inactive user");
        }

        // Only current primary user or platform admin can transfer
        if (!updatedByUserId.equals(currentPrimary.getId())) {
            // TODO: Add check for platform admin permissions when platform user service is available
            throw new IllegalArgumentException("Only the current primary user can transfer ownership");
        }
    }

    private void validateDeactivationPermissions(Long deactivatedByUserId, FarmUser targetUser) {
        // Users cannot deactivate themselves if they are the primary user
        if (deactivatedByUserId.equals(targetUser.getId()) && targetUser.getIsPrimaryUser()) {
            throw new IllegalArgumentException("Primary users cannot deactivate themselves");
        }

        // TODO: Add additional permission checks based on role system when available
    }

    private void validateReactivationPermissions(Long reactivatedByUserId, Long accountId) {
        // TODO: Add permission checks when role system is available
        // For now, only check that reactivator is from the same account and is primary user
        FarmUser reactivator = findById(reactivatedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reactivator not found: " + reactivatedByUserId));

        if (!reactivator.getAccountId().equals(accountId)) {
            throw new IllegalArgumentException("User can only reactivate users in their own account");
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}