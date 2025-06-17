package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.FarmUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FarmUser entity operations.
 *
 * Provides CRUD operations and custom queries specific to farm users
 * and account management functionality.
 */
@Repository
public interface FarmUserRepository extends JpaRepository<FarmUser, Long> {

    // Basic Farm User Queries
    Optional<FarmUser> findByEmail(String email);

    Optional<FarmUser> findByUsername(String username);

    boolean existsByEmail(String email);

    // Active Farm Users
    List<FarmUser> findByIsActiveTrue();

    List<FarmUser> findByIsActiveFalse();

    // Account-Specific Queries
    @Query("SELECT f FROM FarmUser f WHERE f.isActive = true AND f.accountId = :accountId")
    List<FarmUser> findActiveFarmUsersByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT f FROM FarmUser f WHERE f.accountId = :accountId")
    List<FarmUser> findAllFarmUsersByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT f FROM FarmUser f WHERE f.accountId = :accountId AND f.isPrimaryUser = true")
    Optional<FarmUser> findPrimaryUserByAccountId(@Param("accountId") Long accountId);

    // User Invitation Tracking
    @Query("SELECT f FROM FarmUser f WHERE f.invitedByUserId = :inviterId AND f.isActive = true")
    List<FarmUser> findActiveUsersInvitedBy(@Param("inviterId") Long inviterId);

    @Query("SELECT f FROM FarmUser f WHERE f.invitedByUserId = :inviterId")
    List<FarmUser> findAllUsersInvitedBy(@Param("inviterId") Long inviterId);

    // Account User Limits and Counts
    @Query("SELECT COUNT(f) FROM FarmUser f WHERE f.accountId = :accountId AND f.isActive = true")
    Long countActiveFarmUsersByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT COUNT(f) FROM FarmUser f WHERE f.accountId = :accountId")
    Long countAllFarmUsersByAccountId(@Param("accountId") Long accountId);

    // Primary User Management
    @Query("SELECT f FROM FarmUser f WHERE f.isPrimaryUser = true AND f.isActive = true")
    List<FarmUser> findAllActivePrimaryUsers();

    boolean existsByAccountIdAndIsPrimaryUserTrue(Long accountId);

    // Search Farm Users
    @Query("SELECT f FROM FarmUser f WHERE f.isActive = true AND f.accountId = :accountId AND " +
            "(LOWER(f.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<FarmUser> searchActiveFarmUsersByAccount(@Param("accountId") Long accountId,
                                                  @Param("searchTerm") String searchTerm);

    // Analytics Queries
    @Query("SELECT f FROM FarmUser f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    List<FarmUser> findCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT f FROM FarmUser f WHERE f.accountId = :accountId AND f.createdAt BETWEEN :startDate AND :endDate")
    List<FarmUser> findCreatedBetweenByAccount(@Param("accountId") Long accountId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT f FROM FarmUser f WHERE f.lastLogin IS NOT NULL AND f.lastLogin > :since")
    List<FarmUser> findLoggedInSince(@Param("since") LocalDateTime since);

    @Query("SELECT f FROM FarmUser f WHERE f.accountId = :accountId AND f.lastLogin IS NOT NULL AND f.lastLogin > :since")
    List<FarmUser> findLoggedInSinceByAccount(@Param("accountId") Long accountId,
                                              @Param("since") LocalDateTime since);
}