package com.bwg.channel.backend.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * 모든 서비스가 공유하는 OpenAPI 기본 메타(회사/버전)와 JWT 보안 스킴 "정의"를 제공한다.
 * <p>
 * 보안 적용 여부(global SecurityRequirement), tags, servers 등 서비스마다 갈리는 설정은
 * 각 서비스의 {@code OpenAPI} 빈에서 {@link #base(String, String)} 결과에 덧붙여 결정한다.
 * <p>
 * {@code @Bean}/{@code @Configuration}이 아닌 정적 빌더로 둔 이유: 서비스마다 자기 {@code OpenAPI}
 * 빈을 정의하므로, 공통도 빈으로 두면 빈 충돌이 발생한다.
 */
public final class OpenApiSupport {

    private OpenApiSupport() {
    }

    /** JWT Bearer 보안 스킴 이름. 서비스가 SecurityRequirement를 걸 때 이 이름을 참조한다. */
    public static final String BEARER_SCHEME = "bearerAuth";

    private static final String COMPANY = "BankwareGlobal";
    private static final String VERSION = "0.0.1";

    /**
     * 공통 메타 + JWT 보안 스킴 정의가 채워진 기본 OpenAPI를 만든다.
     *
     * @param title       문서 제목 (서비스별)
     * @param description 문서 설명 (서비스별)
     */
    public static OpenAPI base(String title, String description) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(VERSION)
                        .description(description + " - " + COMPANY))
                // 보안 요구사항 적용 여부는 서비스별로 다르므로 여기서는 scheme 정의만 제공한다.
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Authorization: Bearer {accessToken}")));
    }
}
