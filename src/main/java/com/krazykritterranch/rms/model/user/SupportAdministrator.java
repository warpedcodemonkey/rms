package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * SupportAdministrator represents customer service representatives in the RMS system.
 *
 * SupportAdministrators provide customer support and platform assistance.
 * All business logic, validation, and permissions are handled through the Role/Permission system.
 *
 * Business Rules (handled in services):
 * - SupportAdministrators are system-level users (no account association)
 * - Can access multiple customer accounts based on assigned roles/permissions
 * - All customer account access is logged for audit purposes
 */
@Entity
@DiscriminatorValue("SUPPORT_ADMIN")
public class SupportAdministrator extends User {

    @Column(name = "employee_id", length = 50)
    @Size(max = 50, message = "Employee ID cannot exceed 50 characters")
    private String employeeId;

    @Column(name = "support_tier", nullable = false)
    @Min(value = 1, message = "Support tier must be at least 1")
    @Max(value = 5, message = "Support tier cannot exceed 5")
    private Integer supportTier = 1;

    @Column(name = "department", length = 100)
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "supervisor_user_id")
    private Long supervisorUserId;

    @Column(name = "specialization", length = 200)
    @Size(max = 200, message = "Specialization cannot exceed 200 characters")
    private String specialization;

    @Column(name = "shift_start_time", length = 10)
    private String shiftStartTime;

    @Column(name = "shift_end_time", length = 10)
    private String shiftEndTime;

    @Column(name = "timezone_support", length = 100)
    private String timezoneSupport = "America/New_York";

    @Column(name = "languages_supported", length = 200)
    private String languagesSupported = "English";

    @Column(name = "total_accounts_accessed")
    private Long totalAccountsAccessed = 0L;

    @Column(name = "last_training_date")
    private LocalDateTime lastTrainingDate;

    @Column(name = "next_training_due")
    private LocalDateTime nextTrainingDue;

    @Column(name = "performance_rating")
    @Enumerated(EnumType.STRING)
    private PerformanceRating performanceRating = PerformanceRating.SATISFACTORY;

    /**
     * Performance ratings for SupportAdministrators
     */
    public enum PerformanceRating {
        NEEDS_IMPROVEMENT("Needs Improvement"),
        SATISFACTORY("Satisfactory"),
        GOOD("Good"),
        EXCELLENT("Excellent"),
        OUTSTANDING("Outstanding");

        private final String displayName;

        PerformanceRating(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Track which accounts this support admin has accessed (for audit)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "support_admin_account_access",
            joinColumns = @JoinColumn(name = "support_admin_id"))
    @Column(name = "account_id")
    private Set<Long> accessedAccountIds = new HashSet<>();

    // Constructors
    public SupportAdministrator() {
        super();
    }

    public SupportAdministrator(String email, String password, String firstName, String lastName) {
        super(email, password, firstName, lastName);
    }

    // Abstract Method Implementations
    @Override
    public UserLevel getUserLevel() {
        return UserLevel.SUPPORT_ADMIN;
    }

    @Override
    public String getUserType() {
        return "SUPPORT_ADMIN";
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getSupportTier() {
        return supportTier;
    }

    public void setSupportTier(Integer supportTier) {
        this.supportTier = supportTier;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public Long getSupervisorUserId() {
        return supervisorUserId;
    }

    public void setSupervisorUserId(Long supervisorUserId) {
        this.supervisorUserId = supervisorUserId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(String shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public String getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(String shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public String getTimezoneSupport() {
        return timezoneSupport;
    }

    public void setTimezoneSupport(String timezoneSupport) {
        this.timezoneSupport = timezoneSupport;
    }

    public String getLanguagesSupported() {
        return languagesSupported;
    }

    public void setLanguagesSupported(String languagesSupported) {
        this.languagesSupported = languagesSupported;
    }

    public Long getTotalAccountsAccessed() {
        return totalAccountsAccessed;
    }

    public void setTotalAccountsAccessed(Long totalAccountsAccessed) {
        this.totalAccountsAccessed = totalAccountsAccessed;
    }

    public LocalDateTime getLastTrainingDate() {
        return lastTrainingDate;
    }

    public void setLastTrainingDate(LocalDateTime lastTrainingDate) {
        this.lastTrainingDate = lastTrainingDate;
    }

    public LocalDateTime getNextTrainingDue() {
        return nextTrainingDue;
    }

    public void setNextTrainingDue(LocalDateTime nextTrainingDue) {
        this.nextTrainingDue = nextTrainingDue;
    }

    public PerformanceRating getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(PerformanceRating performanceRating) {
        this.performanceRating = performanceRating;
    }

    public Set<Long> getAccessedAccountIds() {
        return accessedAccountIds;
    }

    public void setAccessedAccountIds(Set<Long> accessedAccountIds) {
        this.accessedAccountIds = accessedAccountIds;
    }

    // toString for debugging
    @Override
    public String toString() {
        return "SupportAdministrator{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", supportTier=" + supportTier +
                ", specialization='" + specialization + '\'' +
                ", totalAccountsAccessed=" + totalAccountsAccessed +
                ", isActive=" + getIsActive() +
                '}';
    }
}