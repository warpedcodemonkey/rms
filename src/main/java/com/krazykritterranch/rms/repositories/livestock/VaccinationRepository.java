package com.krazykritterranch.rms.repositories.livestock;

import com.krazykritterranch.rms.model.livestock.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {

    // Find vaccinations by account (tenant filtering)
    @Query("SELECT v FROM Vaccination v WHERE v.account.id = :accountId")
    List<Vaccination> findByAccountId(@Param("accountId") Long accountId);

    // Find vaccinations accessible by veterinarian
    @Query("SELECT v FROM Vaccination v JOIN VetPermission vp ON v.account.id = vp.account.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true " +
            "AND (vp.expiresAt IS NULL OR vp.expiresAt > CURRENT_TIMESTAMP)")
    List<Vaccination> findByVeterinarianAccess(@Param("vetId") Long vetId);

    // Find vaccination by ID with account check (security)
    @Query("SELECT v FROM Vaccination v WHERE v.id = :id AND v.account.id = :accountId")
    Optional<Vaccination> findByIdAndAccount(@Param("id") Long id, @Param("accountId") Long accountId);

    // Count vaccinations by account
    @Query("SELECT COUNT(v) FROM Vaccination v WHERE v.account.id = :accountId")
    long countByAccount(@Param("accountId") Long accountId);

    // Find vaccinations by vaccine name within account
    @Query("SELECT v FROM Vaccination v WHERE v.vacName = :vacName AND v.account.id = :accountId")
    List<Vaccination> findByVacNameAndAccount(@Param("vacName") String vacName, @Param("accountId") Long accountId);

    // Find vaccinations by vaccine type within account
    @Query("SELECT v FROM Vaccination v WHERE v.vaccineType = :vaccineType AND v.account.id = :accountId")
    List<Vaccination> findByVaccineTypeAndAccount(@Param("vaccineType") String vaccineType, @Param("accountId") Long accountId);

    // Find vaccinations administered by specific person within account
    @Query("SELECT v FROM Vaccination v WHERE v.administeredBy = :administeredBy AND v.account.id = :accountId")
    List<Vaccination> findByAdministeredByAndAccount(@Param("administeredBy") String administeredBy, @Param("accountId") Long accountId);

    // Find vaccinations due for renewal (next due date is approaching) within account
    @Query("SELECT v FROM Vaccination v WHERE v.nextDueDate <= :dueDate AND v.account.id = :accountId ORDER BY v.nextDueDate")
    List<Vaccination> findUpcomingVaccinationsByAccount(@Param("dueDate") Date dueDate, @Param("accountId") Long accountId);

    // Find vaccinations administered within a date range for account
    @Query("SELECT v FROM Vaccination v WHERE v.administerDate BETWEEN :startDate AND :endDate AND v.account.id = :accountId ORDER BY v.administerDate DESC")
    List<Vaccination> findByDateRangeAndAccount(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("accountId") Long accountId);

    // Find vaccinations with expired vaccine lots within account
    @Query("SELECT v FROM Vaccination v WHERE v.expirationDate < :currentDate AND v.account.id = :accountId")
    List<Vaccination> findExpiredVaccinesByAccount(@Param("currentDate") Date currentDate, @Param("accountId") Long accountId);
}