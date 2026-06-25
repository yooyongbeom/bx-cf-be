package com.bwg.channel.backend.systemsvc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.mybatis-main.jdbc-url=jdbc:h2:mem:system_mybatis;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.mybatis-main.driver-class-name=org.h2.Driver",
        "spring.datasource.mybatis-main.username=sa",
        "spring.datasource.mybatis-main.password=",
        "eureka.client.enabled=false",
        "app.log-path=build/logs"
})
class SystemServiceApplicationTests {

    @LocalServerPort
    int port;

    @Test
    void contextLoads() {
    }

    @Test
    void apiDocsLoads() {
        String body = new TestRestTemplate()
                .getForObject("http://localhost:" + port + "/system/v3/api-docs", String.class);

        org.assertj.core.api.Assertions.assertThat(body)
                .contains("System Service API")
                .contains("메뉴 ID");
    }
}
