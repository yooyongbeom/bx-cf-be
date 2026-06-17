package com.bwg.channel.backend.productsvc.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "상품 정보")
public class ProductDto {

    @Schema(description = "상품 ID", example = "1001")
    private Long   productId;

    @Schema(description = "상품명", example = "안정형 펀드")
    private String productNm;

    @Schema(description = "상품 설명")
    private String productDesc;

    @Schema(description = "가격", example = "100000")
    private Long   price;

    @Schema(description = "재고 수량", example = "50")
    private Integer stockQty;

    @Schema(description = "사용 여부 (Y/N)", example = "Y")
    private String useYn;
}
