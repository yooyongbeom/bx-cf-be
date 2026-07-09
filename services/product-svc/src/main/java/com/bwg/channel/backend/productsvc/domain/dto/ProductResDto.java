package com.bwg.channel.backend.productsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.OffsetDateTime;

/**
 * 상품 API 응답 모델. 엔드포인트별 응답 스키마를 생성한다.
 * <p>
 * 같은 DTO로 목록(list)은 요약 필드만, 단건(detail)은 전체 필드를 노출하며 필수 여부도 다르게 지정한다.
 * <ul>
 *   <li>list  → productId, productNm, price(선택)</li>
 *   <li>detail→ productId, productNm, productDesc, price, stockQty, useYn (전부 노출)</li>
 * </ul>
 */
@Alias("ProductResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "Product", endpoints = {"list", "detail"})
public class ProductResDto {

    @ApiField(description = "상품 ID", example = "1001", required = {"list", "detail"})
    private Long productId;

    @ApiField(description = "상품명", example = "안정형 펀드", required = {"list", "detail"})
    private String productNm;

    @ApiField(description = "상품 설명", required = {"detail"})
    private String productDesc;

    @ApiField(description = "가격", example = "100000", required = {"detail"}, optional = {"list"})
    private Long price;

    @ApiField(description = "재고 수량", example = "50", required = {"detail"})
    private Integer stockQty;

    @ApiField(description = "사용 여부 (Y/N)", example = "Y",
              allowableValues = {"Y", "N"}, optional = {"detail"})
    private String useYn;

    @ApiField(description = "생성자 ID", example = "system", optional = {"detail"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "system", optional = {"detail"})
    private String updatedBy;

    @ApiField(description = "생성 일시", example = "2026-06-25T16:01:14+09:00", optional = {"detail"})
    private OffsetDateTime createdAt;

    @ApiField(description = "수정 일시", example = "2026-06-25T16:01:14+09:00", optional = {"detail"})
    private OffsetDateTime updatedAt;
}
