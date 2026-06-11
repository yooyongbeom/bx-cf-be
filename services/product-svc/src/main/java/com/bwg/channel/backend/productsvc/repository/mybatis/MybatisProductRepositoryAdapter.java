package com.bwg.channel.backend.productsvc.repository.mybatis;

import com.bwg.channel.backend.productsvc.cmm.constants.ProductErrorCode;
import com.bwg.channel.backend.productsvc.cmm.exception.BwgProductException;
import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import com.bwg.channel.backend.productsvc.repository.mybatis.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("mybatisProduct")
@RequiredArgsConstructor
public class MybatisProductRepositoryAdapter implements ProductRepository {

    private final ProductMapper productMapper;

    @Override
    public List<ProductDto> findAll(ProductDto paramDto) {
        return productMapper.findAll(paramDto);
    }

    @Override
    public ProductDto findById(Long productId) {
        return productMapper.findById(productId)
                .orElseThrow(() -> new BwgProductException.Builder()
                        .code(ProductErrorCode.PRODUCT_NOT_FOUND)
                        .message(ProductErrorCode.PRODUCT_NOT_FOUND.getMsg())
                        .build());
    }
}
