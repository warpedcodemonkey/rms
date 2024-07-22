package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name="id", column = @Column(name = "weight_id"))
public class Weight extends BaseVO {
    private Date weightDate;
    private BigDecimal weight;

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

    @Override
    public String toString() {
        return new StringJoiner(", ", Weight.class.getSimpleName() + "[", "]")
                .add("weightDate=" + weightDate)
                .add("weight=" + weight)
                .toString();
    }
}
