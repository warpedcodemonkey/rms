package com.krazykritterranch.rms.model.user;

/**
 * UserLevel defines the hierarchical structure of users in the RMS multi-tenant system.
 *
 * Hierarchy (lower number = higher authority):
 * 1. SUPER_ADMIN - Platform owners (your employees)
 * 2. SUPPORT_ADMIN - Customer service representatives
 * 3. CUSTOMER - Account owners/subscribers (paying customers like "Bob")
 * 4. ACCOUNT_USER - Ranch employees/family members under Customer
 * 5. VETERINARIAN - External professionals with cross-account access
 *
 * Business Rules:
 * - System users (SUPER_ADMIN, SUPPORT_ADMIN) have NO account association
 * - Account users (CUSTOMER, ACCOUNT_USER) MUST have account association
 * - Veterinarians have NO primary account but can access multiple accounts
 * - Higher levels can manage lower levels (lower number manages higher number)
 */
public enum UserLevel {

    SUPER_ADMIN(1, "Super Administrator", "Platform owners with system-wide access", true, false),
    SUPPORT_ADMIN(2, "Support Administrator", "Customer service and platform support", true, false),
    ACCOUNT_USER(3, "Account User", "Account-level user with role-based permissions", false, true),
    VETERINARIAN(4, "Veterinarian", "External professional with cross-account medical access", false, false);

    private final int level;
    private final String displayName;
    private final String description;
    private final boolean isSystemUser;
    private final boolean requiresAccount;

    UserLevel(int level, String displayName, String description, boolean isSystemUser, boolean requiresAccount) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
        this.isSystemUser = isSystemUser;
        this.requiresAccount = requiresAccount;
    }

    // Getters
    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystemUser() {
        return isSystemUser;
    }

    public boolean requiresAccount() {
        return requiresAccount;
    }

    /**
     * Determines if this user level can manage (create, modify, delete) another user level.
     * Lower level numbers have higher authority.
     *
     * @param other The user level to check management authority against
     * @return true if this level can manage the other level
     */
    public boolean canManage(UserLevel other) {
        return this.level < other.level;
    }

    /**
     * Determines if this user level can view/access another user level.
     * Generally, users can access same level and lower levels.
     *
     * @param other The user level to check access authority against
     * @return true if this level can access the other level
     */
    public boolean canAccess(UserLevel other) {
        return this.level <= other.level;
    }

    /**
     * Determines if this user level requires an account association.
     * System users operate across all accounts.
     * Account users belong to a specific account.
     * Veterinarians have no primary account but access multiple accounts via permissions.
     *
     * @return true if this user type must be associated with an account
     */
    public boolean requiresAccountAssociation() {
        return requiresAccount;
    }

    /**
     * Determines if this user level is a system-level user (platform operator).
     * System users can access multiple accounts for administration/support.
     *
     * @return true if this is a system-level user
     */
    public boolean isSystemLevel() {
        return isSystemUser;
    }

    /**
     * Determines if this user level can create other users.
     *
     * @return true if this level can create users
     */
    public boolean canCreateUsers() {
        return this == SUPER_ADMIN || this == SUPPORT_ADMIN;
        // Note: ACCOUNT_USER creation rights depend on having "Account Admin" role
    }

    /**
     * Determines if this user level can modify system settings.
     *
     * @return true if this level can modify system settings
     */
    public boolean canModifySystemSettings() {
        return this == SUPER_ADMIN;
    }

    /**
     * Determines if this user level can access multiple accounts.
     *
     * @return true if this level can access multiple accounts
     */
    public boolean canAccessMultipleAccounts() {
        return this == SUPER_ADMIN || this == SUPPORT_ADMIN || this == VETERINARIAN;
    }

    /**
     * Get the maximum number of users this level can create per account.
     * Only applicable to ACCOUNT_USER level with account admin permissions.
     *
     * @return maximum users that can be created, -1 for unlimited (system users)
     */
    public int getMaxUsersPerAccount() {
        switch (this) {
            case SUPER_ADMIN:
            case SUPPORT_ADMIN:
                return -1; // Unlimited
            case ACCOUNT_USER:
                return 4; // Account admin can create 4 additional users (5 total per account)
            case VETERINARIAN:
                return 0; // Cannot create users
            default:
                return 0;
        }
    }

    /**
     * Get the default role name for this user level.
     * Used for initial role assignment.
     *
     * @return default role name
     */
    public String getDefaultRoleName() {
        switch (this) {
            case SUPER_ADMIN:
                return "ROLE_SUPER_ADMIN";
            case SUPPORT_ADMIN:
                return "ROLE_SUPPORT_ADMIN";
            case ACCOUNT_USER:
                return "ROLE_ACCOUNT_USER"; // Will get additional roles based on permissions
            case VETERINARIAN:
                return "ROLE_VETERINARIAN";
            default:
                return "ROLE_USER";
        }
    }

    /**
     * Validates if a user with this level can be assigned to the specified account.
     *
     * @param hasAccount whether an account is being assigned
     * @return true if the assignment is valid
     */
    public boolean validateAccountAssignment(boolean hasAccount) {
        if (requiresAccount && !hasAccount) {
            return false; // Account required but not provided
        }
        if (isSystemUser && hasAccount) {
            return false; // System user cannot have account
        }
        return true;
    }

    @Override
    public String toString() {
        return displayName + " (Level " + level + ")";
    }
}