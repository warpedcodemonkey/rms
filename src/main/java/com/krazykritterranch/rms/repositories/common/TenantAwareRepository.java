package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.service.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {

    // This will be implemented by concrete repositories that need tenant filtering
    List<T> findAllForCurrentTenant();

    List<T> findAllForAccount(Long accountId);
}