package com.krazykritterranch.rms.model.user;

public enum SystemPermissions {
    // Customer permissions
    CUSTOMER_READ_OWN("CUSTOMER_READ_OWN", "Read own customer information", "Customer"),
    CUSTOMER_UPDATE_OWN("CUSTOMER_UPDATE_OWN", "Update own customer information", "Customer"),
    CUSTOMER_MANAGE_VET_PERMISSIONS("CUSTOMER_MANAGE_VET_PERMISSIONS", "Manage veterinarian access permissions", "Customer"),

    // Veterinarian permissions
    VET_READ_AUTHORIZED_CUSTOMERS("VET_READ_AUTHORIZED_CUSTOMERS", "Read authorized customer information", "Veterinarian"),
    VET_UPDATE_AUTHORIZED_CUSTOMERS("VET_UPDATE_AUTHORIZED_CUSTOMERS", "Update authorized customer information", "Veterinarian"),
    VET_READ_OWN("VET_READ_OWN", "Read own veterinarian information", "Veterinarian"),
    VET_UPDATE_OWN("VET_UPDATE_OWN", "Update own veterinarian information", "Veterinarian"),

    // Administrator permissions
    ADMIN_READ_ALL_USERS("ADMIN_READ_ALL_USERS", "Read all user information", "Administrator"),
    ADMIN_UPDATE_ALL_USERS("ADMIN_UPDATE_ALL_USERS", "Update all user information", "Administrator"),
    ADMIN_DELETE_USERS("ADMIN_DELETE_USERS", "Delete user accounts", "Administrator"),
    ADMIN_MANAGE_ROLES("ADMIN_MANAGE_ROLES", "Manage roles and permissions", "Administrator"),
    ADMIN_SYSTEM_SETTINGS("ADMIN_SYSTEM_SETTINGS", "Manage system settings", "Administrator"),

    // General permissions
    USER_READ_OWN_PROFILE("USER_READ_OWN_PROFILE", "Read own profile", "General"),
    USER_UPDATE_OWN_PROFILE("USER_UPDATE_OWN_PROFILE", "Update own profile", "General"),
    USER_CHANGE_PASSWORD("USER_CHANGE_PASSWORD", "Change own password", "General");

    private final String name;
    private final String description;
    private final String category;

    SystemPermissions(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
}