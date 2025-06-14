package com.krazykritterranch.rms.model.vendor;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.Email;
import com.krazykritterranch.rms.model.common.Note;
import com.krazykritterranch.rms.model.common.Phone;
import com.krazykritterranch.rms.model.product.Product;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "vendor_id"))
public class Vendor extends BaseVO {

    @Column(nullable = false)
    private String vendorName;

    // CRITICAL: Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Vendor type as a relationship (account-specific types)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_type_id")
    private VendorType vendorType;

    @ManyToMany
    @JoinTable(
            name = "vendor_emails",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "email_id")
    )
    private List<Email> emails = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "vendor_phones",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "phone_id")
    )
    private List<Phone> phones = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "vendor_addresses",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "address_id")
    )
    private List<Address> addresses = new ArrayList<>();

    private String website;

    @ManyToMany
    @JoinTable(
            name = "vendor_products",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "vendor_notes",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "note_id")
    )
    private List<Note> notes = new ArrayList<>();

    // Add status field
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Add contact person
    @Column(name = "primary_contact_name")
    private String primaryContactName;

    // Constructors
    public Vendor() {}

    public Vendor(String vendorName, Account account, VendorType vendorType) {
        this.vendorName = vendorName;
        this.account = account;
        this.vendorType = vendorType;
        this.isActive = true;
    }

    // Getters and Setters
    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public VendorType getVendorType() {
        return vendorType;
    }

    public void setVendorType(VendorType vendorType) {
        this.vendorType = vendorType;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getPrimaryContactName() {
        return primaryContactName;
    }

    public void setPrimaryContactName(String primaryContactName) {
        this.primaryContactName = primaryContactName;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Vendor.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .add("vendorName='" + vendorName + "'")
                .add("vendorType='" + (vendorType != null ? vendorType.getTypeName() : "null") + "'")
                .add("account=" + (account != null ? account.getAccountNumber() : "null"))
                .add("website='" + website + "'")
                .add("isActive=" + isActive)
                .add("primaryContactName='" + primaryContactName + "'")
                .add("emails=" + emails.size())
                .add("phones=" + phones.size())
                .add("addresses=" + addresses.size())
                .add("products=" + products.size())
                .add("notes=" + notes.size())
                .toString();
    }
}