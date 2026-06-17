package com.bwg.channel.backend.productsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import lombok.Data;

@Data
@ApiDto(name = "Product", endpoints = {})
public class ProductDto {

    @ApiField(description = "상품 ID", example = "1001", optional = {"list"})
    private Long productId;

    @ApiField(description = "상품명", example = "안정형 펀드", optional = {"list"})
    private String productNm;

    @ApiField(description = "상품 설명", optional = {"list"})
    private String productDesc;

    @ApiField(description = "가격", example = "100000", optional = {"list"})
    private Long price;

    @ApiField(description = "재고 수량", example = "50", optional = {"list"})
    private Integer stockQty;

    @ApiField(description = "사용 여부 (Y/N)", example = "Y",
              allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;
}
