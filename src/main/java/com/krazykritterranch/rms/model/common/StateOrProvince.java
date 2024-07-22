package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "state_province_id"))
public class StateOrProvince  extends BaseVO {
    private String name;
    private String abbr;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StateOrProvince.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("abbr='" + abbr + "'")
                .toString();
    }
}
