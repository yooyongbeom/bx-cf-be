package com.bwg.channel.backend.authsvc.config;

import com.bwg.channel.backend.common.config.OpenApiSupport;
import com.bwg.channel.backend.common.config.ResponseWrapperSchemaCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * auth-svc Swagger 문서의 기본 정보와 공통 스키마 후처리를 설정한다.
 */
@Configuration
public class OpenApiConfig {

    // API Gateway를 통해 호출되는 베이스 경로. gateway StripPrefix=4 설정 이후 /auth/** 로 전달된다.
    private static final String GATEWAY_BASE_PATH = "/channel/backend/api/v1/auth";

    /**
     * 인증 API 문서의 제목, 서버 경로, 태그 정보를 구성한다.
     */
    @Bean
    public OpenAPI authOpenAPI() {
        // 로그인/토큰 발급 API는 게이트웨이에서 permitAll이므로 global 보안 requirement는 걸지 않는다.
        return OpenApiSupport.base("Auth Service API", "인증 / 토큰 발급 API")
                .addServersItem(new Server().url(GATEWAY_BASE_PATH).description("API Gateway"))
                .tags(List.of(
                        new Tag().name("인증").description("ERP 로그인, 일반 로그인, 토큰 재발급, 로그아웃")
                ));
    }

    /**
     * 공통 응답 래퍼 스키마를 문서 컴포넌트 목록에서 정리한다.
     */
    @Bean
    public OpenApiCustomizer responseWrapperSchemaCustomizer() {
        return new ResponseWrapperSchemaCustomizer();
    }
}
