package com.bwg.channel.backend.businesscommon.domain.vo;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 업무 날짜 범위 값 객체의 생성 규칙과 포함 여부 검증
 */
class BusinessDateRangeTests {

    @Test
    void createsClosedDateRangeAndChecksContainment() {
        // 시작일과 종료일을 모두 포함하는 닫힌 범위
        BusinessDateRange range = BusinessDateRange.of(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        assertThat(range.contains(LocalDate.of(2026, 1, 1))).isTrue();
        assertThat(range.contains(LocalDate.of(2026, 1, 31))).isTrue();
        assertThat(range.contains(LocalDate.of(2026, 2, 1))).isFalse();
    }

    @Test
    void supportsOpenEndedDateRange() {
        // 종료일이 없는 열린 범위
        BusinessDateRange range = BusinessDateRange.from(LocalDate.of(2026, 1, 1));

        assertThat(range.isOpenEnded()).isTrue();
        assertThat(range.contains(LocalDate.of(2027, 1, 1))).isTrue();
    }

    @Test
    void rejectsStartDateAfterEndDate() {
        assertThatThrownBy(() -> BusinessDateRange.of(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 31)
        ))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.INVALID_DATE_RANGE);
    }
}
