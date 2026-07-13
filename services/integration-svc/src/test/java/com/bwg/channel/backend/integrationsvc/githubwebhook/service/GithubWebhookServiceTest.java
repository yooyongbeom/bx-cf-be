package com.bwg.channel.backend.integrationsvc.githubwebhook.service;

import com.bwg.channel.backend.integrationsvc.githubwebhook.config.GithubWebhookProperties;
import com.bwg.channel.backend.integrationsvc.githubwebhook.dto.GithubWebhookResDto;
import com.bwg.channel.backend.integrationsvc.githubwebhook.mapper.GithubIssueWebhookMapper;
import com.bwg.channel.backend.integrationsvc.githubwebhook.security.GithubWebhookSignatureVerifier;
import com.bwg.channel.backend.integrationsvc.notion.client.NotionClient;
import com.bwg.channel.backend.integrationsvc.notion.config.NotionProperties;
import com.bwg.channel.backend.integrationsvc.notion.dto.NotionCreatePageReqDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GithubWebhookServiceTest {

    @Test
    void createsNotionTaskForGithubIssueEvent() throws Exception {
        Fixture fixture = new Fixture();
        String payload = issuePayload("opened", "Add webhook", "https://github.com/acme/demo/issues/7");
        String signature = signature(payload);

        GithubWebhookResDto response = fixture.service.handle("issues", "delivery-1", signature, payload);

        assertThat(response.getResult()).isEqualTo("created");
        assertThat(response.getDeliveryId()).isEqualTo("delivery-1");
        assertThat(response.getIssueNumber()).isEqualTo(7);
        assertThat(fixture.notionClient.requests).hasSize(1);
        assertThat(fixture.notionClient.requests.get(0).getProperties())
                .containsKeys("Name", "Status", "Repository", "GitHub URL", "Issue Number", "GitHub Delivery ID");
    }

    @Test
    void ignoresDuplicateDeliveryId() throws Exception {
        Fixture fixture = new Fixture();
        String payload = issuePayload("edited", "Edit webhook", "https://github.com/acme/demo/issues/8");
        String signature = signature(payload);

        fixture.service.handle("issues", "delivery-2", signature, payload);
        GithubWebhookResDto response = fixture.service.handle("issues", "delivery-2", signature, payload);

        assertThat(response.getResult()).isEqualTo("duplicate");
        assertThat(fixture.notionClient.requests).hasSize(1);
    }

    @Test
    void ignoresUnsupportedGithubEventBeforeCallingNotion() throws Exception {
        Fixture fixture = new Fixture();
        String payload = "{\"zen\":\"Keep it logically awesome.\"}";
        String signature = signature(payload);

        GithubWebhookResDto response = fixture.service.handle("ping", "delivery-3", signature, payload);

        assertThat(response.getResult()).isEqualTo("ignored");
        assertThat(fixture.notionClient.requests).isEmpty();
    }

    private static String issuePayload(String action, String title, String url) {
        return """
                {
                  "action": "%s",
                  "repository": {
                    "full_name": "acme/demo"
                  },
                  "issue": {
                    "number": 7,
                    "title": "%s",
                    "html_url": "%s",
                    "state": "open",
                    "created_at": "2026-07-13T00:00:00Z",
                    "updated_at": "2026-07-13T00:05:00Z",
                    "labels": [
                      {"name": "bug"}
                    ],
                    "assignees": [
                      {"login": "octocat"}
                    ]
                  }
                }
                """.formatted(action, title, url);
    }

    private static String signature(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("github-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private static class Fixture {
        private final CapturingNotionClient notionClient = new CapturingNotionClient();
        private final GithubWebhookService service;

        private Fixture() {
            GithubWebhookProperties githubProperties = new GithubWebhookProperties();
            githubProperties.setSecret("github-secret");

            NotionProperties notionProperties = new NotionProperties();
            notionProperties.setDatabaseId("notion-db");

            service = new GithubWebhookService(
                    new GithubWebhookSignatureVerifier(githubProperties),
                    new GithubIssueWebhookMapper(new ObjectMapper(), notionProperties),
                    notionClient
            );
        }
    }

    private static class CapturingNotionClient implements NotionClient {
        private final List<NotionCreatePageReqDto> requests = new ArrayList<>();

        @Override
        public void createPage(NotionCreatePageReqDto request) {
            requests.add(request);
        }
    }
}
