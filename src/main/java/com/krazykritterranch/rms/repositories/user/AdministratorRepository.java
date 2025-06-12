package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {

    List<Administrator> findByIsActiveTrue();

    List<Administrator> findByDepartment(String department);

    @Query("SELECT a FROM Administrator a WHERE a.accessLevel >= :level")
    List<Administrator> findByAccessLevelGreaterThanEqual(@Param("level") Integer level);

    @Query("SELECT COUNT(a) FROM Administrator a WHERE a.isActive = true")
    long countActiveAdministrators();
}
