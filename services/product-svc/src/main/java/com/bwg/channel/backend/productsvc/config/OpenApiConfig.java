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

@Configuration
public class OpenApiConfig {

    // API Gateway를 통해 호출되는 베이스 경로. (gateway StripPrefix=4 -> /product/** 로 전달)
    private static final String GATEWAY_BASE_PATH = "/channel/backend/api/v1/product";

    @Bean
    public OpenAPI productOpenAPI() {
        // 상품 API는 게이트웨이에서 JWT 인증이 필요하므로 global 보안 requirement를 건다.
        return OpenApiSupport.base("Product Service API", "상품 조회 API")
                .addServersItem(new Server().url(GATEWAY_BASE_PATH).description("API Gateway"))
                .addSecurityItem(new SecurityRequirement().addList(OpenApiSupport.BEARER_SCHEME))
                .tags(List.of(
                        new Tag().name("상품").description("상품 목록 조회 및 단건 조회")
                ));
    }

    // 공통 응답 래퍼(ApiResponse*/CommonResponse*)를 Schemas 목록에서 제거 (응답엔 봉투 구조를 인라인 유지)
    @Bean
    public OpenApiCustomizer responseWrapperSchemaCustomizer() {
        return new ResponseWrapperSchemaCustomizer();
    }
}
