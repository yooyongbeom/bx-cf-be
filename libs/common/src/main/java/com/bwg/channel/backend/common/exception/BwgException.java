package com.bwg.channel.backend.common.exception;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import com.bwg.channel.backend.common.constants.error.CommonErrorCode;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * 업무 오류 코드와 부가 정보를 함께 전달하는 공통 런타임 Exception
 * <p>
 * 서비스별 예외는 {@link Builder}를 상속하거나 {@link #of(String, String, Throwable)}를 사용해
 * {@link BwgErrorCode} 계약을 유지한 채 advice에서 동일한 응답 형식으로 변환된다.
 */
public class BwgException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 일반 예외를 업무 예외로 감쌀 때 사용하는 기본 서버 오류 코드/메시지. */
    public static final String FALLBACK_CODE = CommonErrorCode.SERVER_ERROR.getCode();
    public static final String FALLBACK_MSG = CommonErrorCode.SERVER_ERROR.getMsg();

    private final BwgErrorCode code;
    private final Map<String, Object> details;
    private final Instant occurredAt;

    protected BwgException(Builder<?> builder) {
        super(builder.message, builder.cause);
        this.code = builder.code;
        this.details = builder.details == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(builder.details);
        // 발생 시각을 외부에서 주입하지 않으면 예외 생성 시점으로 기록한다.
        this.occurredAt = builder.occurredAt == null ? Instant.now() : builder.occurredAt;
    }

    public BwgErrorCode getCode() {
        return code;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public static BwgException of(String code, String msg, Throwable cause) {
        return new BwgException(new SimpleBuilder(code, msg, cause)) {
        };
    }

    /**
     * enum으로 정의되지 않은 fallback 오류를 임시 {@link BwgErrorCode}로 감싸기 위한 builder.
     */
    private static final class SimpleBuilder extends Builder<SimpleBuilder> {
        SimpleBuilder(String code, String msg, Throwable cause) {
            this.message(msg).cause(cause).code(new BwgErrorCode() {
                @Override
                public String getCode() {
                    return code;
                }

                @Override
                public String getMsg() {
                    return msg;
                }

                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.INTERNAL_SERVER_ERROR;
                }
            });
        }

        @Override
        public BwgException build() {
            return new BwgException(this) {
            };
        }
    }

    /**
     * 서비스별 커스텀 예외가 공통 필드(message/cause/code/details/occurredAt)를 재사용하기 위한 기본 builder.
     */
    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<B>> {
        private String message;
        private Throwable cause;
        private BwgErrorCode code;
        private Map<String, Object> details;
        private Instant occurredAt;

        public B message(String message) {
            this.message = message;
            return (B) this;
        }

        public B cause(Throwable cause) {
            this.cause = cause;
            return (B) this;
        }

        public B code(BwgErrorCode code) {
            this.code = code;
            return (B) this;
        }

        public B details(Map<String, Object> details) {
            this.details = details;
            return (B) this;
        }

        public B occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return (B) this;
        }

        public abstract BwgException build();
    }
}
