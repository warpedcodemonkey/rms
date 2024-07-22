package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
}
