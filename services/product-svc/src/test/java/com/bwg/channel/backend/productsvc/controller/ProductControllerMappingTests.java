package com.bwg.channel.backend.productsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerMappingTests {

    @Test
    void productListUsesPostBodyRequest() throws NoSuchMethodException {
        Method method = ProductController.class.getDeclaredMethod("getProductList", ApiRequest.class);

        assertThat(method.getAnnotation(GetMapping.class)).isNull();
        assertThat(method.getAnnotation(PostMapping.class).value()).containsExactly("/list");
        assertThat(method.getParameters()[0].getAnnotation(RequestBody.class)).isNotNull();

        Type requestType = method.getGenericParameterTypes()[0];
        assertThat(requestType).isInstanceOf(ParameterizedType.class);
        ParameterizedType parameterizedType = (ParameterizedType) requestType;
        assertThat(parameterizedType.getRawType()).isEqualTo(ApiRequest.class);
        assertThat(parameterizedType.getActualTypeArguments()).containsExactly(ProductReqDto.class);
    }

    @Test
    void productDetailUsesStaticDetailSegment() throws NoSuchMethodException {
        Method method = ProductController.class.getDeclaredMethod("getProduct", Long.class);

        assertThat(method.getAnnotation(GetMapping.class)).isNull();
        // 엔드포인트 ID가 "detail"로 잡히도록 정적 세그먼트를 둔다
        assertThat(method.getAnnotation(PostMapping.class).value()).containsExactly("/detail/{productId}");
    }
}
