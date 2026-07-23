package com.bwg.channel.backend.productsvc.repository.jpa;

import com.bwg.channel.backend.productsvc.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 상품 Entity 접근을 위한 Spring Data JPA Repository
 */
public interface JpaProductRepository extends JpaRepository<Product, Long> {

    /**
     * 사용 여부 기준 상품 목록 조회
     */
    List<Product> findByUseYn(String useYn);

    /**
     * 사용 여부 기준 상품 전체 건수 조회
     */
    long countByUseYn(String useYn);
}
