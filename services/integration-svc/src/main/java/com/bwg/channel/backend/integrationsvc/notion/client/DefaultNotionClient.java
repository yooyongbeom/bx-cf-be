package com.bwg.channel.backend.integrationsvc.notion.client;

import com.bwg.channel.backend.integrationsvc.notion.config.NotionProperties;
import com.bwg.channel.backend.integrationsvc.notion.dto.NotionCreatePageReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultNotionClient implements NotionClient {

    /** Spring MVC 기반 외부 HTTP 호출용 RestClient builder. */
    private final RestClient.Builder restClientBuilder;
    /** Notion API base URL, token, database id 설정. */
    private final NotionProperties properties;

    /**
     * Notion pages API를 호출해 database 하위 page(row)를 생성한다.
     * <p>
     * {@code app.notion.enabled=false}인 local/dev 기본 상태에서는 외부 호출 없이 로그만 남긴다.
     *
     * @param request Notion {@code POST /v1/pages} 요청 body.
     */
    @Override
    public void createPage(NotionCreatePageReqDto request) {
        if (!properties.isEnabled()) {
            log.info("Notion integration is disabled. Skipping page creation.");
            return;
        }
        validateProperties();

        // Notion API는 Bearer token과 Notion-Version 헤더를 모두 요구한다.
        restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri("/v1/pages")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .header("Notion-Version", properties.getVersion())
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * 실제 Notion 호출 전에 필수 설정 누락을 명확한 예외로 드러낸다.
     */
    private void validateProperties() {
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new IllegalStateException("app.notion.base-url is required");
        }
        if (!StringUtils.hasText(properties.getToken())) {
            throw new IllegalStateException("app.notion.token is required");
        }
        if (!StringUtils.hasText(properties.getVersion())) {
            throw new IllegalStateException("app.notion.version is required");
        }
        if (!StringUtils.hasText(properties.getDatabaseId())) {
            throw new IllegalStateException("app.notion.database-id is required");
        }
    }
}
