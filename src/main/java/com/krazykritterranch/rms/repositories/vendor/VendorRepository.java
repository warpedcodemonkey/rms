package com.krazykritterranch.rms.repositories.vendor;

import com.krazykritterranch.rms.model.vendor.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
}
