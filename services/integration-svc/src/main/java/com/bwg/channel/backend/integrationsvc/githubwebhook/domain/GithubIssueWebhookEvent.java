package com.bwg.channel.backend.integrationsvc.githubwebhook.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GithubIssueWebhookEvent {
    /** GitHub webhook 전송 단위 고유 ID. */
    private final String deliveryId;
    /** issue 이벤트 action. 예: opened, edited, closed. */
    private final String action;
    /** owner/repository 형식의 GitHub 저장소 전체 이름. */
    private final String repositoryFullName;
    /** GitHub issue 번호. */
    private final int issueNumber;
    /** GitHub issue 제목. Notion row 제목으로 사용한다. */
    private final String issueTitle;
    /** GitHub issue 상세 화면 URL. */
    private final String issueUrl;
    /** GitHub issue 상태. 예: open, closed. */
    private final String issueState;
    /** GitHub issue 생성 시각 ISO-8601 문자열. */
    private final String createdAt;
    /** GitHub issue 최종 수정 시각 ISO-8601 문자열. */
    private final String updatedAt;
    /** GitHub issue label 이름 목록. */
    private final List<String> labels;
    /** GitHub issue assignee login 목록. */
    private final List<String> assignees;
}
