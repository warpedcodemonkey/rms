package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Veterinarian represents external veterinary professionals in the RMS system.
 *
 * Veterinarians are independent professionals who can be granted access to
 * specific customer accounts for medical record management.
 * All business logic and validation is handled in service layers.
 *
 * Business Rules (handled in services):
 * - Veterinarians are system-level users (no account association)
 * - Can be granted access to multiple customer accounts
 * - Access is controlled through VetPermission entities
 * - Can only access medical and health-related data
 */
@Entity
@DiscriminatorValue("VETERINARIAN")
public class Veterinarian extends User {

    @Column(name = "vet_license_number", unique = true, length = 100)
    @Size(max = 100, message = "License number cannot exceed 100 characters")
    private String vetLicenseNumber;

    @Column(name = "practice_name", length = 200)
    @Size(max = 200, message = "Practice name cannot exceed 200 characters")
    private String practiceName;

    @Column(name = "specialization", length = 200)
    @Size(max = 200, message = "Specialization cannot exceed 200 characters")
    private String specialization;

    @Column(name = "license_state", length = 50)
    @Size(max = 50, message = "License state cannot exceed 50 characters")
    private String licenseState;

    @Column(name = "practice_address", length = 500)
    @Size(max = 500, message = "Practice address cannot exceed 500 characters")
    private String practiceAddress;

    @Column(name = "practice_phone", length = 20)
    @Size(max = 20, message = "Practice phone cannot exceed 20 characters")
    private String practicePhone;

    @Column(name = "practice_email", length = 255)
    @Size(max = 255, message = "Practice email cannot exceed 255 characters")
    private String practiceEmail;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "license_expiration_date")
    private LocalDateTime licenseExpirationDate;

    @Column(name = "is_accepting_new_clients")
    private Boolean isAcceptingNewClients = true;

    @Column(name = "emergency_contact_available")
    private Boolean emergencyContactAvailable = false;

    @Column(name = "after_hours_phone", length = 20)
    @Size(max = 20, message = "After hours phone cannot exceed 20 characters")
    private String afterHoursPhone;

    @Column(name = "website_url", length = 500)
    @Size(max = 500, message = "Website URL cannot exceed 500 characters")
    private String websiteUrl;

    @Column(name = "professional_bio", length = 1000)
    @Size(max = 1000, message = "Professional bio cannot exceed 1000 characters")
    private String professionalBio;

    @Column(name = "total_accounts_accessed")
    private Long totalAccountsAccessed = 0L;

    @Column(name = "last_license_verification")
    private LocalDateTime lastLicenseVerification;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    /**
     * Verification status for veterinarian licenses
     */
    public enum VerificationStatus {
        PENDING("Pending", "License verification in progress"),
        VERIFIED("Verified", "License has been verified"),
        EXPIRED("Expired", "License has expired"),
        SUSPENDED("Suspended", "License is suspended"),
        REVOKED("Revoked", "License has been revoked");

        private final String displayName;
        private final String description;

        VerificationStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    // Constructors
    public Veterinarian() {
        super();
    }

    public Veterinarian(String email, String password, String firstName, String lastName) {
        super(email, password, firstName, lastName);
    }

    // Abstract Method Implementations
    @Override
    public UserLevel getUserLevel() {
        return UserLevel.VETERINARIAN;
    }

    @Override
    public String getUserType() {
        return "VETERINARIAN";
    }

    // Getters and Setters
    public String getVetLicenseNumber() {
        return vetLicenseNumber;
    }

    public void setVetLicenseNumber(String vetLicenseNumber) {
        this.vetLicenseNumber = vetLicenseNumber;
    }

    public String getPracticeName() {
        return practiceName;
    }

    public void setPracticeName(String practiceName) {
        this.practiceName = practiceName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseState() {
        return licenseState;
    }

    public void setLicenseState(String licenseState) {
        this.licenseState = licenseState;
    }

    public String getPracticeAddress() {
        return practiceAddress;
    }

    public void setPracticeAddress(String practiceAddress) {
        this.practiceAddress = practiceAddress;
    }

    public String getPracticePhone() {
        return practicePhone;
    }

    public void setPracticePhone(String practicePhone) {
        this.practicePhone = practicePhone;
    }

    public String getPracticeEmail() {
        return practiceEmail;
    }

    public void setPracticeEmail(String practiceEmail) {
        this.practiceEmail = practiceEmail;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public LocalDateTime getLicenseExpirationDate() {
        return licenseExpirationDate;
    }

    public void setLicenseExpirationDate(LocalDateTime licenseExpirationDate) {
        this.licenseExpirationDate = licenseExpirationDate;
    }

    public Boolean getIsAcceptingNewClients() {
        return isAcceptingNewClients;
    }

    public void setIsAcceptingNewClients(Boolean isAcceptingNewClients) {
        this.isAcceptingNewClients = isAcceptingNewClients;
    }

    public Boolean getEmergencyContactAvailable() {
        return emergencyContactAvailable;
    }

    public void setEmergencyContactAvailable(Boolean emergencyContactAvailable) {
        this.emergencyContactAvailable = emergencyContactAvailable;
    }

    public String getAfterHoursPhone() {
        return afterHoursPhone;
    }

    public void setAfterHoursPhone(String afterHoursPhone) {
        this.afterHoursPhone = afterHoursPhone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getProfessionalBio() {
        return professionalBio;
    }

    public void setProfessionalBio(String professionalBio) {
        this.professionalBio = professionalBio;
    }

    public Long getTotalAccountsAccessed() {
        return totalAccountsAccessed;
    }

    public void setTotalAccountsAccessed(Long totalAccountsAccessed) {
        this.totalAccountsAccessed = totalAccountsAccessed;
    }

    public LocalDateTime getLastLicenseVerification() {
        return lastLicenseVerification;
    }

    public void setLastLicenseVerification(LocalDateTime lastLicenseVerification) {
        this.lastLicenseVerification = lastLicenseVerification;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    // toString for debugging
    @Override
    public String toString() {
        return "Veterinarian{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", vetLicenseNumber='" + vetLicenseNumber + '\'' +
                ", practiceName='" + practiceName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", licenseState='" + licenseState + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", isActive=" + getIsActive() +
                '}';
    }
}