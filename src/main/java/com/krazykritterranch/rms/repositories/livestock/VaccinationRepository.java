package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
}
