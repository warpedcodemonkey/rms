package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Livestock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivestockRepository extends JpaRepository<Livestock, Long> {

    // Find livestock by account
    @Query("SELECT l FROM Livestock l WHERE l.account.id = :accountId")
    List<Livestock> findByAccountId(@Param("accountId") Long accountId);

    // Find livestock accessible by veterinarian
    @Query("SELECT l FROM Livestock l JOIN VetPermission vp ON l.account.id = vp.account.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true " +
            "AND (vp.expiresAt IS NULL OR vp.expiresAt > CURRENT_TIMESTAMP)")
    List<Livestock> findByVeterinarianAccess(@Param("vetId") Long vetId);

    // Find livestock by tag ID within account
    @Query("SELECT l FROM Livestock l WHERE l.tagId = :tagId AND l.account.id = :accountId")
    List<Livestock> findByTagIdAndAccount(@Param("tagId") String tagId, @Param("accountId") Long accountId);

    // Find livestock by breed within account
    @Query("SELECT l FROM Livestock l WHERE l.breed.id = :breedId AND l.account.id = :accountId")
    List<Livestock> findByBreedAndAccount(@Param("breedId") Long breedId, @Param("accountId") Long accountId);

    // Count livestock by account
    @Query("SELECT COUNT(l) FROM Livestock l WHERE l.account.id = :accountId")
    long countByAccount(@Param("accountId") Long accountId);
}