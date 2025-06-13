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

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    // Find users by account
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId AND u.isActive = true")
    List<User> findByAccountId(@Param("accountId") Long accountId);

    // Find account users (excluding admin and vets)
    @Query("SELECT u FROM User u WHERE u.primaryAccount.id = :accountId AND u.isActive = true AND TYPE(u) NOT IN (Administrator, Veterinarian)")
    List<User> findAccountUsersByAccountId(@Param("accountId") Long accountId);

    // Find administrators
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN ('ADMIN', 'SUPER_ADMIN') AND u.isActive = true")
    List<User> findAdministrators();

    // Find veterinarians
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'VETERINARIAN' AND u.isActive = true")
    List<User> findVeterinarians();

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name% OR u.username LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);

    // Count users in account
    @Query("SELECT COUNT(u) FROM User u WHERE u.primaryAccount.id = :accountId AND u.isActive = true")
    long countActiveUsersByAccount(@Param("accountId") Long accountId);
}