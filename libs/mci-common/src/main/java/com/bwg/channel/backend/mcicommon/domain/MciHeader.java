package com.bwg.channel.backend.mcicommon.domain;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MCI 표준 전문의 공통 헤더.
 * <p>
 * 채널, Gateway, MCI, 고객사 시스템을 지나도 추적 기준이 흔들리지 않도록 거래 식별자와 사용자/채널 정보를
 * 표준 필드로 고정한다. 고객사별로 추가 헤더가 필요하면 payload 또는 mapper 단계에서 확장한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MciHeader {

    /** 요청부터 응답까지 한 번의 거래 흐름을 추적하는 ID. */
    private String traceId;

    /** WEB, MOBILE, BATCH처럼 요청이 시작된 채널 코드. */
    private String channelCode;

    /** 거래 registry에서 조회할 표준 거래 코드. */
    private String transactionCode;

    /** 채널 사용자를 식별하는 ID. 내부 인증 사용자 또는 고객사 사용자 ID가 들어올 수 있다. */
    private String userId;

    /** 로그인 세션, 단말 세션 등 거래 맥락을 이어가기 위한 선택 필드. */
    private String sessionId;

    /** 채널에서 요청을 만든 시각. */
    private OffsetDateTime requestAt;

    /** MCI가 표준 응답을 만든 시각. */
    private OffsetDateTime responseAt;

    /** MCI 내부 라우팅과 adapter 호출에 걸린 시간(ms). */
    private Long elapsedMs;
}
