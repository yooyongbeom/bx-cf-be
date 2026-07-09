package com.bwg.channel.backend.common.util;

import com.bwg.channel.backend.common.constants.date.DateFormatType;
import com.bwg.channel.backend.common.constants.date.DateUnit;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateUtilTests {

    @Test
    void convertsBetweenCommonDateFormats() {
        assertThat(DateUtil.convert("20260625", DateFormatType.DATE_COMPACT, DateFormatType.DATE))
                .isEqualTo("2026-06-25");
        assertThat(DateUtil.convert("20260625090000", DateFormatType.DATE_TIME_COMPACT, DateFormatType.DATE_TIME))
                .isEqualTo("2026-06-25 09:00:00");
    }

    @Test
    void parsesAndFormatsOffsetDateTime() {
        OffsetDateTime dateTime = DateUtil.parseOffsetDateTime("2026-06-25T09:00:00+09:00");

        assertThat(dateTime).isEqualTo(OffsetDateTime.parse("2026-06-25T09:00:00+09:00"));
        assertThat(DateUtil.format(dateTime, DateFormatType.ISO_OFFSET_DATE_TIME))
                .isEqualTo("2026-06-25T09:00:00+09:00");
    }

    @Test
    void createsDefaultZoneDayAndMonthBoundaries() {
        LocalDate date = LocalDate.of(2026, 6, 25);

        assertThat(DateUtil.startOfDay(date)).isEqualTo(OffsetDateTime.parse("2026-06-25T00:00:00+09:00"));
        assertThat(DateUtil.startOfNextDay(date)).isEqualTo(OffsetDateTime.parse("2026-06-26T00:00:00+09:00"));
        assertThat(DateUtil.startOfMonth(date)).isEqualTo(OffsetDateTime.parse("2026-06-01T00:00:00+09:00"));
        assertThat(DateUtil.startOfNextMonth(date)).isEqualTo(OffsetDateTime.parse("2026-07-01T00:00:00+09:00"));
    }

    @Test
    void addsAndSubtractsDateUnits() {
        assertThat(DateUtil.plus(LocalDate.of(2026, 1, 31), 1, DateUnit.MONTHS))
                .isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(DateUtil.minus(LocalDateTime.of(2026, 6, 25, 9, 0), 2, DateUnit.HOURS))
                .isEqualTo(LocalDateTime.of(2026, 6, 25, 7, 0));
    }

    @Test
    void rejectsTimeUnitsForLocalDate() {
        assertThatThrownBy(() -> DateUtil.plus(LocalDate.of(2026, 6, 25), 1, DateUnit.HOURS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LocalDate");
    }

    @Test
    void validatesAndChecksHalfOpenRanges() {
        OffsetDateTime beginAt = OffsetDateTime.parse("2026-06-25T00:00:00+09:00");
        OffsetDateTime endAt = OffsetDateTime.parse("2026-06-26T00:00:00+09:00");

        DateUtil.validateRequiredRange(beginAt, endAt, "createdAt");

        assertThat(DateUtil.isBetweenHalfOpen(beginAt, beginAt, endAt)).isTrue();
        assertThat(DateUtil.isBetweenHalfOpen(endAt.minusNanos(1), beginAt, endAt)).isTrue();
        assertThat(DateUtil.isBetweenHalfOpen(endAt, beginAt, endAt)).isFalse();
        assertThatThrownBy(() -> DateUtil.validateRequiredRange(endAt, beginAt, "createdAt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("createdAt");
    }

    @Test
    void calculatesDifferencesAndMinMaxValues() {
        LocalDate first = LocalDate.of(2026, 6, 1);
        LocalDate second = LocalDate.of(2026, 6, 25);
        OffsetDateTime start = OffsetDateTime.parse("2026-06-25T00:00:00+09:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-25T01:30:00+09:00");

        assertThat(DateUtil.daysBetween(first, second)).isEqualTo(24);
        assertThat(DateUtil.durationBetween(start, end).toMinutes()).isEqualTo(90);
        assertThat(DateUtil.min(first, second)).isEqualTo(first);
        assertThat(DateUtil.max(start, end)).isEqualTo(end);
    }
}
