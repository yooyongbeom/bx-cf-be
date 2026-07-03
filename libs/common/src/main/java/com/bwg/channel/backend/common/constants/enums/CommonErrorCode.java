package com.bwg.channel.backend.common.constants.enums;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements BwgErrorCode {

    REQUIRED_VALUE_MISSING("-2001", "Required value is missing", HttpStatus.BAD_REQUEST),
    JSON_STR_TO_VO_PARSING_ERROR("-2003", "Json String to VO Parsing Error", HttpStatus.BAD_REQUEST),
    JSON_VO_TO_STR_PARSING_ERROR("-2004", "Json VO to String Parsing Error", HttpStatus.BAD_REQUEST),

    DB_NO_DATA_ERROR("-4001", "No data found", HttpStatus.NOT_FOUND),
    DB_SAVE_DATA_ERROR("-4002", "Unable to save data to the database", HttpStatus.INTERNAL_SERVER_ERROR),

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
