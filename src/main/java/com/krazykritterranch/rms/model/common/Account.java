package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.livestock.Livestock;
import com.krazykritterranch.rms.model.user.User;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name="id", column = @Column(name="account_id"))
public class Account extends BaseVO {
    @OneToOne
    private Email accountEmail;

    @OneToOne
    private User masterUser;

    private String recoveryPassword;

    @ManyToMany
    @JoinTable(
            name = "account_users",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "account_livestock",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "livestock_id")
    )
    private List<Livestock> livestock = new ArrayList<>();

    private Date signupDate;
    private Date membershipStart;
    private Date membershipEnd;

    public Email getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(Email accountEmail) {
        this.accountEmail = accountEmail;
    }

    public User getMasterUser() {
        return masterUser;
    }

    public void setMasterUser(User masterUser) {
        this.masterUser = masterUser;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Livestock> getLivestock() {
        return livestock;
    }

    public void setLivestock(List<Livestock> livestock) {
        this.livestock = livestock;
    }

    public Date getSignupDate() {
        return signupDate;
    }

    public void setSignupDate(Date signupDate) {
        this.signupDate = signupDate;
    }

    public Date getMembershipStart() {
        return membershipStart;
    }

    public void setMembershipStart(Date membershipStart) {
        this.membershipStart = membershipStart;
    }

    public Date getMembershipEnd() {
        return membershipEnd;
    }

    public void setMembershipEnd(Date membershipEnd) {
        this.membershipEnd = membershipEnd;
    }

    public String getRecoveryPassword() {
        return recoveryPassword;
    }

    public void setRecoveryPassword(String recoveryPassword) {
        this.recoveryPassword = recoveryPassword;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Account.class.getSimpleName() + "[", "]")
                .add("accountEmail=" + accountEmail)
                .add("masterUser=" + masterUser)
                .add("recoveryPassword='" + recoveryPassword + "'")
                .add("users=" + users)
                .add("livestock=" + livestock)
                .add("signupDate=" + signupDate)
                .add("membershipStart=" + membershipStart)
                .add("membershipEnd=" + membershipEnd)
                .toString();
    }
}
