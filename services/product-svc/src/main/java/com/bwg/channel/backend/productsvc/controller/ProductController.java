package com.bwg.channel.backend.productsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import com.bwg.channel.backend.productsvc.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품 목록/단건 조회 API 컨트롤러
 */
@Tag(name = "상품")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회
     */
    @Operation(summary = "상품 목록 조회", description = "조건에 맞는 상품 목록을 조회한다.")
    @PostMapping("/list")
    public ApiResponse<List<ProductDto>> getProductList(@RequestBody ProductDto paramDto) {
        return productService.getProductList(paramDto, "mybatisProduct");
    }

    /**
     * 상품 단건 조회
     */
    @Operation(summary = "상품 단건 조회", description = "상품 ID로 단건 상품을 조회한다.")
    @PostMapping("/{productId}")
    public ApiResponse<ProductDto> getProduct(
            @Parameter(description = "상품 ID", example = "1001")
            @PathVariable Long productId
    ) {
        return productService.getProduct(productId, "mybatisProduct");
    }
}
