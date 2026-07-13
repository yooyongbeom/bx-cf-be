package com.bwg.channel.backend.integrationsvc.githubwebhook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.github.webhook")
public class GithubWebhookProperties {
    /**
     * GitHub webhook 생성 시 등록한 secret.
     * <p>
     * 수신 payload와 함께 HMAC-SHA256 서명을 검증할 때 사용한다.
     */
    private String secret;
}
