package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.FarmUser;
import com.krazykritterranch.rms.model.user.PlatformUser;
import com.krazykritterranch.rms.model.user.Veterinarian;
import com.krazykritterranch.rms.repositories.user.UserRepository;
import com.krazykritterranch.rms.repositories.user.FarmUserRepository;
import com.krazykritterranch.rms.repositories.user.PlatformUserRepository;
import com.krazykritterranch.rms.repositories.user.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User entity operations and business logic.
 *
 * Implements Spring Security UserDetailsService for authentication
 * and provides comprehensive user management functionality.
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FarmUserRepository farmUserRepository;
    private final PlatformUserRepository platformUserRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       FarmUserRepository farmUserRepository,
                       PlatformUserRepository platformUserRepository,
                       VeterinarianRepository veterinarianRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.farmUserRepository = farmUserRepository;
        this.platformUserRepository = platformUserRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Spring Security UserDetailsService Implementation
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Basic User Operations
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    public List<User> searchActiveUsers(String searchTerm) {
        return userRepository.searchActiveUsers(searchTerm);
    }

    // User Creation Methods
    public PlatformUser createPlatformUser(String email, String password, String firstName, String lastName,
                                           PlatformUser.PlatformRole role) {
        validateEmailNotExists(email);

        PlatformUser platformUser = new PlatformUser(email, encodePassword(password), firstName, lastName, role);
        return platformUserRepository.save(platformUser);
    }

    public FarmUser createFarmUser(String email, String password, String firstName, String lastName,
                                   Long accountId, Boolean isPrimaryUser) {
        validateEmailNotExists(email);
        validateAccountUserLimit(accountId);

        if (isPrimaryUser) {
            validateNoPrimaryUserExists(accountId);
        }

        FarmUser farmUser = new FarmUser(email, encodePassword(password), firstName, lastName, accountId, isPrimaryUser);
        return farmUserRepository.save(farmUser);
    }

    public Veterinarian createVeterinarian(String email, String password, String firstName, String lastName,
                                           String licenseNumber, String licenseState, String practiceName) {
        validateEmailNotExists(email);
        validateLicenseNotExists(licenseNumber);

        Veterinarian veterinarian = new Veterinarian(email, encodePassword(password), firstName, lastName,
                licenseNumber, licenseState, practiceName);
        return veterinarianRepository.save(veterinarian);
    }

    // User Management Operations
    public User softDeleteUser(Long userId, Long deletedByUserId, String reason) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsActive(false);
        user.setEndDate(LocalDateTime.now());
        user.setEndReason(reason);
        user.setEndedByUserId(deletedByUserId);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedByUserId(deletedByUserId);

        return userRepository.save(user);
    }

    public User reactivateUser(Long userId, Long reactivatedByUserId) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsActive(true);
        user.setReactivatedDate(LocalDateTime.now());
        user.setReactivatedByUserId(reactivatedByUserId);
        user.setEndDate(null);
        user.setEndReason(null);
        user.setEndedByUserId(null);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedByUserId(reactivatedByUserId);

        return userRepository.save(user);
    }

    public User updateLastLogin(Long userId) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    // Utility Methods
    public String getFullName(User user) {
        if (user == null) return "";
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    public boolean isUserActive(User user) {
        return user != null && user.getIsActive() != null && user.getIsActive();
    }

    // Account Management Methods
    public List<FarmUser> findFarmUsersByAccount(Long accountId) {
        return farmUserRepository.findActiveFarmUsersByAccountId(accountId);
    }

    public Optional<FarmUser> findPrimaryUserByAccount(Long accountId) {
        return farmUserRepository.findPrimaryUserByAccountId(accountId);
    }

    public long countFarmUsersByAccount(Long accountId) {
        return farmUserRepository.countActiveFarmUsersByAccountId(accountId);
    }

    // Veterinarian Management Methods
    public List<Veterinarian> findVerifiedVeterinarians() {
        return veterinarianRepository.findActiveVerifiedVeterinarians();
    }

    public List<Veterinarian> findUnverifiedVeterinarians() {
        return veterinarianRepository.findUnverifiedVeterinarians();
    }

    public Veterinarian verifyVeterinarian(Long veterinarianId, Long verifiedByUserId) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found: " + veterinarianId));

        veterinarian.setIsVerified(true);
        veterinarian.setVerifiedByUserId(verifiedByUserId);
        veterinarian.setUpdatedAt(LocalDateTime.now());
        veterinarian.setUpdatedByUserId(verifiedByUserId);

        return veterinarianRepository.save(veterinarian);
    }

    // Private Helper Methods
    private void validateEmailNotExists(String email) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }

    private void validateAccountUserLimit(Long accountId) {
        long userCount = countFarmUsersByAccount(accountId);
        if (userCount >= 5) {
            throw new IllegalArgumentException("Account has reached the 5-user limit");
        }
    }

    private void validateNoPrimaryUserExists(Long accountId) {
        if (farmUserRepository.existsByAccountIdAndIsPrimaryUserTrue(accountId)) {
            throw new IllegalArgumentException("Account already has a primary user");
        }
    }

    private void validateLicenseNotExists(String licenseNumber) {
        if (veterinarianRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("License number already exists: " + licenseNumber);
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}