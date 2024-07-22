package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Feeding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedingRepository extends JpaRepository<Feeding, Long> {

}
