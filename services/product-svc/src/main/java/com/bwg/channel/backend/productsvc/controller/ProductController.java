package com.bwg.channel.backend.productsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import com.bwg.channel.backend.productsvc.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/channel/backend/api/v1/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    public ApiResponse<List<ProductDto>> getProductList(ProductDto paramDto) {
        return productService.getProductList(paramDto, "mybatisProduct");
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductDto> getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId, "mybatisProduct");
    }
}
