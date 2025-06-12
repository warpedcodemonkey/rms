package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    List<Role> findByIsSystemRoleTrue();

    List<Role> findByIsSystemRoleFalse();

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT r FROM Role r WHERE r.name LIKE %:name% OR r.description LIKE %:description%")
    List<Role> findByNameOrDescriptionContaining(@Param("name") String name, @Param("description") String description);
}