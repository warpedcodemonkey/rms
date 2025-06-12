package com.krazykritterranch.rms.model.user;

import jakarta.persistence.*;
import java.util.*;

@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    @Column(name = "customer_number", unique = true)
    private String customerNumber;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "emergency_phone")
    private String emergencyPhone;

    // Many-to-Many relationship for vet permissions
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "customer_vet_permissions",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "vet_id")
    )
    private Set<Veterinarian> authorizedVets = new HashSet<>();

    // Constructors
    public Customer() {
        super();
    }

    public Customer(String username, String email, String password, String firstName, String lastName) {
        super(username, email, password, firstName, lastName);
        this.customerNumber = generateCustomerNumber();
    }

    @Override
    public String getUserType() {
        return "CUSTOMER";
    }

    private String generateCustomerNumber() {
        return "CUST-" + System.currentTimeMillis();
    }

    public boolean hasAuthorizedVet(Long vetId) {
        return authorizedVets.stream().anyMatch(vet -> vet.getId().equals(vetId));
    }

    // Getters and Setters
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }

    public Set<Veterinarian> getAuthorizedVets() { return authorizedVets; }
    public void setAuthorizedVets(Set<Veterinarian> authorizedVets) { this.authorizedVets = authorizedVets; }
}