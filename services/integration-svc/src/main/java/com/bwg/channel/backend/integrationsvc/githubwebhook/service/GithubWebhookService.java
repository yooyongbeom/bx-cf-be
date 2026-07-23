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

/**
 * GitHub webhook의 필수 헤더와 서명을 검증하고 지원하는 issue 이벤트를 Notion 할일로 적재한다.
 *
 * <p>동일 프로세스 안에서는 delivery ID를 기준으로 중복 처리를 방지하고, payload 변환이나
 * Notion 호출이 실패하면 재시도를 허용하기 위해 delivery ID 마킹을 제거한다.</p>
 */
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
     * <p>필수 헤더와 HMAC 서명을 검증한 뒤 {@code issues} 이외 이벤트를 무시한다.
     * issue 이벤트는 delivery ID로 프로세스 내 중복을 차단하고 지원 action만 Notion page로
     * 생성하며, payload 변환이나 Notion 호출이 실패하면 재시도를 위해 delivery ID 마킹을 제거한다.</p>
     *
     * @param eventName GitHub 이벤트 이름.
     * @param deliveryId GitHub webhook 전송 고유 ID.
     * @param signature GitHub HMAC-SHA256 서명 헤더.
     * @param payload GitHub webhook 원본 JSON 문자열.
     * @return 처리 결과 DTO.
     * @throws ResponseStatusException 필수 헤더가 없거나 webhook 서명이 유효하지 않은 경우
     * @throws RuntimeException payload 변환 또는 Notion page 생성이 실패한 경우
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
     *
     * @param eventName {@code X-GitHub-Event} 헤더 값
     * @param deliveryId {@code X-GitHub-Delivery} 헤더 값
     * @throws ResponseStatusException 이벤트 이름 또는 delivery ID가 비어 있는 경우
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
