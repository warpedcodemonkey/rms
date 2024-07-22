package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "feeding_id"))
public class Feeding extends BaseVO {
    private Date feedingDate;
    private BigDecimal feedAmount; //In US pounds
    @OneToOne
    @JoinColumn(name = "feed_id")
    private Feed feed;

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

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Feeding.class.getSimpleName() + "[", "]")
                .add("feedingDate=" + feedingDate)
                .add("feedAmount=" + feedAmount)
                .add("feed=" + feed)
                .toString();
    }
}
