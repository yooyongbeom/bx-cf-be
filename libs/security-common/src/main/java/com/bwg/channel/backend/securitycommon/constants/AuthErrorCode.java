package com.bwg.channel.backend.securitycommon.constants;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements BwgErrorCode {

    REQUIRED_VALUE_MISSING("-1001", "필수값이 없습니다", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("-1002", "유효하지 않은 토큰", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_CLIENT("-1003", "인증되지 않은 클라이언트", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("-1004", "토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("-1005", "접근이 거부되었습니다", HttpStatus.FORBIDDEN);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    AuthErrorCode(String code, String msg, HttpStatus status) {
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

    public static AuthErrorCode fromCode(String code) {
        for (AuthErrorCode e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown AuthErrorCode: " + code);
    }
}
