package com.bwg.channel.backend.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "spring.jwt.secret=01234567890123456789012345678901",
        "eureka.client.enabled=false",
        "app.log-path=build/logs"
})
class ApiGatewayApplicationTests {

    @LocalServerPort
    int port;

    @Test
    void contextLoads() {
    }

    @Test
    void swaggerConfigLoads() {
        String body = WebClient.create("http://localhost:" + port)
                .get()
                .uri("/v3/api-docs/swagger-config")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        org.assertj.core.api.Assertions.assertThat(body)
                .contains("/auth-svc/v3/api-docs")
                .contains("/product-svc/v3/api-docs")
                .contains("/system-svc/v3/api-docs");
    }

    @Test
    void swaggerInitializerUsesInlineServiceUrls() {
        String body = WebClient.create("http://localhost:" + port)
                .get()
                .uri("/swagger-ui/swagger-initializer.js")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        org.assertj.core.api.Assertions.assertThat(body)
                .doesNotContain("configUrl")
                .contains("/auth-svc/v3/api-docs")
                .contains("/product-svc/v3/api-docs")
                .contains("/system-svc/v3/api-docs");
    }
}
