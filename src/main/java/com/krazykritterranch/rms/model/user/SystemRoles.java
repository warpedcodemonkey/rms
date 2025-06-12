package com.krazykritterranch.rms.model.user;

public enum SystemRoles {
    CUSTOMER("CUSTOMER", "Standard customer role"),
    VETERINARIAN("VETERINARIAN", "Standard veterinarian role"),
    ADMINISTRATOR("ADMINISTRATOR", "Standard administrator role"),
    SUPER_ADMIN("SUPER_ADMIN", "Super administrator with full access");

    private final String name;
    private final String description;

    SystemRoles(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}