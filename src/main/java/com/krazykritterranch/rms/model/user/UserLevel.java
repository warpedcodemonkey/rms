/*
 * CORRECTED USER ARCHITECTURE FOR RMS MULTI-TENANT SAAS
 *
 * This design reflects the actual business model:
 * - SuperAdmin/SupportAdmin: Your employees (system-level)
 * - Customer: Account owner/subscriber (Bob)
 * - AccountUser: Bob's employees/family
 * - Veterinarian: External professionals with cross-account access
 */

// =======================
// USER LEVEL ENUM
// =======================

package com.krazykritterranch.rms.model.user;

public enum UserLevel {
    SUPER_ADMIN(1, "Super Administrator", "System-wide access for platform owners"),
    SUPPORT_ADMIN(2, "Support Administrator", "Customer service and support"),
    CUSTOMER(3, "Customer", "Account owner/subscriber with full account control"),
    ACCOUNT_USER(4, "Account User", "Ranch employee with delegated permissions"),
    VETERINARIAN(5, "Veterinarian", "External professional with cross-account access");

    private final int level;
    private final String displayName;
    private final String description;

    UserLevel(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public boolean canManage(UserLevel other) {
        return this.level < other.level;
    }

    public boolean requiresAccount() {
        return this == CUSTOMER || this == ACCOUNT_USER;
    }

    public boolean isSystemUser() {
        return this == SUPER_ADMIN || this == SUPPORT_ADMIN;
    }
}