package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.vendor.Vendor;

import jakarta.persistence.*;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "feed_id"))
public class Feed extends BaseVO {
    private String feedName;
    @OneToOne
    @JoinColumn(name="vendor_id")
    private Vendor vendor;

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Feed.class.getSimpleName() + "[", "]")
                .add("feedName='" + feedName + "'")
                .add("vendor=" + vendor)
                .toString();
    }
}
