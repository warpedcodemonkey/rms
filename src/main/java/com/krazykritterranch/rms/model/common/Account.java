package com.krazykritterranch.rms.model.common;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.livestock.Livestock;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.user.VetPermission; // ADD THIS IMPORT

import jakarta.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name="id", column = @Column(name="account_id"))
public class Account extends BaseVO {

    @Column(unique = true, nullable = false)
    private String accountNumber;

    private String farmName;

    @OneToOne
    @JoinColumn(name = "primary_email_id")
    private Email accountEmail;

    @OneToOne
    @JoinColumn(name = "master_user_id")
    private User masterUser;

    // Account users (up to 5)
    @OneToMany(mappedBy = "primaryAccount", cascade = CascadeType.ALL)
    private List<User> accountUsers = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Livestock> livestock = new ArrayList<>();

    // Vet permissions - which vets can access this account
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private Set<VetPermission> vetPermissions = new HashSet<>();

    private Date signupDate;
    private Date membershipStart;
    private Date membershipEnd;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    private String recoveryPassword;

    // Constructors
    public Account() {
        this.accountNumber = generateAccountNumber();
    }

    private String generateAccountNumber() {
        return "ACCT-" + System.currentTimeMillis();
    }

    public boolean canAddUser() {
        return accountUsers.size() < 5;
    }

    public boolean hasVetAccess(Long vetId) {
        return vetPermissions.stream()
                .anyMatch(vp -> vp.getVeterinarian().getId().equals(vetId) && vp.getIsActive());
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public Email getAccountEmail() { return accountEmail; }
    public void setAccountEmail(Email accountEmail) { this.accountEmail = accountEmail; }

    public User getMasterUser() { return masterUser; }
    public void setMasterUser(User masterUser) { this.masterUser = masterUser; }

    public List<User> getAccountUsers() { return accountUsers; }
    public void setAccountUsers(List<User> accountUsers) { this.accountUsers = accountUsers; }

    public List<Livestock> getLivestock() { return livestock; }
    public void setLivestock(List<Livestock> livestock) { this.livestock = livestock; }

    public Set<VetPermission> getVetPermissions() { return vetPermissions; }
    public void setVetPermissions(Set<VetPermission> vetPermissions) { this.vetPermissions = vetPermissions; }

    public Date getSignupDate() { return signupDate; }
    public void setSignupDate(Date signupDate) { this.signupDate = signupDate; }

    public Date getMembershipStart() { return membershipStart; }
    public void setMembershipStart(Date membershipStart) { this.membershipStart = membershipStart; }

    public Date getMembershipEnd() { return membershipEnd; }
    public void setMembershipEnd(Date membershipEnd) { this.membershipEnd = membershipEnd; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public String getRecoveryPassword() { return recoveryPassword; }
    public void setRecoveryPassword(String recoveryPassword) { this.recoveryPassword = recoveryPassword; }

    @Override
    public String toString() {
        return new StringJoiner(", ", Account.class.getSimpleName() + "[", "]")
                .add("accountNumber='" + accountNumber + "'")
                .add("farmName='" + farmName + "'")
                .add("accountEmail=" + accountEmail)
                .add("masterUser=" + masterUser)
                .add("signupDate=" + signupDate)
                .add("status=" + status)
                .toString();
    }
}