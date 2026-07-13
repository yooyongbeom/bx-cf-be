package com.bwg.channel.backend.integrationsvc.notion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class NotionCreatePageReqDto {
    /** Notion page를 생성할 부모 database 지정 정보. 예: {"database_id": "..."} */
    private final Map<String, String> parent;
    /** Notion database 컬럼별 page 속성 값. */
    private final Map<String, Object> properties;

    /**
     * Notion API가 요구하는 {@code parent} JSON 필드로 직렬화한다.
     */
    @JsonProperty("parent")
    public Map<String, String> getParent() {
        return parent;
    }

    /**
     * Notion API가 요구하는 {@code properties} JSON 필드로 직렬화한다.
     */
    @JsonProperty("properties")
    public Map<String, Object> getProperties() {
        return properties;
    }
}
