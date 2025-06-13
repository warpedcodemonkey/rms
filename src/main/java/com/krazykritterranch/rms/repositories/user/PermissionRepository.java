package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findByIsSystemPermissionTrue();

    List<Permission> findByIsSystemPermissionFalse();

    List<Permission> findByCategory(String category);

    @Query("SELECT p FROM Permission p WHERE p.name LIKE %:name% OR p.description LIKE %:description%")
    List<Permission> findByNameOrDescriptionContaining(@Param("name") String name, @Param("description") String description);
}