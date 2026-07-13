package com.bwg.channel.backend.integrationsvc;

import com.bwg.channel.backend.integrationsvc.githubwebhook.config.GithubWebhookProperties;
import com.bwg.channel.backend.integrationsvc.notion.config.NotionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 대외계 연동 서비스 Spring Boot 애플리케이션 진입점.
 * <p>
 * GitHub, Notion처럼 특정 업무 서비스(auth/product/system)에 직접 속하지 않는
 * 외부 시스템 연동 기능을 이 서비스에서 담당한다.
 */
@SpringBootApplication(
        scanBasePackages = {"com.bwg.channel.backend.*"}
)
@EnableConfigurationProperties({
        GithubWebhookProperties.class,
        NotionProperties.class
})
public class IntegrationServiceApplication {

    /**
     * integration-svc 애플리케이션을 기동한다.
     */
    public static void main(String[] args) {
        SpringApplication.run(IntegrationServiceApplication.class, args);
    }
}
