package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // Account-based queries
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId")
    List<User> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.primaryAccount.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);

    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ADMIN' OR r.name = 'SUPER_ADMIN'")
    List<User> findAdministrators();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'VETERINARIAN'")
    List<User> findVeterinarians();

    // Veterinarian access queries - users from accounts that a vet has access to
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN VetPermission vp ON u.primaryAccount.id = vp.account.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true")
    List<User> findByVeterinarianAccess(@Param("vetId") Long vetId);

    // Active/inactive users
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findActiveUsers();

    @Query("SELECT u FROM User u WHERE u.isActive = false")
    List<User> findInactiveUsers();

    // Users by account with active filter
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId AND u.isActive = :isActive")
    List<User> findByAccountIdAndActive(@Param("accountId") Long accountId, @Param("isActive") Boolean isActive);

    // Search users by name or email within an account
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchUsersInAccount(@Param("accountId") Long accountId, @Param("searchTerm") String searchTerm);

    // Global search for administrators
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchAllUsers(@Param("searchTerm") String searchTerm);

    // Find users with specific roles
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    // Find users without a primary account (system users like admins, vets)
    @Query("SELECT u FROM User u WHERE u.primaryAccount IS NULL")
    List<User> findSystemUsers();

    // Find users by account and role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.primaryAccount.id = :accountId AND r.name = :roleName")
    List<User> findByAccountIdAndRole(@Param("accountId") Long accountId, @Param("roleName") String roleName);
}
