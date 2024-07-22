package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.Email;
import com.krazykritterranch.rms.model.common.Phone;
import com.krazykritterranch.rms.model.order.Contract;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class User extends BaseVO {
    private String userName;
    private String firstName;
    private String lastName;
    private String passWord;
    private Boolean active;
    @ManyToMany
    @JoinTable(
            name = "customer_emails", // Name of the junction table
            joinColumns = @JoinColumn(name = "customer_id"), // The foreign key column for Customer
            inverseJoinColumns = @JoinColumn(name = "email_id") // The foreign key column for Email
    )
    private List<Email> emails = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "customer_phones", // Name of the junction table
            joinColumns = @JoinColumn(name = "customer_id"), // The foreign key column for Customer
            inverseJoinColumns = @JoinColumn(name = "phone_id") // The foreign key column for Email
    )
    private List<Phone> phones = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "customer_addresses", // Name of the junction table
            joinColumns = @JoinColumn(name = "customer_id"), // The foreign key column for Customer
            inverseJoinColumns = @JoinColumn(name = "address_id") // The foreign key column for Email
    )
    private List<Address> addresses = new ArrayList<>();
    private Date dateOfBirth;
    @ManyToMany
    @JoinTable(
            name="customer_contracts",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "contract_id")
    )
    private List<Contract> contracts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name="user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private List<Role> roles = new ArrayList<>();

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public void setContracts(List<Contract> contracts) {
        this.contracts = contracts;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("userName='" + userName + "'")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("passWord='" + passWord + "'")
                .add("active=" + active)
                .add("emails=" + emails)
                .add("phones=" + phones)
                .add("addresses=" + addresses)
                .add("dateOfBirth=" + dateOfBirth)
                .add("contracts=" + contracts)
                .add("roles=" + roles)
                .toString();
    }
}
