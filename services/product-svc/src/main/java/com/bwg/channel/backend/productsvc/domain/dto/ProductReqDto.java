package com.bwg.channel.backend.productsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;

/**
 * 상품 API 요청(검색 조건) 모델. 엔드포인트별 요청 스키마를 생성한다.
 */
@Data
@ApiDto(type = ApiType.REQUEST, name = "Product", endpoints = {"list"})
public class ProductReqDto {

    @ApiField(description = "상품명", example = "안정형 펀드", optional = {"list"})
    private String productNm;

    @ApiField(description = "사용 여부 (Y/N)", example = "Y",
              allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;
}
