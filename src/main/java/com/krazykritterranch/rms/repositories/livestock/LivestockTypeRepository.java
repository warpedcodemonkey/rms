package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.LivestockType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivestockTypeRepository extends JpaRepository<LivestockType, Long> {
}
