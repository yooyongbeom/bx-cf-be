package com.bwg.channel.backend.mcisvc.config;

import com.bwg.channel.backend.common.openapi.OpenApiSupport;
import com.bwg.channel.backend.common.openapi.customizer.ResponseWrapperSchemaCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mci-svc Swagger 문서 기본 정보와 Gateway 호출 경로를 설정한다.
 * <p>
 * Gateway Swagger UI에서 Try it out을 누르면 브라우저가 이 servers.url을 기준으로 호출한다. 따라서 mci-svc의
 * 직접 주소(/mci)가 아니라 Gateway 외부 API 경로(/channel/backend/api/v1/mci)를 명시해야 CORS 없이 JWT 검증
 * 흐름을 그대로 탈 수 있다.
 */
@Configuration
public class OpenApiConfig {

    /** API Gateway를 통해 mci-svc를 호출하는 외부 기준 경로. */
    private static final String GATEWAY_BASE_PATH = "/channel/backend/api/v1/mci";

    /**
     * MCI API 문서 제목, Gateway server 경로, JWT 보안 요구사항, 태그를 구성한다.
     */
    @Bean
    public OpenAPI mciOpenApi() {
        return OpenApiSupport.base("MCI Service API", "금융 채널 MCI 거래 실행 API")
                .addServersItem(new Server().url(GATEWAY_BASE_PATH).description("API Gateway"))
                .addSecurityItem(new SecurityRequirement().addList(OpenApiSupport.BEARER_SCHEME))
                .tags(List.of(
                        new Tag().name("MCI").description("표준 MCI 요청을 거래 코드 기준으로 adapter/mapper에 라우팅")
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
