package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "livestock_type_id"))
public class LivestockType extends BaseVO {

    private String livestockType;

    public String getLivestockType() {
        return livestockType;
    }

    public void setLivestockType(String livestockType) {
        this.livestockType = livestockType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LivestockType.class.getSimpleName() + "[", "]")
                .add("livestockType='" + livestockType + "'")
                .toString();
    }
}
