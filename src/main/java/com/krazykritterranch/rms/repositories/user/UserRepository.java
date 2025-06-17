package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.FarmUser;
import com.krazykritterranch.rms.model.user.PlatformUser;
import com.krazykritterranch.rms.model.user.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 *
 * Provides CRUD operations and custom queries for all user types
 * in the single-table inheritance hierarchy.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic User Queries
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    // Active/Inactive User Queries
    List<User> findByIsActiveTrue();

    List<User> findByIsActiveFalse();

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastLogin < :cutoffDate")
    List<User> findInactiveUsersSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    // User Type Specific Queries
    @Query("SELECT u FROM PlatformUser u WHERE u.isActive = true")
    List<PlatformUser> findActivePlatformUsers();

    @Query("SELECT u FROM FarmUser u WHERE u.isActive = true AND u.accountId = :accountId")
    List<FarmUser> findActiveFarmUsersByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT u FROM FarmUser u WHERE u.accountId = :accountId AND u.isPrimaryUser = true")
    Optional<FarmUser> findPrimaryUserByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT u FROM Veterinarian u WHERE u.isActive = true AND u.isVerified = true")
    List<Veterinarian> findActiveVerifiedVeterinarians();

    @Query("SELECT u FROM Veterinarian u WHERE u.isActive = true AND u.isVerified = false")
    List<Veterinarian> findUnverifiedVeterinarians();

    // Search and Filtering Queries
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchActiveUsers(@Param("searchTerm") String searchTerm);

    // Account Management Queries
    @Query("SELECT COUNT(u) FROM FarmUser u WHERE u.accountId = :accountId AND u.isActive = true")
    Long countActiveFarmUsersByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT u FROM FarmUser u WHERE u.invitedByUserId = :inviterId AND u.isActive = true")
    List<FarmUser> findUsersInvitedBy(@Param("inviterId") Long inviterId);

    // Audit and Analytics Queries
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u WHERE u.lastLogin IS NOT NULL AND u.lastLogin > :since")
    List<User> findUsersLoggedInSince(@Param("since") LocalDateTime since);

    // Soft Delete Related Queries
    @Query("SELECT u FROM User u WHERE u.endDate IS NOT NULL AND u.endDate BETWEEN :startDate AND :endDate")
    List<User> findUsersDeactivatedBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}