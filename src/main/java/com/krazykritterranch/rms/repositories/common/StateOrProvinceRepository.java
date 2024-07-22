package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.StateOrProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateOrProvinceRepository extends JpaRepository<StateOrProvince, Long> {
    // Custom query methods can be added here if needed

    // Example of a custom query method to find a state or province by its name
    Optional<StateOrProvince> findByName(String name);

    // Example of a custom query method to find all states or provinces with a specific abbreviation
    List<StateOrProvince> findByAbbr(String abbr);

    // Example of a custom query method to find states or provinces with names containing a specific keyword
    List<StateOrProvince> findByNameContainingIgnoreCase(String keyword);


}
