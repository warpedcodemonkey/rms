package com.krazykritterranch.rms.repositories.common;

import com.krazykritterranch.rms.model.common.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {

    List<Phone> findByAreaCode(String areaCode);
}
