package com.bwg.channel.backend.productsvc.repository.mybatis.mapper;

import com.bwg.channel.backend.productsvc.domain.dto.ProductDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductMapper {

    List<ProductDto> findAll(ProductDto paramDto);

    Optional<ProductDto> findById(@Param("productId") Long productId);
}
