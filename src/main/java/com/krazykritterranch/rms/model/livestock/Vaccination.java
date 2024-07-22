package com.krazykritterranch.rms.model.livestock;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "vac_id"))
public class Vaccination extends BaseVO {
    private String vacName;
    private Date administerDate;
    private BigDecimal dosage; //In mL or cc

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

    @Override
    public String toString() {
        return new StringJoiner(", ", Vaccination.class.getSimpleName() + "[", "]")
                .add("vacName='" + vacName + "'")
                .add("administerDate=" + administerDate)
                .add("dosage=" + dosage)
                .toString();
    }
}
