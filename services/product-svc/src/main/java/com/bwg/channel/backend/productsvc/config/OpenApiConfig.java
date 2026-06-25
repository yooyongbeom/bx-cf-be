package com.bwg.channel.backend.productsvc.config;

import com.bwg.channel.backend.common.configuration.OpenApiSupport;
import com.bwg.channel.backend.common.configuration.ResponseWrapperSchemaCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * product-svc Swagger 문서 기본 정보와 응답 스키마 후처리 설정
 */
@Configuration
public class OpenApiConfig {

    // API Gateway 호출 베이스 경로
    private static final String GATEWAY_BASE_PATH = "/channel/backend/api/v1/product";

    /**
     * 상품 API 문서 제목, 게이트웨이 서버 경로, 보안 요구사항, 태그 구성
     */
    @Bean
    public OpenAPI productOpenAPI() {
        // 게이트웨이 JWT 인증 요구사항 적용
        return OpenApiSupport.base("Product Service API", "상품 조회 API")
                .addServersItem(new Server().url(GATEWAY_BASE_PATH).description("API Gateway"))
                .addSecurityItem(new SecurityRequirement().addList(OpenApiSupport.BEARER_SCHEME))
                .tags(List.of(
                        new Tag().name("상품").description("상품 목록 조회 및 단건 조회")
                ));
    }

    /**
     * 공통 응답 래퍼 스키마 컴포넌트 정리
     */
    @Bean
    public OpenApiCustomizer responseWrapperSchemaCustomizer() {
        return new ResponseWrapperSchemaCustomizer();
    }
}
