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
     * 저장소 유형에 따라 상품 목록을 조회한다.
     *
     * @param paramDto 상품 조회 조건과 선택적 페이지 요청
     * @param type 사용할 상품 저장소 유형
     * @return 상품 목록과 요청된 경우 페이지 정보가 포함된 응답
     */
    ApiResponse<List<ProductResDto>> getProductList(ApiRequest<ProductReqDto> paramDto, String type);

    /**
     * 저장소 유형과 상품 ID로 상품 한 건을 조회한다.
     *
     * @param productId 조회할 상품 ID
     * @param type 사용할 상품 저장소 유형
     * @return 조회된 상품이 포함된 응답
     */
    ApiResponse<ProductResDto> getProduct(Long productId, String type);
}
