package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "email_id"))
public class Email extends BaseVO {
    private String emailAccount;
    private String domain;
    private String tld;

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
        if (emailAccount != null && domain != null && tld != null) {
            return new StringBuilder().append(this.getEmailAccount())
                    .append("@")
                    .append(this.getDomain())
                    .append(".")
                    .append(this.getTld())
                    .toString();
        }
        return null;
    }

    public void setEmailAddress(String emailAddress) {
        if (emailAddress != null && emailAddress.contains("@") && emailAddress.contains(".")) {
            String[] parts = emailAddress.split("@");
            if (parts.length == 2) {
                this.emailAccount = parts[0];
                String[] domainParts = parts[1].split("\\.");
                if (domainParts.length >= 2) {
                    this.domain = domainParts[0];
                    this.tld = domainParts[domainParts.length - 1];
                    // Handle cases like sub.domain.com
                    if (domainParts.length > 2) {
                        StringBuilder domainBuilder = new StringBuilder();
                        for (int i = 0; i < domainParts.length - 1; i++) {
                            if (i > 0) domainBuilder.append(".");
                            domainBuilder.append(domainParts[i]);
                        }
                        this.domain = domainBuilder.toString();
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Email.class.getSimpleName() + "[", "]")
                .add("emailAccount='" + emailAccount + "'")
                .add("domain='" + domain + "'")
                .add("tld='" + tld + "'")
                .add("emailAddress='" + getEmailAddress() + "'")
                .toString();
    }
}