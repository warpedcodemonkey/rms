package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "feeding_id"))
public class Feeding extends BaseVO {

    private Date feedingDate;
    private BigDecimal feedAmount; //In US pounds

    // Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToOne
    @JoinColumn(name = "feed_id")
    private Feed feed;

    // Add enhanced feeding fields for Task 7 requirements
    @Column(name = "feeding_time")
    private Time feedingTime; // Time of day for feeding

    @Column(name = "feed_unit")
    private String feedUnit = "lbs"; // Default to pounds, could be kg

    @Column(name = "recorded_by")
    private String recordedBy; // Who recorded the feeding

    @Column(name = "feeding_method")
    private String feedingMethod; // hand fed, automatic feeder, etc.

    @Column(name = "notes")
    private String notes; // Additional notes about feeding

    @Column(name = "scheduled_amount")
    private BigDecimal scheduledAmount; // Planned amount vs actual amount

    @Column(name = "consumption_percentage")
    private BigDecimal consumptionPercentage; // How much was actually consumed

    @Column(name = "waste_amount")
    private BigDecimal wasteAmount; // Amount wasted/not consumed

    @Column(name = "feed_quality_score")
    private Integer feedQualityScore; // 1-10 scale for feed condition

    // Constructors
    public Feeding() {}

    public Feeding(Date feedingDate, BigDecimal feedAmount, Feed feed, Account account) {
        this.feedingDate = feedingDate;
        this.feedAmount = feedAmount;
        this.feed = feed;
        this.account = account;
    }

    public Feeding(Date feedingDate, BigDecimal feedAmount, Feed feed, Account account, String recordedBy) {
        this.feedingDate = feedingDate;
        this.feedAmount = feedAmount;
        this.feed = feed;
        this.account = account;
        this.recordedBy = recordedBy;
    }

    // Getters and Setters
    public Date getFeedingDate() {
        return feedingDate;
    }

    public void setFeedingDate(Date feedingDate) {
        this.feedingDate = feedingDate;
    }

    public BigDecimal getFeedAmount() {
        return feedAmount;
    }

    public void setFeedAmount(BigDecimal feedAmount) {
        this.feedAmount = feedAmount;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Time getFeedingTime() {
        return feedingTime;
    }

    public void setFeedingTime(Time feedingTime) {
        this.feedingTime = feedingTime;
    }

    public String getFeedUnit() {
        return feedUnit;
    }

    public void setFeedUnit(String feedUnit) {
        this.feedUnit = feedUnit;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getFeedingMethod() {
        return feedingMethod;
    }

    public void setFeedingMethod(String feedingMethod) {
        this.feedingMethod = feedingMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getScheduledAmount() {
        return scheduledAmount;
    }

    public void setScheduledAmount(BigDecimal scheduledAmount) {
        this.scheduledAmount = scheduledAmount;
    }

    public BigDecimal getConsumptionPercentage() {
        return consumptionPercentage;
    }

    public void setConsumptionPercentage(BigDecimal consumptionPercentage) {
        this.consumptionPercentage = consumptionPercentage;
    }

    public BigDecimal getWasteAmount() {
        return wasteAmount;
    }

    public void setWasteAmount(BigDecimal wasteAmount) {
        this.wasteAmount = wasteAmount;
    }

    public Integer getFeedQualityScore() {
        return feedQualityScore;
    }

    public void setFeedQualityScore(Integer feedQualityScore) {
        this.feedQualityScore = feedQualityScore;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Feeding.class.getSimpleName() + "[", "]")
                .add("feedingDate=" + feedingDate)
                .add("feedingTime=" + feedingTime)
                .add("feedAmount=" + feedAmount + " " + feedUnit)
                .add("feed=" + (feed != null ? feed.getFeedName() : "null"))
                .add("recordedBy='" + recordedBy + "'")
                .add("feedingMethod='" + feedingMethod + "'")
                .add("consumptionPercentage=" + consumptionPercentage + "%")
                .toString();
    }
}