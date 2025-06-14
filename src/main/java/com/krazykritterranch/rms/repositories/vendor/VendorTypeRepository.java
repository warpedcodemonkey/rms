package com.krazykritterranch.rms.repositories.vendor;

import com.krazykritterranch.rms.model.vendor.VendorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorTypeRepository extends JpaRepository<VendorType, Long> {

    // Find all vendor types for a specific account
    @Query("SELECT vt FROM VendorType vt WHERE vt.account.id = :accountId ORDER BY vt.displayOrder, vt.typeName")
    List<VendorType> findByAccountId(@Param("accountId") Long accountId);

    // Find active vendor types for a specific account
    @Query("SELECT vt FROM VendorType vt WHERE vt.account.id = :accountId AND vt.isActive = true ORDER BY vt.displayOrder, vt.typeName")
    List<VendorType> findActiveByAccountId(@Param("accountId") Long accountId);

    // Find vendor type by ID with account check (security)
    @Query("SELECT vt FROM VendorType vt WHERE vt.id = :id AND vt.account.id = :accountId")
    Optional<VendorType> findByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    // Find vendor type by name within an account (for duplicate checking)
    @Query("SELECT vt FROM VendorType vt WHERE LOWER(vt.typeName) = LOWER(:typeName) AND vt.account.id = :accountId")
    Optional<VendorType> findByTypeNameAndAccountId(@Param("typeName") String typeName, @Param("accountId") Long accountId);

    // Find system default types (for copying to new accounts)
    @Query("SELECT vt FROM VendorType vt WHERE vt.isSystemDefault = true")
    List<VendorType> findSystemDefaults();

    // Count vendor types by account
    @Query("SELECT COUNT(vt) FROM VendorType vt WHERE vt.account.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);

    // Check if a vendor type exists for a specific account
    @Query("SELECT CASE WHEN COUNT(vt) > 0 THEN true ELSE false END FROM VendorType vt WHERE vt.id = :id AND vt.account.id = :accountId")
    boolean existsByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    // Check if a vendor type name exists for a specific account (for validation)
    @Query("SELECT CASE WHEN COUNT(vt) > 0 THEN true ELSE false END FROM VendorType vt WHERE LOWER(vt.typeName) = LOWER(:typeName) AND vt.account.id = :accountId")
    boolean existsByTypeNameAndAccountId(@Param("typeName") String typeName, @Param("accountId") Long accountId);

    // Get max display order for an account (for adding new types)
    @Query("SELECT COALESCE(MAX(vt.displayOrder), 0) FROM VendorType vt WHERE vt.account.id = :accountId")
    Integer getMaxDisplayOrderByAccountId(@Param("accountId") Long accountId);

    // Delete all vendor types for an account
    @Query("DELETE FROM VendorType vt WHERE vt.account.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);
}