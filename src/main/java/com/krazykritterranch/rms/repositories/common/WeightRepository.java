package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Weight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeightRepository extends JpaRepository<Weight, Long> {

    // Find weights by account (tenant filtering)
    @Query("SELECT w FROM Weight w WHERE w.account.id = :accountId")
    List<Weight> findByAccountId(@Param("accountId") Long accountId);

    // Find weights accessible by veterinarian
    @Query("SELECT w FROM Weight w JOIN VetPermission vp ON w.account.id = vp.account.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true " +
            "AND (vp.expiresAt IS NULL OR vp.expiresAt > CURRENT_TIMESTAMP)")
    List<Weight> findByVeterinarianAccess(@Param("vetId") Long vetId);

    // Find weight by ID with account check (security)
    @Query("SELECT w FROM Weight w WHERE w.id = :id AND w.account.id = :accountId")
    Optional<Weight> findByIdAndAccount(@Param("id") Long id, @Param("accountId") Long accountId);

    // Count weights by account
    @Query("SELECT COUNT(w) FROM Weight w WHERE w.account.id = :accountId")
    long countByAccount(@Param("accountId") Long accountId);

    // Find weights within a date range for account
    @Query("SELECT w FROM Weight w WHERE w.weightDate BETWEEN :startDate AND :endDate AND w.account.id = :accountId ORDER BY w.weightDate DESC")
    List<Weight> findByDateRangeAndAccount(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("accountId") Long accountId);

    // Find weights recorded by specific person within account
    @Query("SELECT w FROM Weight w WHERE w.recordedBy = :recordedBy AND w.account.id = :accountId ORDER BY w.weightDate DESC")
    List<Weight> findByRecordedByAndAccount(@Param("recordedBy") String recordedBy, @Param("accountId") Long accountId);

    // Find weights by weighing method within account
    @Query("SELECT w FROM Weight w WHERE w.weighingMethod = :weighingMethod AND w.account.id = :accountId")
    List<Weight> findByWeighingMethodAndAccount(@Param("weighingMethod") String weighingMethod, @Param("accountId") Long accountId);

    // Find weights above a certain threshold within account
    @Query("SELECT w FROM Weight w WHERE w.weight >= :minWeight AND w.account.id = :accountId ORDER BY w.weight DESC")
    List<Weight> findByMinWeightAndAccount(@Param("minWeight") BigDecimal minWeight, @Param("accountId") Long accountId);

    // Find weights below a certain threshold within account
    @Query("SELECT w FROM Weight w WHERE w.weight <= :maxWeight AND w.account.id = :accountId ORDER BY w.weight ASC")
    List<Weight> findByMaxWeightAndAccount(@Param("maxWeight") BigDecimal maxWeight, @Param("accountId") Long accountId);

    // Find weights within a weight range for account
    @Query("SELECT w FROM Weight w WHERE w.weight BETWEEN :minWeight AND :maxWeight AND w.account.id = :accountId ORDER BY w.weightDate DESC")
    List<Weight> findByWeightRangeAndAccount(@Param("minWeight") BigDecimal minWeight, @Param("maxWeight") BigDecimal maxWeight, @Param("accountId") Long accountId);

    // Find estimated vs measured weights within account
    @Query("SELECT w FROM Weight w WHERE w.isEstimated = :isEstimated AND w.account.id = :accountId ORDER BY w.weightDate DESC")
    List<Weight> findByEstimatedStatusAndAccount(@Param("isEstimated") Boolean isEstimated, @Param("accountId") Long accountId);

    // Find weights by body condition score range within account
    @Query("SELECT w FROM Weight w WHERE w.bodyConditionScore BETWEEN :minScore AND :maxScore AND w.account.id = :accountId ORDER BY w.weightDate DESC")
    List<Weight> findByBodyConditionScoreRangeAndAccount(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore, @Param("accountId") Long accountId);

    // Find most recent weights for trend analysis within account
    @Query("SELECT w FROM Weight w WHERE w.account.id = :accountId ORDER BY w.weightDate DESC")
    List<Weight> findRecentWeightsByAccount(@Param("accountId") Long accountId);

    // Find weights recorded in the last N days within account
    @Query("SELECT w FROM Weight w WHERE w.weightDate >= :sinceDate AND w.account.id = :accountId ORDER BY w.weightDate DESC")
    List<Weight> findRecentWeightsSinceDateAndAccount(@Param("sinceDate") Date sinceDate, @Param("accountId") Long accountId);
}