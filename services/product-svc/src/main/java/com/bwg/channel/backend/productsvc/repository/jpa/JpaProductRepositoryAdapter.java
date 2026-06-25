package com.bwg.channel.backend.productsvc.repository.jpa;

import com.bwg.channel.backend.productsvc.cmm.constants.ProductErrorCode;
import com.bwg.channel.backend.productsvc.cmm.exception.BwgProductException;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import com.bwg.channel.backend.productsvc.domain.entity.Product;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductRepository 계약을 JPA Repository 호출로 연결하는 어댑터
 */
@Component("jpaProduct")
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;

    /**
     * JPA 기반 사용 여부별 상품 목록 조회
     */
    @Override
    public List<ProductDto> findAll(ProductDto paramDto) {
        // 사용 여부 기본값 보정
        String useYn = paramDto.getUseYn() != null ? paramDto.getUseYn() : "Y";
        return jpaProductRepository.findByUseYn(useYn).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * JPA 기반 상품 단건 조회
     */
    @Override
    public ProductDto findById(Long productId) {
        Product product = jpaProductRepository.findById(productId)
                .orElseThrow(() -> new BwgProductException.Builder()
                        .code(ProductErrorCode.PRODUCT_NOT_FOUND)
                        .message(ProductErrorCode.PRODUCT_NOT_FOUND.getMsg())
                        .build());
        return toDto(product);
    }

    /**
     * Product Entity를 응답 DTO로 변환
     */
    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductNm(product.getProductNm());
        dto.setProductDesc(product.getProductDesc());
        dto.setPrice(product.getPrice());
        dto.setStockQty(product.getStockQty());
        dto.setUseYn(product.getUseYn());
        return dto;
    }
}
