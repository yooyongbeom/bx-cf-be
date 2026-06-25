package com.bwg.channel.backend.businesscommon.constants.enums;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Y/N 업무 코드 변환과 실패 규칙 검증
 */
class UseYnTests {

    @Test
    void parsesYnCodeIgnoringCaseAndWhitespace() {
        assertThat(UseYn.fromCode(" y ")).isEqualTo(UseYn.Y);
        assertThat(UseYn.fromCode("n")).isEqualTo(UseYn.N);
    }

    @Test
    void exposesBooleanSemantics() {
        assertThat(UseYn.Y.isYes()).isTrue();
        assertThat(UseYn.N.isNo()).isTrue();
    }

    @Test
    void rejectsInvalidYnCodeWithBusinessErrorCode() {
        assertThatThrownBy(() -> UseYn.fromCode("A"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.INVALID_YN_VALUE);
    }
}
