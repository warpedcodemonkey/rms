package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finders
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Existence checks
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    /**
     * Count active administrators in the system
     */
    @Query("SELECT COUNT(u) FROM User u WHERE TYPE(u) = Administrator AND u.isActive = true")
    long countActiveAdministrators();

    /**
     * Find users by account ID (for tenant filtering)
     */
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId")
    List<User> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Count users by account ID
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.primaryAccount.id = :accountId AND u.isActive = true")
    long countByAccountId(@Param("accountId") Long accountId);

    /**
     * Count users by primary account ID
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.primaryAccount.id = :accountId")
    long countByPrimaryAccountId(@Param("accountId") Long accountId);

    /**
     * Find users that a veterinarian has access to
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN VetPermission vp ON vp.account.id = u.primaryAccount.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true")
    List<User> findByVeterinarianAccess(@Param("vetId") Long vetId);

    /**
     * Find all administrators
     */
    @Query("SELECT u FROM User u WHERE TYPE(u) = Administrator")
    List<User> findAdministrators();

    /**
     * Find all veterinarians
     */
    @Query("SELECT u FROM User u WHERE TYPE(u) = Veterinarian")
    List<User> findVeterinarians();

    /**
     * Find users by active status
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * Check if username exists (excluding specific user ID)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :excludeId")
    boolean existsByUsernameExcludingId(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * Check if email exists (excluding specific user ID)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") Long excludeId);

    /**
     * Find users created within date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find inactive users that were deactivated before a certain date
     */
    @Query("SELECT u FROM User u WHERE u.isActive = false AND u.deactivatedAt < :cutoffDate")
    List<User> findInactiveUsersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.customPermissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.customPermissions WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
}