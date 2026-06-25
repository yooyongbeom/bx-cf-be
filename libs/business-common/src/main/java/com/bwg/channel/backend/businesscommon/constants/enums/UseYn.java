package com.bwg.channel.backend.businesscommon.constants.enums;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;

import java.util.Locale;
import java.util.Map;

/**
 * 업무 API와 영속성 모델에서 공통으로 사용하는 Y/N 플래그
 */
public enum UseYn implements BusinessCode {

    Y("Y", "Yes"),
    N("N", "No");

    private final String code;
    private final String description;

    UseYn(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public boolean isYes() {
        return this == Y;
    }

    public boolean isNo() {
        return this == N;
    }

    public static UseYn fromCode(String code) {
        if (code != null) {
            // 쿼리 파라미터 등 외부 입력의 소문자/앞뒤 공백 보정
            String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
            for (UseYn useYn : values()) {
                if (useYn.code.equals(normalizedCode)) {
                    return useYn;
                }
            }
        }

        throw new BwgBusinessException.Builder()
                .code(BusinessErrorCode.INVALID_YN_VALUE)
                .message(BusinessErrorCode.INVALID_YN_VALUE.getMsg())
                .details(Map.of("value", String.valueOf(code)))
                .build();
    }
}
