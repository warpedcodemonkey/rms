package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    // Find by individual components
    List<Email> findByEmailAccount(String emailAccount);

    List<Email> findByDomain(String domain);

    List<Email> findByTld(String tld);

    // Find by email account and domain combination
    List<Email> findByEmailAccountAndDomain(String emailAccount, String domain);

    // Custom query to find by constructed email address
    @Query("SELECT e FROM Email e WHERE CONCAT(e.emailAccount, '@', e.domain, '.', e.tld) = :emailAddress")
    Optional<Email> findByConstructedEmailAddress(@Param("emailAddress") String emailAddress);

    // Custom query to find emails containing a partial match
    @Query("SELECT e FROM Email e WHERE CONCAT(e.emailAccount, '@', e.domain, '.', e.tld) LIKE %:partialEmail%")
    List<Email> findByPartialEmailAddress(@Param("partialEmail") String partialEmail);

    // Check if email address exists
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Email e WHERE CONCAT(e.emailAccount, '@', e.domain, '.', e.tld) = :emailAddress")
    boolean existsByConstructedEmailAddress(@Param("emailAddress") String emailAddress);
}