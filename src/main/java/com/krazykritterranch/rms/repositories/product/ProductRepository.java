package com.krazykritterranch.rms.repositories.product;

import com.krazykritterranch.rms.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductNameContains(String search);

    List<Product> findByProductDescriptionContains(String search);




}
