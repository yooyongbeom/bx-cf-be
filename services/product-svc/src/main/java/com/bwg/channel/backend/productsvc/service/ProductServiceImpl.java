package com.bwg.channel.backend.productsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.productsvc.cmm.constants.ProductErrorCode;
import com.bwg.channel.backend.productsvc.cmm.exception.BwgProductException;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final Map<String, ProductRepository> productRepositoryMap;
    private final ProductRepository defaultProductRepository;

    @Override
    public ApiResponse<List<ProductDto>> getProductList(ProductDto paramDto, String type) {
        ProductRepository repository = getRepository(type);
        List<ProductDto> result = repository.findAll(paramDto);
        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse<ProductDto> getProduct(Long productId, String type) {
        ProductRepository repository = getRepository(type);
        ProductDto result = repository.findById(productId);
        return ApiResponse.success(result);
    }

    private ProductRepository getRepository(String type) {
        ProductRepository repository = (type == null || type.isEmpty())
                ? defaultProductRepository
                : productRepositoryMap.get(type);

        if (repository == null) {
            throw new BwgProductException.Builder()
                    .code(ProductErrorCode.REQUIRED_VALUE_MISSING)
                    .message("Invalid Product repository type : " + type)
                    .build();
        }

        return repository;
    }
}
