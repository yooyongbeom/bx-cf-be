package com.bwg.channel.backend.integrationsvc.githubwebhook.mapper;

import com.bwg.channel.backend.integrationsvc.githubwebhook.domain.GithubIssueWebhookEvent;
import com.bwg.channel.backend.integrationsvc.notion.config.NotionProperties;
import com.bwg.channel.backend.integrationsvc.notion.dto.NotionCreatePageReqDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubIssueWebhookMapper {

    /** GitHub webhook JSON payload를 트리로 파싱하기 위한 Jackson mapper. */
    private final ObjectMapper objectMapper;
    /** Notion database id 등 Notion page 생성에 필요한 설정. */
    private final NotionProperties notionProperties;

    /**
     * GitHub issue webhook 원문 payload를 내부 도메인 이벤트로 변환한다.
     *
     * @param deliveryId GitHub webhook 전송 고유 ID.
     * @param payload GitHub가 보낸 원본 JSON 문자열.
     * @return Notion 매핑에 필요한 필드만 추린 내부 이벤트 객체.
     */
    public GithubIssueWebhookEvent toIssueEvent(String deliveryId, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode issue = root.path("issue");

            return GithubIssueWebhookEvent.builder()
                    .deliveryId(deliveryId)
                    .action(root.path("action").asText())
                    .repositoryFullName(root.path("repository").path("full_name").asText())
                    .issueNumber(issue.path("number").asInt())
                    .issueTitle(issue.path("title").asText())
                    .issueUrl(issue.path("html_url").asText())
                    .issueState(issue.path("state").asText())
                    .createdAt(issue.path("created_at").asText())
                    .updatedAt(issue.path("updated_at").asText())
                    .labels(textList(issue.path("labels"), "name"))
                    .assignees(textList(issue.path("assignees"), "login"))
                    .build();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid GitHub issue webhook payload", ex);
        }
    }

    /**
     * 내부 GitHub issue 이벤트를 Notion pages API 요청 body로 변환한다.
     * <p>
     * Notion DB에는 아래 속성이 존재한다는 전제다.
     * Name, Status, Repository, GitHub URL, Issue Number, Labels, Assignees,
     * Created At, Updated At, GitHub Delivery ID.
     *
     * @param event GitHub issue webhook에서 추출한 내부 이벤트.
     * @return Notion {@code POST /v1/pages} 요청 DTO.
     */
    public NotionCreatePageReqDto toNotionCreatePageRequest(GithubIssueWebhookEvent event) {
        return new NotionCreatePageReqDto(
                Map.of("database_id", notionProperties.getDatabaseId()),
                Map.of(
                        "Name", title(event.getIssueTitle()),
                        "Status", select(event.getAction()),
                        "Repository", richText(event.getRepositoryFullName()),
                        "GitHub URL", url(event.getIssueUrl()),
                        "Issue Number", number(event.getIssueNumber()),
                        "Labels", multiSelect(event.getLabels()),
                        "Assignees", multiSelect(event.getAssignees()),
                        "Created At", date(event.getCreatedAt()),
                        "Updated At", date(event.getUpdatedAt()),
                        "GitHub Delivery ID", richText(event.getDeliveryId())
                )
        );
    }

    /**
     * GitHub labels/assignees처럼 객체 배열로 들어오는 값을 문자열 목록으로 평탄화한다.
     */
    private List<String> textList(JsonNode arrayNode, String fieldName) {
        List<String> values = new ArrayList<>();
        if (!arrayNode.isArray()) {
            return values;
        }
        arrayNode.forEach(node -> {
            String value = node.path(fieldName).asText();
            if (!value.isBlank()) {
                values.add(value);
            }
        });
        return values;
    }

    /** Notion title 속성 형식으로 변환한다. */
    private Map<String, Object> title(String content) {
        return Map.of("title", List.of(Map.of("text", Map.of("content", safe(content)))));
    }

    /** Notion rich_text 속성 형식으로 변환한다. */
    private Map<String, Object> richText(String content) {
        return Map.of("rich_text", List.of(Map.of("text", Map.of("content", safe(content)))));
    }

    /** Notion select 속성 형식으로 변환한다. */
    private Map<String, Object> select(String name) {
        return Map.of("select", Map.of("name", safe(name)));
    }

    /** Notion multi_select 속성 형식으로 변환한다. */
    private Map<String, Object> multiSelect(List<String> names) {
        return Map.of("multi_select", names.stream()
                .map(name -> Map.of("name", safe(name)))
                .toList());
    }

    /** Notion url 속성 형식으로 변환한다. */
    private Map<String, Object> url(String value) {
        return Map.of("url", safe(value));
    }

    /** Notion number 속성 형식으로 변환한다. */
    private Map<String, Object> number(int value) {
        return Map.of("number", value);
    }

    /** Notion date 속성 형식으로 변환한다. */
    private Map<String, Object> date(String value) {
        return Map.of("date", Map.of("start", safe(value)));
    }

    /** Notion API에 null 문자열이 들어가지 않도록 빈 문자열로 보정한다. */
    private String safe(String value) {
        return value == null ? "" : value;
    }
}
