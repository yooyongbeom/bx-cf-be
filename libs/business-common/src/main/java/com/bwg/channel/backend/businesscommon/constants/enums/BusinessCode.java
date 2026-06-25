package com.bwg.channel.backend.businesscommon.constants.enums;

/**
 * DB 저장값이나 API 통신값으로 코드를 사용하는 업무 enum 공통 규약
 */
public interface BusinessCode {

    String getCode();

    String getDescription();

    default boolean matches(String code) {
        return getCode().equals(code);
    }
}
