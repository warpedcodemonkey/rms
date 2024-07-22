package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "email_id"))
public class Email extends BaseVO {
    private String emailAccount;
    private String domain;
    private String tld;
    private String emailAddress;

    public String getEmailAccount() {
        return emailAccount;
    }

    public void setEmailAccount(String emailAccount) {
        this.emailAccount = emailAccount;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTld() {
        return tld;
    }

    public void setTld(String tld) {
        this.tld = tld;
    }

    public String getEmailAddress() {
        return new StringBuilder().append(this.getEmailAccount())
                .append("@")
                .append(this.getDomain())
                .append(".")
                .append(this.tld)
                .toString();
    }

    public void setEmailAddress(String emailAddress){
        this.emailAddress = getEmailAddress();
    }



    @Override
    public String toString() {
        return new StringJoiner(", ", Email.class.getSimpleName() + "[", "]")
                .add("emailAccount='" + emailAccount + "'")
                .add("domain='" + domain + "'")
                .add("tld='" + tld + "'")
                .toString();
    }
}
