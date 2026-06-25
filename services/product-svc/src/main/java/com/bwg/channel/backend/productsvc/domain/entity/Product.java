package com.bwg.channel.backend.productsvc.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 상품 테이블(products) JPA Entity
 */
@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, name = "product_nm")
    private String productNm;

    @Column(name = "product_desc", columnDefinition = "TEXT")
    private String productDesc;

    @Column(nullable = false, name = "price")
    private Long price;

    @Column(nullable = false, name = "stock_qty")
    private Integer stockQty;

    @Column(nullable = false, name = "use_yn", length = 1)
    private String useYn = "Y";
}
