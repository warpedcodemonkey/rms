package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
