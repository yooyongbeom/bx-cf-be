package com.bwg.channel.backend.productsvc.controller;

import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerMappingTests {

    @Test
    void productListUsesPostBodyRequest() throws NoSuchMethodException {
        Method method = ProductController.class.getDeclaredMethod("getProductList", ProductReqDto.class);

        assertThat(method.getAnnotation(GetMapping.class)).isNull();
        assertThat(method.getAnnotation(PostMapping.class).value()).containsExactly("/list");
        assertThat(method.getParameters()[0].getAnnotation(RequestBody.class)).isNotNull();
    }

    @Test
    void productDetailUsesStaticDetailSegment() throws NoSuchMethodException {
        Method method = ProductController.class.getDeclaredMethod("getProduct", Long.class);

        assertThat(method.getAnnotation(GetMapping.class)).isNull();
        // 엔드포인트 ID가 "detail"로 잡히도록 정적 세그먼트를 둔다
        assertThat(method.getAnnotation(PostMapping.class).value()).containsExactly("/detail/{productId}");
    }
}
