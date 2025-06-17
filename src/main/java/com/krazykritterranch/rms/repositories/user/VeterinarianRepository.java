package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Veterinarian entity operations.
 *
 * Provides CRUD operations and custom queries specific to veterinarians
 * including verification status and practice information.
 */
@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {

    // Basic Veterinarian Queries
    Optional<Veterinarian> findByEmail(String email);

    Optional<Veterinarian> findByUsername(String username);

    boolean existsByEmail(String email);

    // Active Veterinarians
    List<Veterinarian> findByIsActiveTrue();

    List<Veterinarian> findByIsActiveFalse();

    // Verification Status Queries
    @Query("SELECT v FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = true")
    List<Veterinarian> findActiveVerifiedVeterinarians();

    @Query("SELECT v FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = false")
    List<Veterinarian> findUnverifiedVeterinarians();

    @Query("SELECT v FROM Veterinarian v WHERE v.isVerified = false ORDER BY v.createdAt ASC")
    List<Veterinarian> findPendingVerificationOrderByCreatedDate();

    // License and Practice Queries
    @Query("SELECT v FROM Veterinarian v WHERE v.isActive = true AND v.licenseState = :state")
    List<Veterinarian> findActiveVeterinariansByLicenseState(@Param("state") String state);

    @Query("SELECT v FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = true AND v.licenseState = :state")
    List<Veterinarian> findVerifiedVeterinariansByState(@Param("state") String state);

    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT v FROM Veterinarian v WHERE v.licenseNumber = :licenseNumber AND v.licenseState = :state")
    Optional<Veterinarian> findByLicenseNumberAndState(@Param("licenseNumber") String licenseNumber,
                                                       @Param("state") String state);

    // Practice Information Queries
    @Query("SELECT v FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = true AND " +
            "LOWER(v.practiceName) LIKE LOWER(CONCAT('%', :practiceName, '%'))")
    List<Veterinarian> findByPracticeNameContaining(@Param("practiceName") String practiceName);

    @Query("SELECT v FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = true AND " +
            "LOWER(v.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    List<Veterinarian> findBySpecializationContaining(@Param("specialization") String specialization);

    // Search Veterinarians
    @Query("SELECT v FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = true AND " +
            "(LOWER(v.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.practiceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(v.specialization) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Veterinarian> searchVerifiedVeterinarians(@Param("searchTerm") String searchTerm);

    // Verification Management
    @Query("SELECT v FROM Veterinarian v WHERE v.verifiedByUserId = :verifierId")
    List<Veterinarian> findVerifiedByUser(@Param("verifierId") Long verifierId);

    // Analytics Queries
    @Query("SELECT v FROM Veterinarian v WHERE v.createdAt BETWEEN :startDate AND :endDate")
    List<Veterinarian> findCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT v FROM Veterinarian v WHERE v.lastLogin IS NOT NULL AND v.lastLogin > :since")
    List<Veterinarian> findLoggedInSince(@Param("since") LocalDateTime since);

    // Count Queries
    @Query("SELECT COUNT(v) FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = true")
    Long countActiveVerifiedVeterinarians();

    @Query("SELECT COUNT(v) FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = false")
    Long countPendingVerification();

    @Query("SELECT COUNT(v) FROM Veterinarian v WHERE v.isActive = true AND v.isVerified = true AND v.licenseState = :state")
    Long countVerifiedVeterinariansByState(@Param("state") String state);
}