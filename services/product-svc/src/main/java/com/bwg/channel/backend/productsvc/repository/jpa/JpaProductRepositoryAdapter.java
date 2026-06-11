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

@Component("jpaProduct")
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;

    @Override
    public List<ProductDto> findAll(ProductDto paramDto) {
        String useYn = paramDto.getUseYn() != null ? paramDto.getUseYn() : "Y";
        return jpaProductRepository.findByUseYn(useYn).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto findById(Long productId) {
        Product product = jpaProductRepository.findById(productId)
                .orElseThrow(() -> new BwgProductException.Builder()
                        .code(ProductErrorCode.PRODUCT_NOT_FOUND)
                        .message(ProductErrorCode.PRODUCT_NOT_FOUND.getMsg())
                        .build());
        return toDto(product);
    }

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
