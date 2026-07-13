package com.bwg.channel.backend.mcicommon.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MCI 표준 요청 모델.
 * <p>
 * header는 거래 라우팅/추적에 사용하고, data는 업무별 요청 본문을 담는다. 고객사별 TCP/REST/SOAP/MQ 전문 변환은
 * 이 모델을 기준으로 mapper가 담당한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MciRequest<T> {

    /** 라우팅, 추적, 채널 통제를 위한 표준 헤더. */
    private MciHeader header;

    /** 업무별 요청 데이터. */
    private T data;
}
