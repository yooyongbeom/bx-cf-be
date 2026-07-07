package com.bwg.channel.backend.productsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductResDto;
import com.bwg.channel.backend.productsvc.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품 목록/단건 조회 API 컨트롤러.
 * <p>API 경계에서 공통 요청 블록과 상품 요청 데이터 {@link ProductReqDto}를 함께 사용한다.
 */
@Tag(name = "상품")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private static final String REPOSITORY_TYPE = "mybatisProduct";

    private final ProductService productService;

    /**
     * 상품 목록 조회 (list: 요약 필드 응답)
     */
    @Operation(summary = "상품 목록 조회", description = "조건에 맞는 상품 목록을 조회한다.")
    @PostMapping("/list")
    public ApiResponse<List<ProductResDto>> getProductList(@RequestBody ApiRequest<ProductReqDto> req) {
        return productService.getProductList(req, REPOSITORY_TYPE);
    }

    /**
     * 상품 단건 조회 (detail: 전체 필드 응답)
     */
    @Operation(summary = "상품 단건 조회", description = "상품 ID로 단건 상품을 조회한다.")
    @PostMapping("/detail/{productId}")
    public ApiResponse<ProductResDto> getProduct(
            @Parameter(description = "상품 ID", example = "1001")
            @PathVariable Long productId
    ) {
        return productService.getProduct(productId, REPOSITORY_TYPE);
    }
}
