package com.bwg.channel.backend.gateway.constants;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;

public enum GatewayErrorCode implements BwgErrorCode {

    SERVER_ERROR("-9999", "게이트웨이 서버 내부 오류");

    private final String code;
    private final String msg;

    GatewayErrorCode(String code, String msg) {
        this.code = code;
        this.msg  = msg;
    }

    @Override public String getCode() { return code; }
    @Override public String getMsg()  { return msg;  }

    public static GatewayErrorCode fromCode(String code) {
        for (GatewayErrorCode e : values()) {
            if (e.code.equals(code)) return e;
        }
        throw new IllegalArgumentException("Unknown GatewayErrorCode: " + code);
    }
}
