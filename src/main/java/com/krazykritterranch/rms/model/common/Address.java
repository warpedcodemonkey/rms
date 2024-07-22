package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.*;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "address_id"))
public class Address extends BaseVO {
    private String address1;
    private String address2;
    private String address3;
    private String city;
    @ManyToOne
    @JoinColumn(name = "state_province_id")
    private StateOrProvince stateOrProvince;
    private String postalCode;

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public StateOrProvince getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(StateOrProvince stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Address.class.getSimpleName() + "[", "]")
                .add("address1='" + address1 + "'")
                .add("address2='" + address2 + "'")
                .add("address3='" + address3 + "'")
                .add("city='" + city + "'")
                .add("stateOrProvince=" + stateOrProvince)
                .add("postalCode='" + postalCode + "'")
                .toString();
    }
}
