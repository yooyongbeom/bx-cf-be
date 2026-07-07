package com.bwg.channel.backend.productsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductResDto;

import java.util.List;

/**
 * 상품 조회 기능의 서비스 계약
 */
public interface ProductService {

    /**
     * 상품 목록 조회
     */
    ApiResponse<List<ProductResDto>> getProductList(ApiRequest<ProductReqDto> paramDto, String type);

    /**
     * 상품 단건 조회
     */
    ApiResponse<ProductResDto> getProduct(Long productId, String type);
}
