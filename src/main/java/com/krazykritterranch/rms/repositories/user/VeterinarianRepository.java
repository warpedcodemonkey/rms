package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {

    Optional<Veterinarian> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Veterinarian> findByIsActiveTrue();

    List<Veterinarian> findBySpecializationContainingIgnoreCase(String specialization);

    @Query("SELECT v FROM Veterinarian v WHERE v.clinicName LIKE %:clinicName%")
    List<Veterinarian> findByClinicNameContaining(@Param("clinicName") String clinicName);

    @Query("SELECT COUNT(v) FROM Veterinarian v WHERE v.isActive = true")
    long countActiveVeterinarians();

}
