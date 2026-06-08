package com.bwg.channel.backend.common.constants.enums;

public enum GatewayErrorCode {
    SERVER_ERROR("-9999", "서버 내부 오류");

    private final String code;      // 시스템 간 주고받는 코드
    private final String msg;   // 사용자/로그용 설명

    GatewayErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 코드 문자열로 enum을 찾고 싶을 때 유틸 메서드
     */
    public static GatewayErrorCode fromCode(String code) {
        for (GatewayErrorCode e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown GatewayErrorCode: " + code);
    }
}
