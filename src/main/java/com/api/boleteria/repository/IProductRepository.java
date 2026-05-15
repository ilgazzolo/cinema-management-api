package com.api.boleteria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.api.boleteria.model.Product;
import java.util.List;


@Repository
public interface IProductRepository extends JpaRepository <Product, Long>{
    Optional<Product> findByName (String name);
    boolean existsByName (String name);
    boolean existsByNameAndIdNot (String name, Long id);
    List<Product> findByAvailable(Boolean available);
}
