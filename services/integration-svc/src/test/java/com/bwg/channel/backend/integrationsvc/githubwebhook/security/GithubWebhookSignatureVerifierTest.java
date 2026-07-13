package com.bwg.channel.backend.integrationsvc.githubwebhook.security;

import com.bwg.channel.backend.integrationsvc.githubwebhook.config.GithubWebhookProperties;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class GithubWebhookSignatureVerifierTest {

    @Test
    void acceptsGithubSha256SignatureForRawPayload() throws Exception {
        GithubWebhookProperties properties = new GithubWebhookProperties();
        properties.setSecret("github-secret");
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier(properties);

        String payload = "{\"action\":\"opened\"}";
        String signature = "sha256=" + hmacSha256Hex("github-secret", payload);

        assertThat(verifier.isValid(payload, signature)).isTrue();
    }

    @Test
    void rejectsMissingOrMismatchedSignature() {
        GithubWebhookProperties properties = new GithubWebhookProperties();
        properties.setSecret("github-secret");
        GithubWebhookSignatureVerifier verifier = new GithubWebhookSignatureVerifier(properties);

        assertThat(verifier.isValid("{\"action\":\"opened\"}", null)).isFalse();
        assertThat(verifier.isValid("{\"action\":\"opened\"}", "sha256=bad")).isFalse();
    }

    private String hmacSha256Hex(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
