package com.bwg.channel.backend.productsvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "spring.jwt.secret=01234567890123456789012345678901",
        "spring.datasource.jpa-main.jdbc-url=jdbc:h2:mem:product_jpa;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.mybatis-main.jdbc-url=jdbc:h2:mem:product_mybatis;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.jpa-main.driver-class-name=org.h2.Driver",
        "spring.datasource.mybatis-main.driver-class-name=org.h2.Driver",
        "spring.datasource.jpa-main.username=sa",
        "spring.datasource.mybatis-main.username=sa",
        "spring.datasource.jpa-main.password=",
        "spring.datasource.mybatis-main.password=",
        "eureka.client.enabled=false",
        "app.log-path=build/logs"
})
class ProductServiceApplicationTests {

    @LocalServerPort
    int port;

    @Test
    void contextLoads() {
    }

    @Test
    void apiDocsLoads() {
        String body = new TestRestTemplate()
                .getForObject("http://localhost:" + port + "/product/v3/api-docs", String.class);

        org.assertj.core.api.Assertions.assertThat(body)
                .contains("Product Service API")
                .contains("ProductListRequest")
                .contains("상품 ID");
    }
}
