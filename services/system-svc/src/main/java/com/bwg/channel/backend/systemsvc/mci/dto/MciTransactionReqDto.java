package com.bwg.channel.backend.systemsvc.mci.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * MCI 거래 관리 요청 DTO.
 * <p>
 * 초기에는 mci-svc YAML 구조와 같은 필드를 사용한다. 나중에 관리자 화면/DB 저장으로 전환해도 API 계약은 크게
 * 흔들리지 않도록 거래 정의 단위를 기준으로 설계한다.
 */
@Getter
@Setter
public class MciTransactionReqDto {

    /** 조회/저장 대상 거래 코드. */
    private String code;

    /** 운영자가 식별할 거래명. */
    private String name;

    /** 거래 설명. */
    private String description;

    /** 거래 사용 여부. */
    private Boolean enabled;

    /** 허용 채널 코드 목록. */
    private List<String> channels = new ArrayList<>();

    /** 계정계/정보계/대외계 등 대상 시스템 구분. */
    private String targetSystem;

    /** 거래가 사용할 adapter 이름. */
    private String adapter;

    /** 표준 요청을 고객사 요청으로 바꿀 mapper 이름. */
    private String requestMapper;

    /** 고객사 응답을 표준 응답으로 바꿀 mapper 이름. */
    private String responseMapper;

    /** 거래 제한 시간(ms). */
    private Integer timeoutMs;

    /** 재시도 사용 여부. */
    private Boolean retry;

    /** payload 로그 적재 여부. */
    private Boolean logPayload;

    /** 로그 적재 시 마스킹 필드 목록. */
    private List<String> maskFields = new ArrayList<>();
}
