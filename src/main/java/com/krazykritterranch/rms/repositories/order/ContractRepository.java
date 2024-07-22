package com.krazykritterranch.rms.repositories.order;

import com.krazykritterranch.rms.model.order.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByCustomerId(Long id);


}
