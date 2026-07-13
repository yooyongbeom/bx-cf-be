package com.bwg.channel.backend.mcicommon.registry;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * MCI 거래 정의.
 * <p>
 * 지금은 YAML에서 이 모델로 바인딩하지만, 나중에 system-svc 관리자 화면이 붙으면 같은 구조를 DB에서 읽도록
 * registry 구현만 교체하면 된다.
 */
@Getter
@Setter
public class TransactionDefinition {

    /** 채널이 요청하는 표준 거래 코드. 예: CIF001. */
    private String code;

    /** 운영자가 식별하기 쉬운 거래명. */
    private String name;

    /** 거래 목적과 특이사항을 적는 설명. */
    private String description;

    /** false면 registry에는 남겨두되 라우팅하지 않는다. */
    private boolean enabled;

    /** 이 거래를 호출할 수 있는 채널 코드 목록. 비어 있으면 채널 제한을 두지 않는다. */
    private List<String> channels = new ArrayList<>();

    /** CORE_BANKING, INFO_SYSTEM처럼 운영 관점에서 보는 대상 시스템 구분. */
    private String targetSystem;

    /** 실제 통신을 담당할 MciAdapter bean 이름. */
    private String adapter;

    /** 표준 요청을 고객사 전문으로 바꿀 MciMapper bean 이름. */
    private String requestMapper;

    /** 고객사 응답을 표준 응답으로 바꿀 MciMapper bean 이름. */
    private String responseMapper;

    /** 거래별 제한 시간(ms). 실제 timeout 적용은 adapter 구현에서 사용한다. */
    private Integer timeoutMs;

    /** 일시 오류 재시도 여부. 실제 재시도 정책은 adapter 또는 router 확장 지점에서 사용한다. */
    private boolean retry;

    /** payload 로그 적재 여부. 금융 데이터는 마스킹 정책과 함께 사용해야 한다. */
    private boolean logPayload;

    /** 로그 적재 시 마스킹해야 할 필드명 목록. */
    private List<String> maskFields = new ArrayList<>();
}
