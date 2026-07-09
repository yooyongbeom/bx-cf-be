package com.bwg.channel.backend.common.constants.date;

import java.time.format.DateTimeFormatter;

/**
 * 프로젝트에서 공통으로 사용하는 날짜/시간 문자열 포맷.
 *
 * <p>화면, 전문, 외부 연동에서 반복되는 포맷을 enum으로 고정하여
 * 임의 문자열 패턴이 서비스 코드에 흩어지지 않도록 한다.</p>
 */
public enum DateFormatType {

    /** 하이픈으로 구분된 일반 날짜. 예: 2026-06-25 */
    DATE("yyyy-MM-dd"),
    /** 구분자 없는 날짜. 예: 20260625 */
    DATE_COMPACT("yyyyMMdd"),
    /** 슬래시로 구분된 날짜. 예: 2026/06/25 */
    DATE_SLASH("yyyy/MM/dd"),
    /** 점으로 구분된 날짜. 예: 2026.06.25 */
    DATE_DOT("yyyy.MM.dd"),

    /** 일반 날짜/시간. 예: 2026-06-25 09:00:00 */
    DATE_TIME("yyyy-MM-dd HH:mm:ss"),
    /** 구분자 없는 날짜/시간. 예: 20260625090000 */
    DATE_TIME_COMPACT("yyyyMMddHHmmss"),
    /** 날짜는 compact, 시간은 구분자를 사용하는 날짜/시간. 예: 20260625 09:00:00 */
    DATE_TIME_COMPACT_DATE("yyyyMMdd HH:mm:ss"),
    /** offset 없는 ISO local date-time. 예: 2026-06-25T09:00:00 */
    ISO_LOCAL_DATE_TIME("yyyy-MM-dd'T'HH:mm:ss"),
    /** offset 포함 ISO date-time. 예: 2026-06-25T09:00:00+09:00 */
    ISO_OFFSET_DATE_TIME("yyyy-MM-dd'T'HH:mm:ssXXX"),

    /** 하이픈으로 구분된 년월. 예: 2026-06 */
    YEAR_MONTH("yyyy-MM"),
    /** 구분자 없는 년월. 예: 202606 */
    YEAR_MONTH_COMPACT("yyyyMM"),

    /** 일반 시간. 예: 09:00:00 */
    TIME("HH:mm:ss"),
    /** 구분자 없는 시간. 예: 090000 */
    TIME_COMPACT("HHmmss");

    private final String pattern;
    private final DateTimeFormatter formatter;

    DateFormatType(String pattern) {
        this.pattern = pattern;
        this.formatter = DateTimeFormatter.ofPattern(pattern);
    }

    /** DateTimeFormatter 패턴 문자열. */
    public String getPattern() {
        return pattern;
    }

    /** enum 값에 대응하는 thread-safe formatter. */
    public DateTimeFormatter formatter() {
        return formatter;
    }

    /** 날짜만 표현하는 포맷 여부. */
    public boolean isDateOnly() {
        return this == DATE || this == DATE_COMPACT || this == DATE_SLASH || this == DATE_DOT;
    }

    /** offset 없는 날짜/시간 포맷 여부. */
    public boolean isDateTime() {
        return this == DATE_TIME
                || this == DATE_TIME_COMPACT
                || this == DATE_TIME_COMPACT_DATE
                || this == ISO_LOCAL_DATE_TIME;
    }

    /** offset 포함 날짜/시간 포맷 여부. */
    public boolean isOffsetDateTime() {
        return this == ISO_OFFSET_DATE_TIME;
    }

    /** 년월 포맷 여부. */
    public boolean isYearMonth() {
        return this == YEAR_MONTH || this == YEAR_MONTH_COMPACT;
    }

    /** 시간만 표현하는 포맷 여부. */
    public boolean isTimeOnly() {
        return this == TIME || this == TIME_COMPACT;
    }
}
