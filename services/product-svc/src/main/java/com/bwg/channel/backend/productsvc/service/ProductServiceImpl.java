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

/**
 * 상품 저장소 전략을 선택해 상품 조회를 처리하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final Map<String, ProductRepository> productRepositoryMap;
    private final ProductRepository defaultProductRepository;

    /**
     * 상품 목록 조회
     */
    @Override
    public ApiResponse<List<ProductDto>> getProductList(ProductDto paramDto, String type) {
        // 저장소 전략 선택
        ProductRepository repository = getRepository(type);
        // 선택된 저장소 기준 상품 목록 조회
        List<ProductDto> result = repository.findAll(paramDto);
        return ApiResponse.success(result);
    }

    /**
     * 상품 단건 조회
     */
    @Override
    public ApiResponse<ProductDto> getProduct(Long productId, String type) {
        // 저장소 전략 선택
        ProductRepository repository = getRepository(type);
        // 선택된 저장소 기준 상품 단건 조회
        ProductDto result = repository.findById(productId);
        return ApiResponse.success(result);
    }

    /**
     * 상품 저장소 전략 선택
     */
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
