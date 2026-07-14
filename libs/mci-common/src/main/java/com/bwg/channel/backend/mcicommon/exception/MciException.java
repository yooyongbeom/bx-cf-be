package com.bwg.channel.backend.mcicommon.exception;

import com.bwg.channel.backend.common.exception.BwgException;
import com.bwg.channel.backend.mcicommon.constants.MciErrorCode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MCI 라우팅/변환/adapter 선택 과정에서 발생하는 공통 예외.
 */
public class MciException extends BwgException {
    private static final long serialVersionUID = 1L;

    private MciException(Builder builder) {
        super(builder);
    }

    public static MciException of(MciErrorCode code) {
        return of(code, Collections.emptyMap());
    }

    public static MciException of(MciErrorCode code, Map<String, ?> details) {
        MciErrorCode requiredCode = Objects.requireNonNull(code, "code must not be null");
        Map<String, Object> copiedDetails = new LinkedHashMap<>();
        Objects.requireNonNull(details, "details must not be null")
                .forEach(copiedDetails::put);

        return new Builder()
                .code(requiredCode)
                .message(requiredCode.getMsg())
                .details(copiedDetails)
                .build();
    }

    @Override
    public MciErrorCode getCode() {
        return (MciErrorCode) super.getCode();
    }

    public static class Builder extends BwgException.Builder<Builder> {

        public Builder code(MciErrorCode code) {
            super.code(code);
            return this;
        }

        @Override
        public MciException build() {
            return new MciException(this);
        }
    }
}
