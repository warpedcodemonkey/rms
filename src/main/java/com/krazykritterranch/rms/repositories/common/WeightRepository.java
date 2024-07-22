package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Weight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightRepository extends JpaRepository<Weight, Long> {
}
