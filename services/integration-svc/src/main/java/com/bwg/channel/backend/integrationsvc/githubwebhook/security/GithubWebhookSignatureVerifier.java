package com.bwg.channel.backend.integrationsvc.githubwebhook.security;

import com.bwg.channel.backend.integrationsvc.githubwebhook.config.GithubWebhookProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class GithubWebhookSignatureVerifier {

    /** GitHub가 HMAC-SHA256 서명 헤더에 붙이는 고정 prefix. */
    private static final String SIGNATURE_PREFIX = "sha256=";
    /** GitHub webhook secret 설정. */
    private final GithubWebhookProperties properties;

    /**
     * GitHub webhook 요청의 서명을 검증한다.
     * <p>
     * GitHub는 raw payload와 webhook secret으로 HMAC-SHA256을 계산한 뒤
     * {@code X-Hub-Signature-256} 헤더로 전달한다.
     *
     * @param payload GitHub가 보낸 원본 JSON 문자열.
     * @param actualSignature 요청 헤더의 {@code X-Hub-Signature-256} 값.
     * @return secret과 서명이 모두 유효하면 true.
     */
    public boolean isValid(String payload, String actualSignature) {
        if (!StringUtils.hasText(properties.getSecret()) || !StringUtils.hasText(actualSignature)) {
            return false;
        }
        if (!actualSignature.startsWith(SIGNATURE_PREFIX)) {
            return false;
        }

        String expectedSignature = SIGNATURE_PREFIX + hmacSha256Hex(properties.getSecret(), payload);
        // 서명 비교는 길이나 일부 문자 일치 여부가 노출되지 않도록 constant-time 비교를 사용한다.
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                actualSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * webhook secret과 raw payload로 GitHub 서명과 동일한 HMAC-SHA256 hex 값을 계산한다.
     */
    private String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to verify GitHub webhook signature", ex);
        }
    }
}
