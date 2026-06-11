package com.bwg.channel.backend.productsvc.repository.jpa;

import com.bwg.channel.backend.productsvc.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUseYn(String useYn);
}
