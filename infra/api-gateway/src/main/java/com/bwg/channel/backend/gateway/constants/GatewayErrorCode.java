package com.bwg.channel.backend.gateway.constants;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import org.springframework.http.HttpStatus;

public enum GatewayErrorCode implements BwgErrorCode {

    SERVER_ERROR("-9999", "게이트웨이 서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    GatewayErrorCode(String code, String msg, HttpStatus status) {
        this.code = code;
        this.msg = msg;
        this.status = status;
    }

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
        return status;
    }

    public static GatewayErrorCode fromCode(String code) {
        for (GatewayErrorCode e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown GatewayErrorCode: " + code);
    }
}
