package com.bwg.channel.backend.common.constants.error;

import org.springframework.http.HttpStatus;

public enum BusinessTestErrorCode implements BwgErrorCode {
    NOT_FOUND("-3998", "not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    BusinessTestErrorCode(String code, String msg, HttpStatus status) {
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
}
