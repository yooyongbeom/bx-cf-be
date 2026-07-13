package com.bwg.channel.backend.integrationsvc.githubwebhook.service;

import com.bwg.channel.backend.integrationsvc.githubwebhook.domain.GithubIssueWebhookEvent;
import com.bwg.channel.backend.integrationsvc.githubwebhook.dto.GithubWebhookResDto;
import com.bwg.channel.backend.integrationsvc.githubwebhook.mapper.GithubIssueWebhookMapper;
import com.bwg.channel.backend.integrationsvc.githubwebhook.security.GithubWebhookSignatureVerifier;
import com.bwg.channel.backend.integrationsvc.notion.client.NotionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GithubWebhookService {

    /** GitHub issue 변경 webhook 이벤트 이름. */
    private static final String ISSUE_EVENT = "issues";
    /** Notion 할일 row 생성 대상으로 삼는 issue action 목록. */
    private static final Set<String> SUPPORTED_ISSUE_ACTIONS = Set.of(
            "opened", "edited", "reopened", "closed", "assigned", "unassigned", "labeled", "unlabeled"
    );

    /** GitHub HMAC-SHA256 서명 검증기. */
    private final GithubWebhookSignatureVerifier signatureVerifier;
    /** GitHub issue payload와 Notion page 요청 간 변환기. */
    private final GithubIssueWebhookMapper issueWebhookMapper;
    /** Notion pages API 호출 client. */
    private final NotionClient notionClient;
    /**
     * 프로세스 내 delivery id 중복 방지 저장소.
     * <p>
     * 운영에서 인스턴스가 여러 개이거나 재기동 후 중복 방지가 필요하면 DB/Redis 기반 저장소로 교체해야 한다.
     */
    private final Set<String> processedDeliveryIds = ConcurrentHashMap.newKeySet();

    /**
     * GitHub webhook을 검증하고 지원 대상 이벤트만 Notion 할일 row로 적재한다.
     *
     * @param eventName GitHub 이벤트 이름.
     * @param deliveryId GitHub webhook 전송 고유 ID.
     * @param signature GitHub HMAC-SHA256 서명 헤더.
     * @param payload GitHub webhook 원본 JSON 문자열.
     * @return 처리 결과 DTO.
     */
    public GithubWebhookResDto handle(String eventName, String deliveryId, String signature, String payload) {
        validateHeaders(eventName, deliveryId);
        if (!signatureVerifier.isValid(payload, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid GitHub webhook signature");
        }
        if (!ISSUE_EVENT.equals(eventName)) {
            return GithubWebhookResDto.of(deliveryId, eventName, "ignored", null);
        }
        if (!processedDeliveryIds.add(deliveryId)) {
            return GithubWebhookResDto.of(deliveryId, eventName, "duplicate", null);
        }

        try {
            GithubIssueWebhookEvent issueEvent = issueWebhookMapper.toIssueEvent(deliveryId, payload);
            if (!SUPPORTED_ISSUE_ACTIONS.contains(issueEvent.getAction())) {
                return GithubWebhookResDto.of(deliveryId, eventName, "ignored", issueEvent.getIssueNumber());
            }

            notionClient.createPage(issueWebhookMapper.toNotionCreatePageRequest(issueEvent));
            return GithubWebhookResDto.of(deliveryId, eventName, "created", issueEvent.getIssueNumber());
        } catch (RuntimeException ex) {
            // Notion 호출이나 payload 변환이 실패하면 같은 delivery id를 재처리할 수 있도록 중복 마킹을 되돌린다.
            processedDeliveryIds.remove(deliveryId);
            throw ex;
        }
    }

    /**
     * GitHub webhook 처리에 반드시 필요한 헤더가 들어왔는지 확인한다.
     */
    private void validateHeaders(String eventName, String deliveryId) {
        if (!StringUtils.hasText(eventName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-GitHub-Event header is required");
        }
        if (!StringUtils.hasText(deliveryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-GitHub-Delivery header is required");
        }
    }
}
