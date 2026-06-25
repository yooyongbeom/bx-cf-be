package com.bwg.channel.backend.businesscommon.exception;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.common.exception.BwgException;

/**
 * 업무공통 규칙과 값 객체에서 사용하는 기본 예외
 */
public class BwgBusinessException extends BwgException {
    private static final long serialVersionUID = 1L;

    private BwgBusinessException(Builder builder) {
        super(builder);
    }

    @Override
    public BusinessErrorCode getCode() {
        return (BusinessErrorCode) super.getCode();
    }

    public static class Builder extends BwgException.Builder<Builder> {

        public Builder code(BusinessErrorCode code) {
            super.code(code);
            return this;
        }

        @Override
        public BwgBusinessException build() {
            return new BwgBusinessException(this);
        }
    }
}
