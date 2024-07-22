package com.krazykritterranch.rms.model.vendor;

import com.krazykritterranch.rms.model.BaseVO;
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

    private String vendorName;
    @ManyToMany
    @JoinTable(
            name = "vendor_emails", // Name of the junction table
            joinColumns = @JoinColumn(name = "vendor_id"), // The foreign key column for Customer
            inverseJoinColumns = @JoinColumn(name = "email_id") // The foreign key column for Email
    )
    private List<Email> emails = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "vendor_phones", // Name of the junction table
            joinColumns = @JoinColumn(name = "vendor_id"), // The foreign key column for Customer
            inverseJoinColumns = @JoinColumn(name = "phone_id") // The foreign key column for Email
    )
    private List<Phone> phones = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "vendor_addresses", // Name of the junction table
            joinColumns = @JoinColumn(name = "vendor_id"), // The foreign key column for Customer
            inverseJoinColumns = @JoinColumn(name = "address_id") // The foreign key column for Email
    )
    private List<Address> addresses = new ArrayList<>();
    private String website;

    @ManyToMany
    @JoinTable(
            name="vendor_products",
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


    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", Vendor.class.getSimpleName() + "[", "]")
                .add("vendorName='" + vendorName + "'")
                .add("emails=" + emails)
                .add("phones=" + phones)
                .add("addresses=" + addresses)
                .add("website='" + website + "'")
                .add("products=" + products)
                .add("notes=" + notes)
                .toString();
    }
}
