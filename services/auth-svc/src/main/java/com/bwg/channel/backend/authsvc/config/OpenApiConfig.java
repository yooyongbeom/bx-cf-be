package com.bwg.channel.backend.authsvc.config;

import com.bwg.channel.backend.common.configuration.OpenApiSupport;
import com.bwg.channel.backend.common.configuration.ResponseWrapperSchemaCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    // API Gateway를 통해 호출되는 베이스 경로. (gateway StripPrefix=4 -> /auth/** 로 전달)
    private static final String GATEWAY_BASE_PATH = "/channel/backend/api/v1/auth";

    @Bean
    public OpenAPI authOpenAPI() {
        // 로그인/토큰 발급 API는 게이트웨이에서 permitAll 이므로 global 보안 requirement는 걸지 않는다.
        return OpenApiSupport.base("Auth Service API", "인증 / 토큰 발급 API")
                .addServersItem(new Server().url(GATEWAY_BASE_PATH).description("API Gateway"))
                .tags(List.of(
                        new Tag().name("인증").description("ERP 로그인, 일반 로그인, 토큰 재발급")
                ));
    }

    // 공통 응답 래퍼(ApiResponse*/CommonResponse*)를 Schemas 목록에서 제거 (응답엔 봉투 구조를 인라인 유지)
    @Bean
    public OpenApiCustomizer responseWrapperSchemaCustomizer() {
        return new ResponseWrapperSchemaCustomizer();
    }
}
