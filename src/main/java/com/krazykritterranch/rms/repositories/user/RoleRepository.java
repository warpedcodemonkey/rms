package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Role;
import com.krazykritterranch.rms.model.user.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RoleRepository provides data access operations for Role entities.
 *
 * This repository handles basic CRUD operations and common queries for roles
 * in the RBAC system. Business logic is handled in service layers.
 *
 * Roles group permissions together for easier user management.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Basic role lookups
    Optional<Role> findByName(String name);
    boolean existsByName(String name);

    // Exclude specific role when checking uniqueness (for updates)
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.name = :name AND r.id != :excludeId")
    boolean existsByNameExcludingId(@Param("name") String name, @Param("excludeId") Long excludeId);

    // Find by active status
    List<Role> findByIsActive(Boolean isActive);
    List<Role> findByIsActiveOrderByDisplayOrder(Boolean isActive);

    // System vs custom roles
    List<Role> findByIsSystemRole(Boolean isSystemRole);
    List<Role> findByIsSystemRoleAndIsActive(Boolean isSystemRole, Boolean isActive);

    // Default roles for user level assignment
    List<Role> findByIsDefaultRole(Boolean isDefaultRole);
    List<Role> findByIsDefaultRoleAndIsActive(Boolean isDefaultRole, Boolean isActive);

    // Find roles by user level
    List<Role> findByUserLevel(UserLevel userLevel);
    List<Role> findByUserLevelAndIsActive(UserLevel userLevel, Boolean isActive);

    // Search roles by name or description
    @Query("SELECT r FROM Role r WHERE " +
            "(LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND r.isActive = true " +
            "ORDER BY r.displayOrder, r.name")
    List<Role> searchRoles(@Param("searchTerm") String searchTerm);

    // Find roles with specific permission
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName AND r.isActive = true")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    // Find roles with permissions in specific category
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.category = :category AND r.isActive = true ORDER BY r.displayOrder")
    List<Role> findByPermissionCategory(@Param("category") String category);

    // Find roles assigned to specific user
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId AND r.isActive = true")
    List<Role> findByUserId(@Param("userId") Long userId);

    // Find roles available for specific user level
    @Query("SELECT r FROM Role r WHERE " +
            "(r.userLevel = :userLevel OR r.userLevel IS NULL) " +
            "AND r.isActive = true " +
            "ORDER BY r.displayOrder, r.name")
    List<Role> findAvailableForUserLevel(@Param("userLevel") UserLevel userLevel);

    // Count queries for statistics
    @Query("SELECT COUNT(r) FROM Role r WHERE r.isActive = true")
    long countActiveRoles();

    @Query("SELECT COUNT(r) FROM Role r WHERE r.isSystemRole = true AND r.isActive = true")
    long countSystemRoles();

    @Query("SELECT COUNT(r) FROM Role r WHERE r.isSystemRole = false AND r.isActive = true")
    long countCustomRoles();

    // Count users with specific role
    @Query("SELECT COUNT(DISTINCT u) FROM Role r JOIN r.users u WHERE r.id = :roleId AND u.isActive = true")
    long countActiveUsersWithRole(@Param("roleId") Long roleId);

    // Check if role can accommodate more users (based on maxUsersWithRole)
    @Query("SELECT r FROM Role r WHERE r.id = :roleId AND " +
            "(r.maxUsersWithRole IS NULL OR " +
            "(SELECT COUNT(DISTINCT u) FROM Role r2 JOIN r2.users u WHERE r2.id = :roleId AND u.isActive = true) < r.maxUsersWithRole)")
    Optional<Role> findIfCanAccommodateMoreUsers(@Param("roleId") Long roleId);

    // Find roles created by specific user
    List<Role> findByCreatedByUserId(Long createdByUserId);

    // Find roles created within date range
    @Query("SELECT r FROM Role r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Role> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find roles ordered by display order
    @Query("SELECT r FROM Role r WHERE r.isActive = true ORDER BY r.displayOrder, r.name")
    List<Role> findAllActiveOrderedByDisplayOrder();

    // Find roles for dropdown/selection lists (minimal data)
    @Query("SELECT new com.krazykritterranch.rms.model.user.Role(r.id, r.name, r.displayName) FROM Role r WHERE r.isActive = true ORDER BY r.displayOrder, r.name")
    List<Role> findRoleSelectionList();

    // Eager fetch with permissions for complete role data
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :roleId")
    Optional<Role> findByIdWithPermissions(@Param("roleId") Long roleId);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    // Find roles without any permissions (for validation/cleanup)
    @Query("SELECT r FROM Role r WHERE r.permissions IS EMPTY AND r.isActive = true")
    List<Role> findRolesWithoutPermissions();

    // Find orphaned roles (no active users)
    @Query("SELECT r FROM Role r WHERE r.isSystemRole = false AND r.isActive = true AND " +
            "NOT EXISTS (SELECT 1 FROM User u JOIN u.roles ur WHERE ur.id = r.id AND u.isActive = true)")
    List<Role> findOrphanedCustomRoles();

    // Bulk operations support
    @Query("SELECT r FROM Role r WHERE r.id IN :roleIds")
    List<Role> findByIds(@Param("roleIds") List<Long> roleIds);

    // Find roles that need permission updates (system roles modified by users)
    @Query("SELECT r FROM Role r WHERE r.isSystemRole = true AND r.updatedByUserId IS NOT NULL AND r.updatedAt > r.createdAt")
    List<Role> findModifiedSystemRoles();

    // Performance optimization queries
    @Query("SELECT r.id, r.name, COUNT(u) as userCount FROM Role r LEFT JOIN r.users u WHERE r.isActive = true GROUP BY r.id, r.name")
    List<Object[]> getRoleUsageStatistics();
}