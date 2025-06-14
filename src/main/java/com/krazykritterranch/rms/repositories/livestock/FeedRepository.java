package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    // Find feed by account (tenant filtering)
    @Query("SELECT f FROM Feed f WHERE f.account.id = :accountId")
    List<Feed> findByAccountId(@Param("accountId") Long accountId);

    // Find feed accessible by veterinarian
    @Query("SELECT f FROM Feed f JOIN VetPermission vp ON f.account.id = vp.account.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true " +
            "AND (vp.expiresAt IS NULL OR vp.expiresAt > CURRENT_TIMESTAMP)")
    List<Feed> findByVeterinarianAccess(@Param("vetId") Long vetId);

    // Find feed by ID with account check (security)
    @Query("SELECT f FROM Feed f WHERE f.id = :id AND f.account.id = :accountId")
    Optional<Feed> findByIdAndAccount(@Param("id") Long id, @Param("accountId") Long accountId);

    // Count feed by account
    @Query("SELECT COUNT(f) FROM Feed f WHERE f.account.id = :accountId")
    long countByAccount(@Param("accountId") Long accountId);

    // Find feed by type within account
    @Query("SELECT f FROM Feed f WHERE f.feedType = :feedType AND f.account.id = :accountId")
    List<Feed> findByFeedTypeAndAccount(@Param("feedType") String feedType, @Param("accountId") Long accountId);
}