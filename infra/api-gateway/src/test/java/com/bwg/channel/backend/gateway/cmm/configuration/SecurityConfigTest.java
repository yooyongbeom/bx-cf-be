package com.bwg.channel.backend.gateway.cmm.configuration;

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
}
