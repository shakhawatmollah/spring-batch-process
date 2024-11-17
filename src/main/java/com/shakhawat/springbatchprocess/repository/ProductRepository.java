package com.shakhawat.springbatchprocess.repository;

import com.shakhawat.springbatchprocess.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
