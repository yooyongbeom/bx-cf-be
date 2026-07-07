package com.bwg.channel.backend.productsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 상품 요청 데이터 모델.
 */
@Alias("ProductReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "Product", endpoints = {"list"})
public class ProductReqDto {

    @ApiField(description = "상품명", example = "KB 적립식 펀드", optional = {"list"})
    private String productNm;
}
