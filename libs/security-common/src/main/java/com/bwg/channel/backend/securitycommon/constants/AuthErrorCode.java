package com.bwg.channel.backend.securitycommon.constants;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;

public enum AuthErrorCode implements BwgErrorCode {

    // 인증/인가 (-1xxx)
    REQUIRED_VALUE_MISSING("-1001", "필수값이 없습니다"),
    INVALID_TOKEN         ("-1002", "유효하지 않은 토큰"),
    UNAUTHORIZED_CLIENT   ("-1003", "인증되지 않은 클라이언트"),
    EXPIRED_TOKEN         ("-1004", "토큰이 만료되었습니다"),
    ACCESS_DENIED         ("-1005", "접근이 거부되었습니다"),

    // 파싱 (-2xxx)
    JSON_STR_TO_VO_PARSING_ERROR("-2003", "Json String to VO Parsing Error"),
    JSON_VO_TO_STR_PARSING_ERROR("-2004", "Json VO to String Parsing Error"),

    // DB (-4xxx)
    DB_NO_DATA_ERROR  ("-4001", "No data found"),
    DB_SAVE_DATA_ERROR("-4002", "Unable to save data to the database"),

    // 서버 오류 (-9999)
    SERVER_ERROR("-9999", "서버 내부 오류");

    private final String code;
    private final String msg;

    AuthErrorCode(String code, String msg) {
        this.code = code;
        this.msg  = msg;
    }

    @Override public String getCode() { return code; }
    @Override public String getMsg()  { return msg;  }

    public static AuthErrorCode fromCode(String code) {
        for (AuthErrorCode e : values()) {
            if (e.code.equals(code)) return e;
        }
        throw new IllegalArgumentException("Unknown AuthErrorCode: " + code);
    }
}
