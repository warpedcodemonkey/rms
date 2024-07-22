package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Breed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BreedRepository extends JpaRepository<Breed, Long> {
}
