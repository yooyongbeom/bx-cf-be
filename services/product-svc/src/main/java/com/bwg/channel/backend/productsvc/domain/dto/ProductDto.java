package com.bwg.channel.backend.productsvc.domain.dto;

import lombok.Data;

@Data
public class ProductDto {
    private Long   productId;
    private String productNm;
    private String productDesc;
    private Long   price;
    private Integer stockQty;
    private String useYn;
}
