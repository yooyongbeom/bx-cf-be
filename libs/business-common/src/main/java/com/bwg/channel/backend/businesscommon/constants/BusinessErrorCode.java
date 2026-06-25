package com.bwg.channel.backend.businesscommon.constants;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;

/**
 * 업무 도메인 모듈에서 함께 사용하는 공통 에러 코드
 *
 * <p>business-common은 -3xxx 대역을 사용하고, 서비스별 도메인은 별도 코드 대역 사용</p>
 */
public enum BusinessErrorCode implements BwgErrorCode {

    REQUIRED_VALUE_MISSING("-3001", "Required value is missing"),
    INVALID_BUSINESS_CODE("-3002", "Invalid business code"),
    INVALID_DATE_RANGE("-3003", "Start date cannot be after end date"),
    INVALID_YN_VALUE("-3004", "Value must be Y or N"),

    BUSINESS_RULE_VIOLATION("-3101", "Business rule violation"),
    BUSINESS_DATA_NOT_FOUND("-3201", "Business data not found"),

    SERVER_ERROR("-3999", "Business common server error");

    private final String code;
    private final String msg;

    BusinessErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
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
