package com.bwg.channel.backend.productsvc.repository.jpa;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.productsvc.constants.ProductErrorCode;
import com.bwg.channel.backend.productsvc.exception.BwgProductException;
import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductResDto;
import com.bwg.channel.backend.productsvc.domain.entity.Product;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductRepository 계약을 JPA Repository 호출로 연결하는 어댑터
 */
@Repository("jpaProduct")
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;

    /**
     * JPA 기반 사용 여부별 상품 목록 조회
     */
    @Override
    public List<ProductResDto> findAll(ApiRequest<ProductReqDto> paramDto) {
        return jpaProductRepository.findByUseYn(resolveUseYn(paramDto)).stream()
                .map(this::toResDto)
                .collect(Collectors.toList());
    }

    /**
     * JPA 목록 조회와 동일한 사용 여부 조건의 전체 건수를 조회한다.
     */
    @Override
    public long count(ApiRequest<ProductReqDto> paramDto) {
        return jpaProductRepository.countByUseYn(resolveUseYn(paramDto));
    }

    /**
     * JPA 기반 상품 단건 조회
     */
    @Override
    public ProductResDto findById(Long productId) {
        Product product = jpaProductRepository.findById(productId)
                .orElseThrow(() -> new BwgProductException.Builder()
                        .code(ProductErrorCode.PRODUCT_NOT_FOUND)
                        .message(ProductErrorCode.PRODUCT_NOT_FOUND.getMsg())
                        .build());
        return toResDto(product);
    }

    /**
     * Product Entity를 응답 DTO로 변환
     */
    private ProductResDto toResDto(Product product) {
        ProductResDto dto = new ProductResDto();
        dto.setProductId(product.getProductId());
        dto.setProductNm(product.getProductNm());
        dto.setProductDesc(product.getProductDesc());
        dto.setPrice(product.getPrice());
        dto.setStockQty(product.getStockQty());
        dto.setUseYn(product.getUseYn());
        return dto;
    }

    /**
     * JPA 상품 조회에 사용할 사용 여부 조건을 요청에서 추출하고 기본값을 적용한다.
     *
     * @param paramDto 상품 조회 요청
     * @return 요청된 사용 여부 또는 기본값 {@code Y}
     */
    private String resolveUseYn(ApiRequest<ProductReqDto> paramDto) {
        // JPA 목록과 COUNT가 항상 같은 기본 필터를 사용하도록 한 곳에서 값을 결정한다.
        return paramDto != null
                && paramDto.getFilter() != null
                && paramDto.getFilter().getUseYn() != null
                ? paramDto.getFilter().getUseYn()
                : "Y";
    }
}
