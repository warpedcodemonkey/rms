package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.vendor.Vendor;

import jakarta.persistence.*;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "feed_id"))
public class Feed extends BaseVO {

    private String feedName;

    // Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToOne
    @JoinColumn(name="vendor_id")
    private Vendor vendor;

    // Add feed type for categorization as mentioned in Task 7
    @Column(name = "feed_type")
    private String feedType; // grain, layer feed, hay, alfalfa, etc.

    // Add description field
    @Column(name = "description")
    private String description;

    // Constructors
    public Feed() {}

    public Feed(String feedName, Account account) {
        this.feedName = feedName;
        this.account = account;
    }

    // Getters and Setters
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getFeedType() {
        return feedType;
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Feed.class.getSimpleName() + "[", "]")
                .add("feedName='" + feedName + "'")
                .add("feedType='" + feedType + "'")
                .add("vendor=" + vendor)
                .add("description='" + description + "'")
                .toString();
    }
}