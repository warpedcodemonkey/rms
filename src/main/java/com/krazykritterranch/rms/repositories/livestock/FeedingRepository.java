package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Feeding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedingRepository extends JpaRepository<Feeding, Long> {

    // Find feedings by account (tenant filtering)
    @Query("SELECT f FROM Feeding f WHERE f.account.id = :accountId")
    List<Feeding> findByAccountId(@Param("accountId") Long accountId);

    // Find feedings accessible by veterinarian
    @Query("SELECT f FROM Feeding f JOIN VetPermission vp ON f.account.id = vp.account.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true " +
            "AND (vp.expiresAt IS NULL OR vp.expiresAt > CURRENT_TIMESTAMP)")
    List<Feeding> findByVeterinarianAccess(@Param("vetId") Long vetId);

    // Find feeding by ID with account check (security)
    @Query("SELECT f FROM Feeding f WHERE f.id = :id AND f.account.id = :accountId")
    Optional<Feeding> findByIdAndAccount(@Param("id") Long id, @Param("accountId") Long accountId);

    // Count feedings by account
    @Query("SELECT COUNT(f) FROM Feeding f WHERE f.account.id = :accountId")
    long countByAccount(@Param("accountId") Long accountId);

    // Find feedings within a date range for account
    @Query("SELECT f FROM Feeding f WHERE f.feedingDate BETWEEN :startDate AND :endDate AND f.account.id = :accountId ORDER BY f.feedingDate DESC, f.feedingTime DESC")
    List<Feeding> findByDateRangeAndAccount(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("accountId") Long accountId);

    // Find feedings by feed type within account
    @Query("SELECT f FROM Feeding f JOIN f.feed feed WHERE feed.feedType = :feedType AND f.account.id = :accountId ORDER BY f.feedingDate DESC")
    List<Feeding> findByFeedTypeAndAccount(@Param("feedType") String feedType, @Param("accountId") Long accountId);

    // Find feedings recorded by specific person within account
    @Query("SELECT f FROM Feeding f WHERE f.recordedBy = :recordedBy AND f.account.id = :accountId ORDER BY f.feedingDate DESC")
    List<Feeding> findByRecordedByAndAccount(@Param("recordedBy") String recordedBy, @Param("accountId") Long accountId);

    // Find feedings by feeding method within account
    @Query("SELECT f FROM Feeding f WHERE f.feedingMethod = :feedingMethod AND f.account.id = :accountId")
    List<Feeding> findByFeedingMethodAndAccount(@Param("feedingMethod") String feedingMethod, @Param("accountId") Long accountId);

    // Find feedings with high waste (consumption analysis) within account
    @Query("SELECT f FROM Feeding f WHERE f.wasteAmount > :minWaste AND f.account.id = :accountId ORDER BY f.wasteAmount DESC")
    List<Feeding> findHighWasteByAccount(@Param("minWaste") BigDecimal minWaste, @Param("accountId") Long accountId);

    // Find feedings with low consumption percentage within account
    @Query("SELECT f FROM Feeding f WHERE f.consumptionPercentage < :maxConsumption AND f.account.id = :accountId ORDER BY f.consumptionPercentage ASC")
    List<Feeding> findLowConsumptionByAccount(@Param("maxConsumption") BigDecimal maxConsumption, @Param("accountId") Long accountId);

    // Find recent feedings for trend analysis within account
    @Query("SELECT f FROM Feeding f WHERE f.feedingDate >= :sinceDate AND f.account.id = :accountId ORDER BY f.feedingDate DESC, f.feedingTime DESC")
    List<Feeding> findRecentFeedingsByAccount(@Param("sinceDate") Date sinceDate, @Param("accountId") Long accountId);

    // Calculate total feed consumption by account for a date range
    @Query("SELECT SUM(f.feedAmount) FROM Feeding f WHERE f.feedingDate BETWEEN :startDate AND :endDate AND f.account.id = :accountId")
    BigDecimal calculateTotalFeedConsumptionByAccount(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("accountId") Long accountId);

    // Calculate average daily feed consumption by account
    @Query("SELECT AVG(f.feedAmount) FROM Feeding f WHERE f.feedingDate BETWEEN :startDate AND :endDate AND f.account.id = :accountId")
    BigDecimal calculateAverageDailyFeedConsumptionByAccount(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("accountId") Long accountId);

    // Find feedings by specific feed within account
    @Query("SELECT f FROM Feeding f WHERE f.feed.id = :feedId AND f.account.id = :accountId ORDER BY f.feedingDate DESC")
    List<Feeding> findByFeedIdAndAccount(@Param("feedId") Long feedId, @Param("accountId") Long accountId);

    // Find feedings by feed quality score range within account
    @Query("SELECT f FROM Feeding f WHERE f.feedQualityScore BETWEEN :minScore AND :maxScore AND f.account.id = :accountId ORDER BY f.feedingDate DESC")
    List<Feeding> findByFeedQualityScoreRangeAndAccount(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore, @Param("accountId") Long accountId);

    // Find feedings where actual differs significantly from scheduled within account
    @Query("SELECT f FROM Feeding f WHERE ABS(f.feedAmount - f.scheduledAmount) > :threshold AND f.account.id = :accountId ORDER BY ABS(f.feedAmount - f.scheduledAmount) DESC")
    List<Feeding> findSignificantVarianceFromScheduledByAccount(@Param("threshold") BigDecimal threshold, @Param("accountId") Long accountId);

    // Find feedings by time range (for daily schedule analysis) within account
    @Query("SELECT f FROM Feeding f WHERE f.feedingTime BETWEEN :startTime AND :endTime AND f.account.id = :accountId ORDER BY f.feedingDate DESC, f.feedingTime")
    List<Feeding> findByTimeRangeAndAccount(@Param("startTime") Time startTime, @Param("endTime") Time endTime, @Param("accountId") Long accountId);
}