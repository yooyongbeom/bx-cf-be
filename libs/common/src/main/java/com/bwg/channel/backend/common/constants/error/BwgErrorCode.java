package com.bwg.channel.backend.common.constants.error;

import org.springframework.http.HttpStatus;

/**
 * 서비스별 오류 enum이 맞춰야 하는 공통
 * <p>
 * 예외 처리는 이 인터페이스만 보고 업무 코드, 메시지, HTTP 상태를 응답으로 변환한다.
 */
public interface BwgErrorCode {
    /** 프론트/연동 시스템이 분기할 업무 오류 코드. */
    String getCode();

    /** 사용자 또는 로그에 노출할 기본 오류 메시지. */
    String getMsg();

    /** 해당 오류를 HTTP 응답으로 변환할 때 사용할 상태 코드. */
    HttpStatus getStatus();
}
