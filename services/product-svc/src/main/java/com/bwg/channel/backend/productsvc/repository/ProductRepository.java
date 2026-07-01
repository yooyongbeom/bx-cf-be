package com.bwg.channel.backend.productsvc.repository;

import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductResDto;

import java.util.List;

/**
 * 상품 데이터 접근을 추상화한 저장소 계약
 */
public interface ProductRepository {

    /**
     * 상품 목록 조회
     */
    List<ProductResDto> findAll(ProductReqDto paramDto);

    /**
     * 상품 단건 조회
     */
    ProductResDto findById(Long productId);
}
