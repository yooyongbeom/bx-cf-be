package com.bwg.channel.backend.productsvc.repository;

import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;

import java.util.List;

/**
 * 상품 데이터 접근을 추상화한 저장소 계약
 */
public interface ProductRepository {

    /**
     * 상품 목록 조회
     */
    List<ProductDto> findAll(ProductDto paramDto);

    /**
     * 상품 단건 조회
     */
    ProductDto findById(Long productId);
}
