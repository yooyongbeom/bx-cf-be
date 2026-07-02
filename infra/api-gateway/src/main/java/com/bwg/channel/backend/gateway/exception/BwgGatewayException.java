package com.bwg.channel.backend.gateway.exception;

import com.bwg.channel.backend.common.exception.BwgException;
import com.bwg.channel.backend.gateway.constants.GatewayErrorCode;

public class BwgGatewayException extends BwgException {
    private static final long serialVersionUID = 1L;

    private BwgGatewayException(Builder builder) {
        super(builder);
    }

    @Override
    public GatewayErrorCode getCode() {
        return (GatewayErrorCode) super.getCode();
    }

    public static class Builder extends BwgException.Builder<Builder> {

        public Builder code(GatewayErrorCode code) {
            super.code(code);
            return this;
        }

        @Override
        public BwgGatewayException build() {
            return new BwgGatewayException(this);
        }
    }
}
