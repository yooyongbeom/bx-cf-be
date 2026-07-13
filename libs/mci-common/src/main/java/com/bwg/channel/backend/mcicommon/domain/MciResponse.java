package com.bwg.channel.backend.mcicommon.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MCI 표준 응답 모델.
 * <p>
 * adapter가 고객사 시스템 응답을 받아오면 mapper가 이 응답으로 다시 정규화한다. 채널은 고객사 시스템 종류를 몰라도
 * 동일한 응답 규격으로 처리할 수 있다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MciResponse<T> {

    /** 요청 헤더를 이어받은 표준 응답 헤더. */
    private MciHeader header;

    /** 성공/실패와 표준 결과 코드를 담는 결과 영역. */
    private MciResult result;

    /** 업무별 응답 데이터. */
    private T data;
}
