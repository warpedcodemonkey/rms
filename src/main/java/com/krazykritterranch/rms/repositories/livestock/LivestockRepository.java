package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Livestock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivestockRepository extends JpaRepository<Livestock, Long> {
}
