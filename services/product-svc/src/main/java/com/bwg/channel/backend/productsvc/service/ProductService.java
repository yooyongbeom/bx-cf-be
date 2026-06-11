package com.bwg.channel.backend.productsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;

import java.util.List;

public interface ProductService {

    ApiResponse<List<ProductDto>> getProductList(ProductDto paramDto, String type);

    ApiResponse<ProductDto> getProduct(Long productId, String type);
}
