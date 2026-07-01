package com.bwg.channel.backend.productsvc.repository.mybatis.mapper;

import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductResDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 상품 SQL 매핑을 담당하는 MyBatis Mapper
 */
@Mapper
public interface ProductMapper {

    /**
     * 상품 목록 조회
     */
    List<ProductResDto> findAll(ProductReqDto paramDto);

    /**
     * 상품 ID 기준 단건 조회
     */
    Optional<ProductResDto> findById(@Param("productId") Long productId);
}
