package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.sql.Date;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "hc_id"))
public class HeatCycle  extends BaseVO {


    private Date startDate;
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HeatCycle.class.getSimpleName() + "[", "]")
                .add("startDate=" + startDate)
                .add("endDate=" + endDate)
                .toString();
    }
}
