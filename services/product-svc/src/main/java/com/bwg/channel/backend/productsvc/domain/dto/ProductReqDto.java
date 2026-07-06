package com.bwg.channel.backend.productsvc.domain.dto;

import com.bwg.channel.backend.common.domain.dto.BaseSearchReqDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 상품 API 요청(검색 조건) 모델. 공통 검색 조건과 상품별 검색 조건을 함께 사용한다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiDto(type = ApiType.REQUEST, name = "Product", endpoints = {"list"})
public class ProductReqDto extends BaseSearchReqDto {

    @ApiField(description = "상품명", example = "예정된 상품", optional = {"list"})
    private String productNm;
}
