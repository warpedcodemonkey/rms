package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Address;
import com.krazykritterranch.rms.model.common.StateOrProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Custom query methods can be added here if needed

    @Query("select a from Address a where a.stateOrProvince = :stateOrProvince")
    List<Address> listByState(StateOrProvince stateOrProvince);

    List<Address> findByPostalCode(String postalCode);




}
