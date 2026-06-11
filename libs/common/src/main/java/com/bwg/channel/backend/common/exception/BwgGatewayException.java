package com.bwg.channel.backend.common.exception;


import com.bwg.channel.backend.common.constants.enums.GatewayErrorCode;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Gateway 관련 비즈니스 로직에서 발생하는 예외
 * - code : 사전에 정의된 에러 코드
 * - details : 추가 디버깅 정보를 담을 수 있는 키/값
 * - occurredAt : 발생 시각
*/
public class BwgGatewayException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 사전에 정의된 에러 코드 */
    private final GatewayErrorCode code;

    /** 추가 디버깅/로그를 위한 정보 */
    private final Map<String, Object> details;

    /** 예외 발생 시각 (UTC) */
    private final Instant occurredAt;

    // 생성자는 private, 빌더만 사용하도록 제한
    private BwgGatewayException(Builder builder) {
        super(builder.message, builder.cause);
        this.code = builder.code;
        this.details = builder.details == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(builder.details);
        this.occurredAt = builder.occurredAt == null
                ? Instant.now()
                : builder.occurredAt;
    }

    public GatewayErrorCode getCode() {
        return code;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    // 빌더 패턴
    public static class Builder {
        private String message;
        private Throwable cause;
        private GatewayErrorCode code;
        private Map<String, Object> details;
        private Instant occurredAt;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder code(GatewayErrorCode code) {
            this.code = code;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public BwgGatewayException build() {
            return new BwgGatewayException(this);
        }
    }
}

