package com.bwg.channel.backend.businesscommon.constants;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 업무 공통 모듈에서 함께 사용하는 에러 코드.
 *
 * <p>business-common은 -3xxx 대역을 사용하고, 서비스별 도메인은 별도 코드 대역을 사용한다.</p>
 */
public enum BusinessErrorCode implements BwgErrorCode {

    REQUIRED_VALUE_MISSING("-3001", "Required value is missing", HttpStatus.BAD_REQUEST),
    INVALID_BUSINESS_CODE("-3002", "Invalid business code", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("-3003", "Start date cannot be after end date", HttpStatus.BAD_REQUEST),
    INVALID_YN_VALUE("-3004", "Value must be Y or N", HttpStatus.BAD_REQUEST),

    BUSINESS_RULE_VIOLATION("-3101", "Business rule violation", HttpStatus.BAD_REQUEST),
    BUSINESS_DATA_NOT_FOUND("-3201", "Business data not found", HttpStatus.NOT_FOUND),

    SERVER_ERROR("-3999", "Business common server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    BusinessErrorCode(String code, String msg, HttpStatus status) {
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

    public static BusinessErrorCode fromCode(String code) {
        for (BusinessErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown BusinessErrorCode: " + code);
    }
}
