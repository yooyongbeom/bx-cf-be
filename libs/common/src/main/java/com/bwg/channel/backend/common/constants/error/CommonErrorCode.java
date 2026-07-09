package com.bwg.channel.backend.common.constants.error;

import org.springframework.http.HttpStatus;

/**
 * 모든 서비스에서 공통으로 사용하는 기본 오류 코드.
 */
public enum CommonErrorCode implements BwgErrorCode {

    // Request validation / binding errors
    REQUIRED_VALUE_MISSING("-2001", "Required value is missing", HttpStatus.BAD_REQUEST),
    JSON_STR_TO_VO_PARSING_ERROR("-2003", "Json String to VO Parsing Error", HttpStatus.BAD_REQUEST),
    JSON_VO_TO_STR_PARSING_ERROR("-2004", "Json VO to String Parsing Error", HttpStatus.BAD_REQUEST),

    // Database errors
    DB_NO_DATA_ERROR("-4001", "No data found", HttpStatus.NOT_FOUND),
    DB_SAVE_DATA_ERROR("-4002", "Unable to save data to the database", HttpStatus.INTERNAL_SERVER_ERROR),
    DB_ACCESS_ERROR("-4003", "Database access error", HttpStatus.INTERNAL_SERVER_ERROR),
    DB_TIMEOUT_ERROR("-4004", "Database query timeout", HttpStatus.GATEWAY_TIMEOUT),

    // System errors
    TIMEOUT_ERROR("-9001", "Request processing timeout", HttpStatus.GATEWAY_TIMEOUT),
    SERVICE_UNAVAILABLE("-9002", "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    SERVER_ERROR("-9999", "Server internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    CommonErrorCode(String code, String msg, HttpStatus status) {
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
