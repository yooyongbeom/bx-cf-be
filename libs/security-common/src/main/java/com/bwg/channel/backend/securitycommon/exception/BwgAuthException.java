package com.bwg.channel.backend.securitycommon.exception;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import com.bwg.channel.backend.common.exception.BwgException;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;

public class BwgAuthException extends BwgException {
    private static final long serialVersionUID = 1L;

    private BwgAuthException(Builder builder) {
        super(builder);
    }

    @Override
    public BwgErrorCode getCode() {
        return super.getCode();
    }

    public static class Builder extends BwgException.Builder<Builder> {

        public Builder code(AuthErrorCode code) {
            super.code(code);
            return this;
        }

        public Builder code(BwgErrorCode code) {
            super.code(code);
            return this;
        }

        @Override
        public BwgAuthException build() {
            return new BwgAuthException(this);
        }
    }
}
