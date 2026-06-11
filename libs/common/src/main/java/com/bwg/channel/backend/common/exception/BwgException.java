package com.bwg.channel.backend.common.exception;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class BwgException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public static final String FALLBACK_CODE = "-9999";
    public static final String FALLBACK_MSG  = "서버 내부 오류";

    private final BwgErrorCode code;
    private final Map<String, Object> details;
    private final Instant occurredAt;

    protected BwgException(Builder<?> builder) {
        super(builder.message, builder.cause);
        this.code       = builder.code;
        this.details    = builder.details == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(builder.details);
        this.occurredAt = builder.occurredAt == null ? Instant.now() : builder.occurredAt;
    }

    public BwgErrorCode getCode()              { return code; }
    public Map<String, Object> getDetails()    { return details; }
    public Instant getOccurredAt()             { return occurredAt; }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<B>> {
        private String message;
        private Throwable cause;
        private BwgErrorCode code;
        private Map<String, Object> details;
        private Instant occurredAt;

        public B message(String message)              { this.message    = message;    return (B) this; }
        public B cause(Throwable cause)               { this.cause      = cause;      return (B) this; }
        public B code(BwgErrorCode code)              { this.code       = code;       return (B) this; }
        public B details(Map<String, Object> details) { this.details    = details;    return (B) this; }
        public B occurredAt(Instant occurredAt)       { this.occurredAt = occurredAt; return (B) this; }

        public abstract BwgException build();
    }
}
