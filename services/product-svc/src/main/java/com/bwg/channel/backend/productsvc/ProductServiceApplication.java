package com.bwg.channel.backend.productsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 상품(product-svc) Spring Boot 애플리케이션 진입점
 */
@SpringBootApplication(
    scanBasePackages = {"com.bwg.channel.backend.*"}
)
public class ProductServiceApplication {

    /**
     * product-svc 애플리케이션 실행
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
