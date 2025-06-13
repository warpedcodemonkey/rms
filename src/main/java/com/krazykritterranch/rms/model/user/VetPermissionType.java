package com.krazykritterranch.rms.model.user;

public enum VetPermissionType {
    VIEW_LIVESTOCK("View livestock information"),
    EDIT_LIVESTOCK("Edit livestock information"),
    VIEW_MEDICAL_RECORDS("View medical records"),
    EDIT_MEDICAL_RECORDS("Edit medical records"),
    ADD_VACCINATIONS("Add vaccination records"),
    VIEW_BREEDING_RECORDS("View breeding records"),
    EDIT_BREEDING_RECORDS("Edit breeding records"),
    VIEW_WEIGHTS("View weight records"),
    ADD_WEIGHTS("Add weight records"),
    VIEW_NOTES("View notes"),
    ADD_NOTES("Add notes");

    private final String description;

    VetPermissionType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}