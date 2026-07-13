package com.bwg.channel.backend.systemsvc.mci.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * MCI 거래 관리 응답 DTO.
 * <p>
 * system-svc는 관리자/운영자에게 거래 정의를 보여주는 역할을 맡고, 실제 거래 실행은 mci-svc가 담당한다.
 */
@Getter
@Builder
public class MciTransactionResDto {

    /** 표준 거래 코드. */
    private String code;

    /** 거래명. */
    private String name;

    /** 거래 설명. */
    private String description;

    /** 거래 사용 여부. */
    private boolean enabled;

    /** 허용 채널 코드 목록. */
    @Builder.Default
    private List<String> channels = new ArrayList<>();

    /** 대상 시스템 구분. */
    private String targetSystem;

    /** adapter 이름. */
    private String adapter;

    /** 요청 mapper 이름. */
    private String requestMapper;

    /** 응답 mapper 이름. */
    private String responseMapper;

    /** 거래 제한 시간(ms). */
    private Integer timeoutMs;

    /** 재시도 사용 여부. */
    private boolean retry;

    /** payload 로그 적재 여부. */
    private boolean logPayload;

    /** 마스킹 필드 목록. */
    @Builder.Default
    private List<String> maskFields = new ArrayList<>();

    /** 현재 저장소 종류. YAML 단계인지 DB 단계인지 운영자가 알 수 있게 한다. */
    private String source;
}
