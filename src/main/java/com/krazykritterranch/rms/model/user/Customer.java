package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.common.Account;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    @Column(name = "customer_number", unique = true)
    private String customerNumber;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_tier")
    private String subscriptionTier;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_id")
    private String taxId;

    public Customer() {
        super();
        this.customerNumber = generateCustomerNumber();
    }

    public Customer(String email, String password, String firstName, String lastName, Account account) {
        super();
        setUsername(email); // Username should be email for customers
        setEmail(email);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setPrimaryAccount(account); // Customer MUST have an account
        this.customerNumber = generateCustomerNumber();
        this.subscriptionStartDate = LocalDateTime.now();
        this.billingEmail = email;
    }

    @Override
    public UserLevel getUserLevel() {
        return UserLevel.CUSTOMER;
    }

    @Override
    public String getUserTypeString() {
        return "CUSTOMER";
    }

    private String generateCustomerNumber() {
        return "CUST-" + System.currentTimeMillis();
    }

    // Customer-specific methods
    public boolean canCreateAccountUsers() {
        Account account = getPrimaryAccount();
        return account != null && account.getUserCount() < account.getMaxUsers();
    }

    public int getRemainingUserSlots() {
        Account account = getPrimaryAccount();
        if (account == null) return 0;
        return account.getMaxUsers() - account.getUserCount();
    }

    // Customer-specific fields
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public LocalDateTime getSubscriptionStartDate() { return subscriptionStartDate; }
    public void setSubscriptionStartDate(LocalDateTime subscriptionStartDate) { this.subscriptionStartDate = subscriptionStartDate; }

    public String getSubscriptionTier() { return subscriptionTier; }
    public void setSubscriptionTier(String subscriptionTier) { this.subscriptionTier = subscriptionTier; }

    public String getBillingEmail() { return billingEmail; }
    public void setBillingEmail(String billingEmail) { this.billingEmail = billingEmail; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
}