package com.bwg.channel.backend.integrationsvc.notion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.notion")
public class NotionProperties {
    /** true일 때만 실제 Notion API를 호출한다. */
    private boolean enabled;
    /** Notion API base URL. 기본값은 공식 API 주소다. */
    private String baseUrl = "https://api.notion.com";
    /** Notion API 버전 헤더 값. */
    private String version = "2022-06-28";
    /** Notion integration secret token. */
    private String token;
    /** 할일 row를 생성할 Notion database id. */
    private String databaseId;
}
