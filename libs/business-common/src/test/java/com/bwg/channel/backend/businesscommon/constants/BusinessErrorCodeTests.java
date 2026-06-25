package com.bwg.channel.backend.businesscommon.constants;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 업무공통 에러 코드 조회 규칙 검증
 */
class BusinessErrorCodeTests {

    @Test
    void findsErrorCodeByCodeValue() {
        assertThat(BusinessErrorCode.fromCode("-3001"))
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
    }

    @Test
    void rejectsUnknownCodeValue() {
        assertThatThrownBy(() -> BusinessErrorCode.fromCode("-3998"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown BusinessErrorCode");
    }
}
