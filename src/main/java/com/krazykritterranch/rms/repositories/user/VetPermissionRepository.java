package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.VetPermission;
import com.krazykritterranch.rms.model.user.VetPermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VetPermissionRepository extends JpaRepository<VetPermission, Long> {

    @Query("SELECT vp FROM VetPermission vp WHERE vp.veterinarian.id = :vetId AND vp.account.id = :accountId AND vp.isActive = true AND (vp.expiresAt IS NULL OR vp.expiresAt > :now)")
    Optional<VetPermission> findActivePermissionByVetAndAccount(@Param("vetId") Long vetId, @Param("accountId") Long accountId, @Param("now") LocalDateTime now);

    default Optional<VetPermission> findActivePermissionByVetAndAccount(Long vetId, Long accountId) {
        return findActivePermissionByVetAndAccount(vetId, accountId, LocalDateTime.now());
    }

    @Query("SELECT vp FROM VetPermission vp WHERE vp.veterinarian.id = :vetId AND vp.isActive = true AND (vp.expiresAt IS NULL OR vp.expiresAt > :now)")
    List<VetPermission> findActivePermissionsByVet(@Param("vetId") Long vetId, @Param("now") LocalDateTime now);

    @Query("SELECT vp FROM VetPermission vp WHERE vp.account.id = :accountId AND vp.isActive = true")
    List<VetPermission> findPermissionsByAccount(@Param("accountId") Long accountId);

    @Query("SELECT vp FROM VetPermission vp WHERE vp.expiresAt < :now AND vp.isActive = true")
    List<VetPermission> findExpiredPermissions(@Param("now") LocalDateTime now);
}