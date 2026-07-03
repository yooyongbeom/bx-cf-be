package com.bwg.channel.backend.authsvc.controller;

import com.bwg.channel.backend.common.config.OpenApiSupport;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationControllerOpenApiTest {

    @Test
    void logoutRequiresBearerAuthInOpenApi() throws NoSuchMethodException {
        Method logout = AuthenticationController.class.getDeclaredMethod(
                "logout",
                String.class,
                String.class,
                jakarta.servlet.http.HttpServletResponse.class
        );

        assertThat(logout.getAnnotationsByType(SecurityRequirement.class))
                .extracting(SecurityRequirement::name)
                .contains(OpenApiSupport.BEARER_SCHEME);
    }
}
