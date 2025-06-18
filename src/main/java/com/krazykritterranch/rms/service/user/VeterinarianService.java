package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.model.user.Veterinarian;
import com.krazykritterranch.rms.repositories.user.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Veterinarian entity operations and business logic.
 *
 * Handles veterinarian management including professional verification,
 * license validation, and practice information management.
 */
@Service
@Transactional
public class VeterinarianService {

    private final VeterinarianRepository veterinarianRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public VeterinarianService(VeterinarianRepository veterinarianRepository,
                               PasswordEncoder passwordEncoder) {
        this.veterinarianRepository = veterinarianRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Basic CRUD Operations
    public Optional<Veterinarian> findById(Long id) {
        return veterinarianRepository.findById(id);
    }

    public Optional<Veterinarian> findByEmail(String email) {
        return veterinarianRepository.findByEmail(email);
    }

    public Optional<Veterinarian> findByUsername(String username) {
        return veterinarianRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return veterinarianRepository.existsByEmail(email);
    }

    public List<Veterinarian> findAllActive() {
        return veterinarianRepository.findByIsActiveTrue();
    }

    public List<Veterinarian> findAllInactive() {
        return veterinarianRepository.findByIsActiveFalse();
    }

    // Veterinarian Creation
    public Veterinarian createVeterinarian(String email, String password, String firstName, String lastName,
                                           String licenseNumber, String licenseState, String practiceName) {
        validateEmailNotExists(email);
        validateLicenseNotExists(licenseNumber);

        Veterinarian veterinarian = new Veterinarian(email, encodePassword(password), firstName, lastName,
                licenseNumber, licenseState, practiceName);

        return veterinarianRepository.save(veterinarian);
    }

    public Veterinarian registerVeterinarian(String email, String password, String firstName, String lastName,
                                             String licenseNumber, String licenseState, String practiceName,
                                             String practiceAddress, String practicePhone, String specialization) {
        validateEmailNotExists(email);
        validateLicenseNotExists(licenseNumber);

        Veterinarian veterinarian = new Veterinarian(email, encodePassword(password), firstName, lastName,
                licenseNumber, licenseState, practiceName);
        veterinarian.setPracticeAddress(practiceAddress);
        veterinarian.setPracticePhone(practicePhone);
        veterinarian.setSpecialization(specialization);

        return veterinarianRepository.save(veterinarian);
    }

    // Verification Management
    public List<Veterinarian> findActiveVerifiedVeterinarians() {
        return veterinarianRepository.findActiveVerifiedVeterinarians();
    }

    public List<Veterinarian> findUnverifiedVeterinarians() {
        return veterinarianRepository.findUnverifiedVeterinarians();
    }

    public List<Veterinarian> findPendingVerificationQueue() {
        return veterinarianRepository.findPendingVerificationOrderByCreatedDate();
    }

    public Veterinarian verifyVeterinarian(Long veterinarianId, Long verifiedByUserId) {
        Veterinarian veterinarian = findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));

        validateVerificationPermissions(verifiedByUserId);

        veterinarian.setIsVerified(true);
        veterinarian.setVerifiedByUserId(verifiedByUserId);
        veterinarian.setUpdatedAt(LocalDateTime.now());
        veterinarian.setUpdatedByUserId(verifiedByUserId);

        return veterinarianRepository.save(veterinarian);
    }

    public Veterinarian rejectVerification(Long veterinarianId, Long rejectedByUserId, String rejectionReason) {
        Veterinarian veterinarian = findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));

        validateVerificationPermissions(rejectedByUserId);

        // For now, we'll deactivate rejected veterinarians
        veterinarian.setIsActive(false);
        veterinarian.setEndDate(LocalDateTime.now());
        veterinarian.setEndReason("Verification rejected: " + rejectionReason);
        veterinarian.setEndedByUserId(rejectedByUserId);
        veterinarian.setUpdatedAt(LocalDateTime.now());
        veterinarian.setUpdatedByUserId(rejectedByUserId);

        return veterinarianRepository.save(veterinarian);
    }

    public List<Veterinarian> findVerifiedByUser(Long verifierId) {
        return veterinarianRepository.findVerifiedByUser(verifierId);
    }

    // License and Practice Queries
    public List<Veterinarian> findActiveVeterinariansByState(String state) {
        return veterinarianRepository.findActiveVeterinariansByLicenseState(state);
    }

    public List<Veterinarian> findVerifiedVeterinariansByState(String state) {
        return veterinarianRepository.findVerifiedVeterinariansByState(state);
    }

    public Optional<Veterinarian> findByLicenseNumberAndState(String licenseNumber, String state) {
        return veterinarianRepository.findByLicenseNumberAndState(licenseNumber, state);
    }

    public boolean existsByLicenseNumber(String licenseNumber) {
        return veterinarianRepository.existsByLicenseNumber(licenseNumber);
    }

    // Practice Information Queries
    public List<Veterinarian> findByPracticeNameContaining(String practiceName) {
        return veterinarianRepository.findByPracticeNameContaining(practiceName);
    }

    public List<Veterinarian> findBySpecializationContaining(String specialization) {
        return veterinarianRepository.findBySpecializationContaining(specialization);
    }

    // Search Veterinarians
    public List<Veterinarian> searchVerifiedVeterinarians(String searchTerm) {
        return veterinarianRepository.searchVerifiedVeterinarians(searchTerm);
    }

    // Veterinarian Management
    public Veterinarian updatePracticeInformation(Long veterinarianId, String practiceName, String practiceAddress,
                                                  String practicePhone, String specialization, Long updatedByUserId) {
        Veterinarian veterinarian = findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));

        validateUpdatePermissions(veterinarian, updatedByUserId);

        veterinarian.setPracticeName(practiceName);
        veterinarian.setPracticeAddress(practiceAddress);
        veterinarian.setPracticePhone(practicePhone);
        veterinarian.setSpecialization(specialization);
        veterinarian.setUpdatedAt(LocalDateTime.now());
        veterinarian.setUpdatedByUserId(updatedByUserId);

        return veterinarianRepository.save(veterinarian);
    }

    public Veterinarian deactivateVeterinarian(Long veterinarianId, Long deactivatedByUserId, String reason) {
        Veterinarian veterinarian = findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));

        validateDeactivationPermissions(deactivatedByUserId, veterinarian);

        veterinarian.setIsActive(false);
        veterinarian.setEndDate(LocalDateTime.now());
        veterinarian.setEndReason(reason);
        veterinarian.setEndedByUserId(deactivatedByUserId);
        veterinarian.setUpdatedAt(LocalDateTime.now());
        veterinarian.setUpdatedByUserId(deactivatedByUserId);

        return veterinarianRepository.save(veterinarian);
    }

    public Veterinarian reactivateVeterinarian(Long veterinarianId, Long reactivatedByUserId) {
        Veterinarian veterinarian = findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));

        validateReactivationPermissions(reactivatedByUserId);

        veterinarian.setIsActive(true);
        veterinarian.setReactivatedDate(LocalDateTime.now());
        veterinarian.setReactivatedByUserId(reactivatedByUserId);
        veterinarian.setEndDate(null);
        veterinarian.setEndReason(null);
        veterinarian.setEndedByUserId(null);
        veterinarian.setUpdatedAt(LocalDateTime.now());
        veterinarian.setUpdatedByUserId(reactivatedByUserId);

        return veterinarianRepository.save(veterinarian);
    }

    // Analytics and Reporting
    public List<Veterinarian> findCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return veterinarianRepository.findCreatedBetween(startDate, endDate);
    }

    public List<Veterinarian> findLoggedInSince(LocalDateTime since) {
        return veterinarianRepository.findLoggedInSince(since);
    }

    public Long countActiveVerifiedVeterinarians() {
        return veterinarianRepository.countActiveVerifiedVeterinarians();
    }

    public Long countPendingVerification() {
        return veterinarianRepository.countPendingVerification();
    }

    public Long countVerifiedVeterinariansByState(String state) {
        return veterinarianRepository.countVerifiedVeterinariansByState(state);
    }

    // Validation Methods
    private void validateEmailNotExists(String email) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }

    private void validateLicenseNotExists(String licenseNumber) {
        if (existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("License number already exists: " + licenseNumber);
        }
    }

    private void validateVerificationPermissions(Long verifierId) {
        // TODO: Add platform admin permission check when platform user service integration is available
        // For now, we assume the verifier has been validated at the controller level
        if (verifierId == null) {
            throw new IllegalArgumentException("Verifier ID cannot be null");
        }
    }

    private void validateUpdatePermissions(Veterinarian veterinarian, Long updatedByUserId) {
        // Veterinarians can update their own information, or platform admins can update any
        if (!updatedByUserId.equals(veterinarian.getId())) {
            // TODO: Add platform admin permission check when available
            throw new IllegalArgumentException("Veterinarians can only update their own practice information");
        }
    }

    private void validateDeactivationPermissions(Long deactivatedByUserId, Veterinarian targetVeterinarian) {
        // Users can deactivate themselves, or platform admins can deactivate any
        if (!deactivatedByUserId.equals(targetVeterinarian.getId())) {
            // TODO: Add platform admin permission check when available
            throw new IllegalArgumentException("Insufficient permissions to deactivate veterinarian");
        }
    }

    private void validateReactivationPermissions(Long reactivatedByUserId) {
        // TODO: Add platform admin permission check when available
        // Only platform admins should be able to reactivate veterinarians
        if (reactivatedByUserId == null) {
            throw new IllegalArgumentException("Reactivator ID cannot be null");
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}