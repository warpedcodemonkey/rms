package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "vac_id"))
public class Vaccination extends BaseVO {

    private String vacName;
    private Date administerDate;
    private BigDecimal dosage; //In mL or cc

    // Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Add additional fields for enhanced vaccination management as per Task 6
    @Column(name = "vaccine_type")
    private String vaccineType; // injection, oral, other

    @Column(name = "administration_method")
    private String administrationMethod;

    @Column(name = "vaccine_lot_number")
    private String vaccineLotNumber;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "administered_by")
    private String administeredBy; // Veterinarian name or user

    @Column(name = "notes")
    private String notes;

    @Column(name = "next_due_date")
    private Date nextDueDate; // For vaccination schedules

    // Constructors
    public Vaccination() {}

    public Vaccination(String vacName, Date administerDate, BigDecimal dosage, Account account) {
        this.vacName = vacName;
        this.administerDate = administerDate;
        this.dosage = dosage;
        this.account = account;
    }

    // Getters and Setters
    public String getVacName() {
        return vacName;
    }

    public void setVacName(String vacName) {
        this.vacName = vacName;
    }

    public Date getAdministerDate() {
        return administerDate;
    }

    public void setAdministerDate(Date administerDate) {
        this.administerDate = administerDate;
    }

    public BigDecimal getDosage() {
        return dosage;
    }

    public void setDosage(BigDecimal dosage) {
        this.dosage = dosage;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getVaccineType() {
        return vaccineType;
    }

    public void setVaccineType(String vaccineType) {
        this.vaccineType = vaccineType;
    }

    public String getAdministrationMethod() {
        return administrationMethod;
    }

    public void setAdministrationMethod(String administrationMethod) {
        this.administrationMethod = administrationMethod;
    }

    public String getVaccineLotNumber() {
        return vaccineLotNumber;
    }

    public void setVaccineLotNumber(String vaccineLotNumber) {
        this.vaccineLotNumber = vaccineLotNumber;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getAdministeredBy() {
        return administeredBy;
    }

    public void setAdministeredBy(String administeredBy) {
        this.administeredBy = administeredBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(Date nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Vaccination.class.getSimpleName() + "[", "]")
                .add("vacName='" + vacName + "'")
                .add("administerDate=" + administerDate)
                .add("dosage=" + dosage)
                .add("vaccineType='" + vaccineType + "'")
                .add("administrationMethod='" + administrationMethod + "'")
                .add("administeredBy='" + administeredBy + "'")
                .toString();
    }
}