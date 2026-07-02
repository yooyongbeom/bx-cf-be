package com.bwg.channel.backend.systemsvc.config;

import com.bwg.channel.backend.common.config.OpenApiSupport;
import com.bwg.channel.backend.common.config.ResponseWrapperSchemaCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system-svc의 Swagger 문서 기본 정보와 보안/응답 스키마 후처리를 설정한다.
 */
@Configuration
public class OpenApiConfig {

    private static final String GATEWAY_BASE_PATH = "/channel/backend/api/v1/system";

    /**
     * 기준정보 API 문서의 제목, 게이트웨이 서버 경로, 보안 요구사항, 태그를 구성한다.
     */
    @Bean
    public OpenAPI systemOpenApi() {
        return OpenApiSupport.base("System Service API", "기준정보 관리 API")
                .addServersItem(new Server().url(GATEWAY_BASE_PATH).description("API Gateway"))
                .addSecurityItem(new SecurityRequirement().addList(OpenApiSupport.BEARER_SCHEME))
                .tags(java.util.List.of(
                        new Tag().name("기준정보-메뉴").description("메뉴 및 역할별 메뉴 권한 관리"),
                        new Tag().name("기준정보-공통코드").description("공통코드 그룹 및 공통코드 관리")
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
