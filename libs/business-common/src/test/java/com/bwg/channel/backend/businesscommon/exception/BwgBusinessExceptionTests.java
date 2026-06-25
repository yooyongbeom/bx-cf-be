package com.bwg.channel.backend.businesscommon.exception;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 업무공통 예외의 타입 지정 에러 코드와 상세 정보 유지 검증
 */
class BwgBusinessExceptionTests {

    @Test
    void keepsTypedBusinessErrorCodeAndDetails() {
        BwgBusinessException exception = new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message("rule failed")
                .details(Map.of("field", "useYn"))
                .build();

        assertThat(exception.getCode()).isEqualTo(BusinessErrorCode.BUSINESS_RULE_VIOLATION);
        assertThat(exception.getDetails()).containsEntry("field", "useYn");
    }
}
