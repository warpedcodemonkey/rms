package com.krazykritterranch.rms.controller.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserDeleteRequest {

    @NotNull(message = "Confirmation is required")
    private Boolean confirmed;

    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    private String additionalNotes;

    // Constructors
    public UserDeleteRequest() {}

    public UserDeleteRequest(Boolean confirmed, String reason) {
        this.confirmed = confirmed;
        this.reason = reason;
    }

    // Getters and Setters
    public Boolean isConfirmed() {
        return confirmed != null && confirmed;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}