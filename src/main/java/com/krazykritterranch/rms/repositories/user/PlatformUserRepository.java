package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PlatformUser entity operations.
 *
 * Provides CRUD operations and custom queries specific to platform users
 * (Super Admins and Support Admins).
 */
@Repository
public interface PlatformUserRepository extends JpaRepository<PlatformUser, Long> {

    // Basic Platform User Queries
    Optional<PlatformUser> findByEmail(String email);

    Optional<PlatformUser> findByUsername(String username);

    boolean existsByEmail(String email);

    // Active Platform Users
    List<PlatformUser> findByIsActiveTrue();

    List<PlatformUser> findByIsActiveFalse();

    // Platform Role Specific Queries
    @Query("SELECT p FROM PlatformUser p WHERE p.isActive = true AND p.platformRole = :role")
    List<PlatformUser> findActiveByPlatformRole(@Param("role") PlatformUser.PlatformRole role);

    @Query("SELECT p FROM PlatformUser p WHERE p.isActive = true AND p.platformRole = 'SUPER_ADMIN'")
    List<PlatformUser> findActiveSuperAdmins();

    @Query("SELECT p FROM PlatformUser p WHERE p.isActive = true AND p.platformRole = 'SUPPORT_ADMIN'")
    List<PlatformUser> findActiveSupportAdmins();

    // Search Platform Users
    @Query("SELECT p FROM PlatformUser p WHERE p.isActive = true AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<PlatformUser> searchActivePlatformUsers(@Param("searchTerm") String searchTerm);

    // Analytics Queries
    @Query("SELECT p FROM PlatformUser p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<PlatformUser> findCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM PlatformUser p WHERE p.lastLogin IS NOT NULL AND p.lastLogin > :since")
    List<PlatformUser> findLoggedInSince(@Param("since") LocalDateTime since);

    // Count Queries
    @Query("SELECT COUNT(p) FROM PlatformUser p WHERE p.isActive = true AND p.platformRole = :role")
    Long countActiveByPlatformRole(@Param("role") PlatformUser.PlatformRole role);
}