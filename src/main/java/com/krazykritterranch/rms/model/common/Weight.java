package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name="id", column = @Column(name = "weight_id"))
public class Weight extends BaseVO {

    private Date weightDate;
    private BigDecimal weight; // In US pounds

    // Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Add additional fields for enhanced weight management
    @Column(name = "weight_unit")
    private String weightUnit = "lbs"; // Default to pounds, could be kg

    @Column(name = "recorded_by")
    private String recordedBy; // Who recorded the weight

    @Column(name = "weighing_method")
    private String weighingMethod; // scale type, estimation method, etc.

    @Column(name = "notes")
    private String notes; // Additional notes about the weighing

    @Column(name = "body_condition_score")
    private Integer bodyConditionScore; // 1-9 scale for livestock condition

    @Column(name = "is_estimated")
    private Boolean isEstimated = false; // Whether weight was estimated vs measured

    // Constructors
    public Weight() {}

    public Weight(Date weightDate, BigDecimal weight, Account account) {
        this.weightDate = weightDate;
        this.weight = weight;
        this.account = account;
    }

    public Weight(Date weightDate, BigDecimal weight, Account account, String recordedBy) {
        this.weightDate = weightDate;
        this.weight = weight;
        this.account = account;
        this.recordedBy = recordedBy;
    }

    // Getters and Setters
    public Date getWeightDate() {
        return weightDate;
    }

    public void setWeightDate(Date weightDate) {
        this.weightDate = weightDate;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getWeighingMethod() {
        return weighingMethod;
    }

    public void setWeighingMethod(String weighingMethod) {
        this.weighingMethod = weighingMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getBodyConditionScore() {
        return bodyConditionScore;
    }

    public void setBodyConditionScore(Integer bodyConditionScore) {
        this.bodyConditionScore = bodyConditionScore;
    }

    public Boolean getIsEstimated() {
        return isEstimated;
    }

    public void setIsEstimated(Boolean isEstimated) {
        this.isEstimated = isEstimated;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Weight.class.getSimpleName() + "[", "]")
                .add("weightDate=" + weightDate)
                .add("weight=" + weight + " " + weightUnit)
                .add("recordedBy='" + recordedBy + "'")
                .add("weighingMethod='" + weighingMethod + "'")
                .add("bodyConditionScore=" + bodyConditionScore)
                .add("isEstimated=" + isEstimated)
                .toString();
    }
}