package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Permission entity represents individual permissions in the RMS system.
 *
 * Permissions are fine-grained actions that can be granted to users either:
 * 1. Through their assigned roles
 * 2. As custom permissions directly assigned to the user
 *
 * All business logic and validation is handled in service layers.
 */
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "name"),
        @Index(name = "idx_permission_category", columnList = "category"),
        @Index(name = "idx_permission_system_flag", columnList = "is_system_permission"),
        @Index(name = "idx_permission_active", columnList = "is_active")
})
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name cannot exceed 100 characters")
    private String name;

    @Column(name = "display_name", nullable = false, length = 150)
    @NotBlank(message = "Display name is required")
    @Size(max = 150, message = "Display name cannot exceed 150 characters")
    private String displayName;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    @Column(name = "is_system_permission", nullable = false)
    private Boolean isSystemPermission = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "permission_level")
    @Enumerated(EnumType.STRING)
    private PermissionLevel permissionLevel = PermissionLevel.STANDARD;

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    // Relationships
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "customPermissions", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    /**
     * Permission levels indicate the sensitivity/importance of the permission
     */
    public enum PermissionLevel {
        BASIC("Basic", "Low-risk permissions for basic operations"),
        STANDARD("Standard", "Normal permissions for regular operations"),
        ELEVATED("Elevated", "Higher-risk permissions requiring additional validation"),
        CRITICAL("Critical", "High-risk permissions affecting system security or data integrity");

        private final String displayName;
        private final String description;

        PermissionLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    /**
     * Standard permission categories used in the system
     */
    public static class Categories {
        public static final String LIVESTOCK = "LIVESTOCK";
        public static final String MEDICAL = "MEDICAL";
        public static final String FEEDING = "FEEDING";
        public static final String BUSINESS = "BUSINESS";
        public static final String VENDOR = "VENDOR";
        public static final String USER = "USER";
        public static final String REPORT = "REPORT";
        public static final String SYSTEM = "SYSTEM";
        public static final String ACCOUNT = "ACCOUNT";
        public static final String SUPPORT = "SUPPORT";
    }

    // Constructors
    public Permission() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Permission(String name, String displayName, String description, String category) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.isSystemPermission = false;
    }

    // JPA Lifecycle Methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsSystemPermission() { return isSystemPermission; }
    public void setIsSystemPermission(Boolean isSystemPermission) { this.isSystemPermission = isSystemPermission; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public PermissionLevel getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(PermissionLevel permissionLevel) { this.permissionLevel = permissionLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Long getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(Long updatedByUserId) { this.updatedByUserId = updatedByUserId; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }

    // toString, equals, and hashCode
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", category='" + category + '\'' +
                ", isSystemPermission=" + isSystemPermission +
                ", permissionLevel=" + permissionLevel +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}