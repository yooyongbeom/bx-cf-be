package com.bwg.channel.backend.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void permitsAllSwaggerApiDocsProxyPaths() throws Exception {
        Field field = SecurityConfig.class.getDeclaredField("PERMIT_URL_ARRAY");
        field.setAccessible(true);

        String[] permitUrls = (String[]) field.get(null);

        assertThat(Arrays.asList(permitUrls))
                .contains(
                        "/auth-svc/v3/api-docs",
                        "/product-svc/v3/api-docs",
                        "/system-svc/v3/api-docs",
                        "/integration-svc/v3/api-docs",
                        "/mci-svc/v3/api-docs"
                );
    }

    @Test
    void permitsOnlyPreAuthenticationAuthPaths() throws Exception {
        Field field = SecurityConfig.class.getDeclaredField("PERMIT_URL_ARRAY");
        field.setAccessible(true);

        String[] permitUrls = (String[]) field.get(null);

        assertThat(Arrays.asList(permitUrls))
                .doesNotContain("/channel/backend/api/v1/auth/**")
                .contains(
                        "/channel/backend/api/v1/auth/login",
                        "/channel/backend/api/v1/auth/erp-login",
                        "/channel/backend/api/v1/auth/refresh-token",
                        "/channel/backend/api/v1/auth/signup",
                        "/channel/backend/api/v1/auth/password/find",
                        "/channel/backend/api/v1/auth/password/reset-request",
                        "/channel/backend/api/v1/auth/password/reset"
                );
    }

    @Test
    void permitsGithubWebhookWithoutJwtBecauseItUsesGithubSignature() throws Exception {
        Field field = SecurityConfig.class.getDeclaredField("PERMIT_URL_ARRAY");
        field.setAccessible(true);

        String[] permitUrls = (String[]) field.get(null);

        assertThat(Arrays.asList(permitUrls))
                .contains("/channel/backend/api/v1/integration/github/webhook");
    }

    @Test
    void corsConfigurationUsesConfiguredOrigins() {
        GatewayCorsProperties properties = new GatewayCorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://localhost:8000", "http://127.0.0.1:8000"));
        properties.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        properties.setAllowedHeaders(List.of("*"));
        properties.setAllowCredentials(true);
        properties.setMaxAge(3600L);

        SecurityConfig securityConfig = new SecurityConfig(null, properties);

        CorsConfiguration corsConfiguration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(MockServerWebExchange.from(MockServerHttpRequest.get("/test").build()));

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.getAllowedOriginPatterns())
                .containsExactly("http://localhost:8000", "http://127.0.0.1:8000");
        assertThat(corsConfiguration.getAllowedMethods())
                .containsExactly("GET", "POST", "OPTIONS");
        assertThat(corsConfiguration.getAllowedHeaders()).containsExactly("*");
        assertThat(corsConfiguration.getAllowCredentials()).isTrue();
        assertThat(corsConfiguration.getMaxAge()).isEqualTo(3600L);
    }
}
