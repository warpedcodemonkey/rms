package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    Email findByEmailAddress(String emailAddress);


}
