package com.bwg.channel.backend.productsvc.repository.mybatis;

import com.bwg.channel.backend.productsvc.cmm.constants.ProductErrorCode;
import com.bwg.channel.backend.productsvc.cmm.exception.BwgProductException;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import com.bwg.channel.backend.productsvc.repository.mybatis.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ProductRepository 계약을 MyBatis Mapper 호출로 연결하는 어댑터
 */
@Component("mybatisProduct")
@Primary
@RequiredArgsConstructor
public class MybatisProductRepositoryAdapter implements ProductRepository {

    private final ProductMapper productMapper;

    /**
     * MyBatis 기반 상품 목록 조회
     */
    @Override
    public List<ProductDto> findAll(ProductDto paramDto) {
        return productMapper.findAll(paramDto);
    }

    /**
     * MyBatis 기반 상품 단건 조회
     */
    @Override
    public ProductDto findById(Long productId) {
        return productMapper.findById(productId)
                .orElseThrow(() -> new BwgProductException.Builder()
                        .code(ProductErrorCode.PRODUCT_NOT_FOUND)
                        .message(ProductErrorCode.PRODUCT_NOT_FOUND.getMsg())
                        .build());
    }
}
