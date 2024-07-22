package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;


import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import java.util.StringJoiner;
@Entity
@AttributeOverride(name = "id", column = @Column(name = "phone_id"))
public class Phone extends BaseVO {
    @NotNull
    private String areaCode;
    @NotNull
    private String exchange;
    @NotNull
    private String phoneNumber;
    private String fullNumber;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullNumber() {
        return fullNumber;
    }

    public void setFullNumber(String fullNumber) {
        this.fullNumber = fullNumber;
    }

    public static Phone formatPhone(Phone phone){
        phone.setFullNumber(new StringBuilder(phone.getAreaCode())
                .append("-")
                .append(phone.getExchange())
                .append("-")
                .append(phone.getPhoneNumber()).toString());
        return phone;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Phone.class.getSimpleName() + "[", "]")
                .add("areaCode='" + areaCode + "'")
                .add("exchange='" + exchange + "'")
                .add("phoneNumber='" + phoneNumber + "'")
                .add("fullNumber='" + fullNumber + "'")
                .toString();
    }
}
