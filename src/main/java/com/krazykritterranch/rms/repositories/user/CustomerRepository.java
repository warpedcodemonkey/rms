package com.krazykritterranch.rms.repositories.user;

import com.krazykritterranch.rms.model.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerNumber(String customerNumber);

    boolean existsByCustomerNumber(String customerNumber);

    List<Customer> findByIsActiveTrue();

    @Query("SELECT c FROM Customer c JOIN c.authorizedVets v WHERE v.id = :vetId")
    List<Customer> findCustomersAuthorizedForVet(@Param("vetId") Long vetId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isActive = true")
    long countActiveCustomers();

    @Query("SELECT c FROM Customer c WHERE c.firstName LIKE %:name% OR c.lastName LIKE %:name% OR c.customerNumber LIKE %:name%")
    List<Customer> findByNameOrNumberContaining(@Param("name") String name);
}