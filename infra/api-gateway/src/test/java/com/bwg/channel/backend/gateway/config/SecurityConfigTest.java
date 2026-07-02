package com.bwg.channel.backend.gateway.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

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
                        "/system-svc/v3/api-docs"
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
}
