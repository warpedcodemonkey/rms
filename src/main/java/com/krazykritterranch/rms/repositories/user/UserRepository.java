package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository provides data access operations for User entities.
 *
 * This repository handles basic CRUD operations and common queries for all user types
 * in the single-table inheritance hierarchy. Business logic is handled in service layers.
 *
 * Multi-tenant security and validation are enforced in service layers, not in the repository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic authentication queries
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Existence checks for validation
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Exclude specific user when checking uniqueness (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :excludeId")
    boolean existsByUsernameExcludingId(@Param("username") String username, @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") Long excludeId);

    // Find by user level (for type-specific queries)
    @Query("SELECT u FROM User u WHERE TYPE(u) = :userClass")
    List<User> findByUserType(@Param("userClass") Class<? extends User> userClass);

    // Find by active status
    List<User> findByIsActive(Boolean isActive);

    // Find users by account association (for account-level users only)
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId")
    List<User> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.primaryAccount.id = :accountId AND u.isActive = true")
    long countActiveUsersByAccountId(@Param("accountId") Long accountId);

    // Find primary account user
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId AND u.isPrimaryAccountUser = true")
    Optional<User> findPrimaryAccountUser(@Param("accountId") Long accountId);

    // Find users created within date range
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find users by last login activity
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :sinceDate AND u.isActive = true")
    List<User> findActiveUsersSinceLastLogin(@Param("sinceDate") LocalDateTime sinceDate);

    // Find inactive users older than specified date (for cleanup)
    @Query("SELECT u FROM User u WHERE u.isActive = false AND u.endDate < :cutoffDate")
    List<User> findInactiveUsersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Search users by name (case-insensitive)
    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND u.isActive = true")
    List<User> searchByFullName(@Param("searchTerm") String searchTerm);

    // Find users with specific roles (via join)
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true")
    List<User> findByRoleName(@Param("roleName") String roleName);

    // Find users with specific permissions (via roles or custom permissions)
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.roles r " +
            "LEFT JOIN r.permissions rp " +
            "LEFT JOIN u.customPermissions cp " +
            "WHERE (rp.name = :permissionName OR cp.name = :permissionName) AND u.isActive = true")
    List<User> findByPermissionName(@Param("permissionName") String permissionName);

    // Count queries for statistics
    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = :userClass AND u.isActive = true")
    long countActiveUsersByType(@Param("userClass") Class<? extends User> userClass);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    // Eager fetch roles and permissions for authentication
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.customPermissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.customPermissions WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    // For multi-contact support (if users have multiple emails/phones)
    @Query("SELECT DISTINCT u FROM User u JOIN u.additionalEmails e WHERE e.emailAddress = :emailAddress")
    List<User> findByAdditionalEmail(@Param("emailAddress") String emailAddress);

    // For veterinarian-specific queries (cross-account access)
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN VetPermission vp ON vp.veterinarian.id = u.id " +
            "WHERE vp.account.id = :accountId AND vp.isActive = true AND u.isActive = true")
    List<User> findVeterinariansWithAccountAccess(@Param("accountId") Long accountId);

    // Bulk operations support
    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findByIds(@Param("userIds") List<Long> userIds);

    // Password reset queries
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    // Check if customer number exists for AccountUser entities
    @Query("SELECT COUNT(u) > 0 FROM AccountUser u WHERE u.customerNumber = :customerNumber")
    boolean existsByCustomerNumber(@Param("customerNumber") String customerNumber);
}