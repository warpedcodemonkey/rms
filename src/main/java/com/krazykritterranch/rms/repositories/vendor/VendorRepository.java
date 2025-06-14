package com.krazykritterranch.rms.repositories.vendor;

import com.krazykritterranch.rms.model.vendor.Vendor;
import com.krazykritterranch.rms.model.vendor.VendorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    // Find all vendors for a specific account (tenant filtering)
    @Query("SELECT v FROM Vendor v WHERE v.account.id = :accountId ORDER BY v.vendorName")
    List<Vendor> findByAccountId(@Param("accountId") Long accountId);

    // Find active vendors for a specific account
    @Query("SELECT v FROM Vendor v WHERE v.account.id = :accountId AND v.isActive = true ORDER BY v.vendorName")
    List<Vendor> findActiveByAccountId(@Param("accountId") Long accountId);

    // Find vendor by ID with account check (security)
    @Query("SELECT v FROM Vendor v WHERE v.id = :id AND v.account.id = :accountId")
    Optional<Vendor> findByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    // Find vendors by type within an account
    @Query("SELECT v FROM Vendor v WHERE v.vendorType.id = :typeId AND v.account.id = :accountId ORDER BY v.vendorName")
    List<Vendor> findByVendorTypeIdAndAccountId(@Param("typeId") Long typeId, @Param("accountId") Long accountId);

    // Find vendors by type name within an account
    @Query("SELECT v FROM Vendor v WHERE v.vendorType.typeName = :typeName AND v.account.id = :accountId ORDER BY v.vendorName")
    List<Vendor> findByVendorTypeNameAndAccountId(@Param("typeName") String typeName, @Param("accountId") Long accountId);

    // Search vendors by name (case-insensitive) within an account
    @Query("SELECT v FROM Vendor v WHERE LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :name, '%')) AND v.account.id = :accountId ORDER BY v.vendorName")
    List<Vendor> findByNameContainingAndAccountId(@Param("name") String name, @Param("accountId") Long accountId);

    // Find vendors with products within an account
    @Query("SELECT DISTINCT v FROM Vendor v JOIN v.products p WHERE v.account.id = :accountId ORDER BY v.vendorName")
    List<Vendor> findVendorsWithProductsByAccountId(@Param("accountId") Long accountId);

    // Count vendors by account
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);

    // Count active vendors by account
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.account.id = :accountId AND v.isActive = true")
    long countActiveByAccountId(@Param("accountId") Long accountId);

    // Check if a vendor exists for a specific account
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vendor v WHERE v.id = :id AND v.account.id = :accountId")
    boolean existsByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    // Find vendors by website domain within an account (useful for duplicate checking)
    @Query("SELECT v FROM Vendor v WHERE v.website LIKE CONCAT('%', :domain, '%') AND v.account.id = :accountId")
    List<Vendor> findByWebsiteDomainAndAccountId(@Param("domain") String domain, @Param("accountId") Long accountId);

    // Delete all vendors for an account (useful for account deletion)
    @Query("DELETE FROM Vendor v WHERE v.account.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);

    // Find vendors with notes within an account
    @Query("SELECT DISTINCT v FROM Vendor v JOIN v.notes n WHERE v.account.id = :accountId ORDER BY v.vendorName")
    List<Vendor> findVendorsWithNotesByAccountId(@Param("accountId") Long accountId);

    // Veterinarian access - vendors accessible through livestock/feed relationships
    @Query("SELECT DISTINCT v FROM Vendor v " +
            "LEFT JOIN Feed f ON f.vendor.id = v.id " +
            "LEFT JOIN VetPermission vp ON f.account.id = vp.account.id " +
            "WHERE vp.veterinarian.id = :vetId AND vp.isActive = true " +
            "AND (vp.expiresAt IS NULL OR vp.expiresAt > CURRENT_TIMESTAMP)")
    List<Vendor> findByVeterinarianAccess(@Param("vetId") Long vetId);
}