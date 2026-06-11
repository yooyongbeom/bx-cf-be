package com.bwg.channel.backend.productsvc.repository;

import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;

import java.util.List;

public interface ProductRepository {

    List<ProductDto> findAll(ProductDto paramDto);

    ProductDto findById(Long productId);
}
