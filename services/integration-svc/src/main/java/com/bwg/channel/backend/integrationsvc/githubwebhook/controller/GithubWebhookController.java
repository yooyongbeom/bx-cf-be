package com.bwg.channel.backend.integrationsvc.githubwebhook.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.integrationsvc.githubwebhook.dto.GithubWebhookResDto;
import com.bwg.channel.backend.integrationsvc.githubwebhook.service.GithubWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대외계-GitHub Webhook")
@RestController
@RequiredArgsConstructor
@RequestMapping("/github")
public class GithubWebhookController {

    /** GitHub webhook 요청 검증, 이벤트 필터링, Notion 연동을 담당하는 서비스. */
    private final GithubWebhookService githubWebhookService;

    /**
     * GitHub webhook 요청을 수신한다.
     * <p>
     * 이 API는 브라우저 사용자 JWT가 없는 GitHub 서버 간 호출이므로 Gateway에서 permit 처리하고,
     * 실제 신뢰성 검증은 {@code X-Hub-Signature-256} 서명으로 수행한다.
     *
     * @param eventName GitHub 이벤트 이름. issue 이벤트는 {@code issues}로 들어온다.
     * @param deliveryId GitHub가 webhook 전송마다 발급하는 고유 ID. 중복 처리 방지에 사용한다.
     * @param signature GitHub가 raw payload와 secret으로 계산한 HMAC-SHA256 서명.
     * @param payload GitHub가 보낸 원본 JSON 문자열. 서명 검증을 위해 변형하지 않고 전달한다.
     * @return webhook 처리 결과. created/ignored/duplicate 중 하나를 payload에 담는다.
     */
    @Operation(summary = "GitHub Webhook 수신", description = "GitHub issue 이벤트를 검증하고 Notion 할일 DB row 생성 요청으로 변환한다.")
    @PostMapping("/webhook")
    public ApiResponse<GithubWebhookResDto> receive(
            @RequestHeader(name = "X-GitHub-Event", required = false) String eventName,
            @RequestHeader(name = "X-GitHub-Delivery", required = false) String deliveryId,
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload
    ) {
        return ApiResponse.success(githubWebhookService.handle(eventName, deliveryId, signature, payload));
    }
}
