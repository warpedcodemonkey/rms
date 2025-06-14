package com.krazykritterranch.rms.service.user;

import com.krazykritterranch.rms.controller.user.dto.UserCreationDTO;
import com.krazykritterranch.rms.model.user.*;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User createUser(UserCreationDTO dto) {
        User user;

        switch (dto.getUserType().toUpperCase()) {
            case "CUSTOMER":
                Customer customer = new Customer(
                        dto.getUsername(),
                        dto.getEmail(),
                        dto.getPassword(),
                        dto.getFirstName(),
                        dto.getLastName()
                );
                customer.setCustomerNumber(dto.getCustomerNumber());
                customer.setEmergencyContact(dto.getEmergencyContact());
                customer.setEmergencyPhone(dto.getEmergencyPhone());
                user = customer;
                break;

            case "ADMINISTRATOR":
                Administrator admin = new Administrator(
                        dto.getUsername(),
                        dto.getEmail(),
                        dto.getPassword(),
                        dto.getFirstName(),
                        dto.getLastName()
                );
                admin.setDepartment(dto.getDepartment());
                admin.setAccessLevel(dto.getAccessLevel());
                user = admin;
                break;

            case "VETERINARIAN":
                Veterinarian vet = new Veterinarian(
                        dto.getUsername(),
                        dto.getEmail(),
                        dto.getPassword(),
                        dto.getFirstName(),
                        dto.getLastName(),
                        dto.getLicenseNumber()
                );
                vet.setLicenseNumber(dto.getLicenseNumber());
                vet.setSpecialization(dto.getSpecialization());
                vet.setClinicName(dto.getClinicName());
                vet.setClinicAddress(dto.getClinicAddress());
                vet.setYearsExperience(dto.getYearsExperience());
                user = vet;
                break;

            default:
                throw new IllegalArgumentException("Invalid user type: " + dto.getUserType());
        }

        // Set common fields
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setIsActive(dto.getIsActive());

        return user;
    }
}