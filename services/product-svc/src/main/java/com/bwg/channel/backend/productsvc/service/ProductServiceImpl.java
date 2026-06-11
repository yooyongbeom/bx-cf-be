package com.bwg.channel.backend.productsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ApplicationContext applicationContext;

    @Override
    public ApiResponse<List<ProductDto>> getProductList(ProductDto paramDto, String type) {
        ProductRepository repository = applicationContext.getBean(type, ProductRepository.class);
        List<ProductDto> result = repository.findAll(paramDto);
        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse<ProductDto> getProduct(Long productId, String type) {
        ProductRepository repository = applicationContext.getBean(type, ProductRepository.class);
        ProductDto result = repository.findById(productId);
        return ApiResponse.success(result);
    }
}
