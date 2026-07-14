package com.bwg.channel.backend.mcicommon.constants;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import org.springframework.http.HttpStatus;

/**
 * MCI 요청 검증과 거래 라우팅 과정에서 사용하는 오류 코드.
 */
public enum MciErrorCode implements BwgErrorCode {

    REQUEST_HEADER_REQUIRED("-6001", "MCI request header is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_CODE_REQUIRED("-6002", "MCI transaction code is required", HttpStatus.BAD_REQUEST),

    TRANSACTION_NOT_REGISTERED("-6101", "MCI transaction is not registered", HttpStatus.NOT_FOUND),
    TRANSACTION_DISABLED("-6102", "MCI transaction is disabled", HttpStatus.SERVICE_UNAVAILABLE),
    CHANNEL_NOT_ALLOWED("-6103", "MCI channel is not allowed", HttpStatus.FORBIDDEN),

    ADAPTER_NOT_REGISTERED("-6201", "MCI adapter configuration is invalid", HttpStatus.INTERNAL_SERVER_ERROR),
    MAPPER_NOT_REGISTERED("-6202", "MCI mapper configuration is invalid", HttpStatus.INTERNAL_SERVER_ERROR),

    SERVER_ERROR("-6999", "MCI server internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    MciErrorCode(String code, String msg, HttpStatus status) {
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

    public static MciErrorCode fromCode(String code) {
        for (MciErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown MciErrorCode: " + code);
    }
}
