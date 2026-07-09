package com.bwg.channel.backend.common.constants.date;

/**
 * DateUtil 날짜/시간 증감 단위.
 *
 * <p>날짜 연산 호출부에서 문자열이나 ChronoUnit을 직접 쓰지 않고,
 * 프로젝트에서 허용하는 단위만 명시적으로 선택하도록 한다.</p>
 */
public enum DateUnit {
    /** 년 단위 증감. */
    YEARS,
    /** 월 단위 증감. */
    MONTHS,
    /** 주 단위 증감. */
    WEEKS,
    /** 일 단위 증감. */
    DAYS,
    /** 시간 단위 증감. LocalDate에는 사용할 수 없다. */
    HOURS,
    /** 분 단위 증감. LocalDate에는 사용할 수 없다. */
    MINUTES,
    /** 초 단위 증감. LocalDate에는 사용할 수 없다. */
    SECONDS
}
