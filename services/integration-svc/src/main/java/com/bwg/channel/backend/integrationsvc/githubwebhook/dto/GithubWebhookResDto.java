package com.bwg.channel.backend.integrationsvc.githubwebhook.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class GithubWebhookResDto {
    /** GitHub webhook 전송 고유 ID. */
    private final String deliveryId;
    /** GitHub 이벤트 이름. 예: issues, ping. */
    private final String eventName;
    /** 처리 결과. created, ignored, duplicate 중 하나. */
    private final String result;
    /** issue 이벤트인 경우 매핑된 GitHub issue 번호. */
    private final Integer issueNumber;
}
