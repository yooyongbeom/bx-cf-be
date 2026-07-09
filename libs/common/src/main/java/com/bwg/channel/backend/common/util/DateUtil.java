package com.bwg.channel.backend.common.util;

import com.bwg.channel.backend.common.constants.date.DateFormatType;
import com.bwg.channel.backend.common.constants.date.DateUnit;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

/**
 * 날짜/시간 포맷, 변환, 기간 검색 경계 계산을 위한 공통 유틸리티.
 *
 * <p>DB timestamp 검색은 원본 timestamp 컬럼과 {@link OffsetDateTime} 파라미터로 처리하고,
 * 화면/전문/외부 연동 문자열 변환은 이 유틸리티를 통해 표준 포맷으로 처리한다.</p>
 */
public final class DateUtil {

    /** 프로젝트 기본 업무 시간대. */
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
    /** 프로젝트 기본 업무 offset. */
    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("+09:00");

    private DateUtil() {
    }

    /** 기본 시간대 기준 현재 시각. */
    public static OffsetDateTime now() {
        return OffsetDateTime.now(DEFAULT_ZONE);
    }

    /** 기본 시간대 기준 오늘 날짜. */
    public static LocalDate today() {
        return LocalDate.now(DEFAULT_ZONE);
    }

    /** 기본 시간대 기준 현재 년월. */
    public static YearMonth currentYearMonth() {
        return YearMonth.now(DEFAULT_ZONE);
    }

    /** LocalDate를 지정 포맷 문자열로 변환한다. */
    public static String format(LocalDate value, DateFormatType formatType) {
        return formatter(formatType).format(required(value, "value"));
    }

    /** LocalDateTime을 지정 포맷 문자열로 변환한다. */
    public static String format(LocalDateTime value, DateFormatType formatType) {
        return formatter(formatType).format(required(value, "value"));
    }

    /** OffsetDateTime을 지정 포맷 문자열로 변환한다. */
    public static String format(OffsetDateTime value, DateFormatType formatType) {
        return formatter(formatType).format(required(value, "value"));
    }

    /** YearMonth를 지정 포맷 문자열로 변환한다. */
    public static String format(YearMonth value, DateFormatType formatType) {
        return formatter(formatType).format(required(value, "value"));
    }

    /** LocalTime을 지정 포맷 문자열로 변환한다. */
    public static String format(LocalTime value, DateFormatType formatType) {
        return formatter(formatType).format(required(value, "value"));
    }

    /** 값이 null이면 null, 아니면 LocalDate 포맷 문자열을 반환한다. */
    public static String formatOrNull(LocalDate value, DateFormatType formatType) {
        return value == null ? null : format(value, formatType);
    }

    /** 값이 null이면 null, 아니면 LocalDateTime 포맷 문자열을 반환한다. */
    public static String formatOrNull(LocalDateTime value, DateFormatType formatType) {
        return value == null ? null : format(value, formatType);
    }

    /** 값이 null이면 null, 아니면 OffsetDateTime 포맷 문자열을 반환한다. */
    public static String formatOrNull(OffsetDateTime value, DateFormatType formatType) {
        return value == null ? null : format(value, formatType);
    }

    /** 문자열을 지정 포맷의 LocalDate로 파싱한다. */
    public static LocalDate parseLocalDate(String value, DateFormatType formatType) {
        return LocalDate.parse(requiredText(value, "value"), formatter(formatType));
    }

    /** 문자열이 blank면 null, 아니면 지정 포맷의 LocalDate로 파싱한다. */
    public static LocalDate parseLocalDateOrNull(String value, DateFormatType formatType) {
        return isBlank(value) ? null : parseLocalDate(value, formatType);
    }

    /** 문자열을 지정 포맷의 LocalDateTime으로 파싱한다. */
    public static LocalDateTime parseLocalDateTime(String value, DateFormatType formatType) {
        return LocalDateTime.parse(requiredText(value, "value"), formatter(formatType));
    }

    /** 문자열이 blank면 null, 아니면 지정 포맷의 LocalDateTime으로 파싱한다. */
    public static LocalDateTime parseLocalDateTimeOrNull(String value, DateFormatType formatType) {
        return isBlank(value) ? null : parseLocalDateTime(value, formatType);
    }

    /** offset 포함 ISO 문자열을 OffsetDateTime으로 파싱한다. */
    public static OffsetDateTime parseOffsetDateTime(String value) {
        return parseOffsetDateTime(value, DateFormatType.ISO_OFFSET_DATE_TIME);
    }

    /** 문자열을 지정 포맷의 OffsetDateTime으로 파싱한다. */
    public static OffsetDateTime parseOffsetDateTime(String value, DateFormatType formatType) {
        return OffsetDateTime.parse(requiredText(value, "value"), formatter(formatType));
    }

    /** 문자열이 blank면 null, 아니면 offset 포함 ISO 문자열을 OffsetDateTime으로 파싱한다. */
    public static OffsetDateTime parseOffsetDateTimeOrNull(String value) {
        return isBlank(value) ? null : parseOffsetDateTime(value);
    }

    /** 문자열을 지정 포맷의 YearMonth로 파싱한다. */
    public static YearMonth parseYearMonth(String value, DateFormatType formatType) {
        return YearMonth.parse(requiredText(value, "value"), formatter(formatType));
    }

    /** 문자열을 지정 포맷의 LocalTime으로 파싱한다. */
    public static LocalTime parseLocalTime(String value, DateFormatType formatType) {
        return LocalTime.parse(requiredText(value, "value"), formatter(formatType));
    }

    /** 문자열을 from 포맷으로 파싱한 뒤 to 포맷 문자열로 변환한다. */
    public static String convert(String value, DateFormatType from, DateFormatType to) {
        required(from, "from");
        required(to, "to");
        TemporalAccessor parsed = parseByFormatType(value, from);
        return formatter(to).format(parsed);
    }

    /** 기본 시간대 기준 해당 날짜의 시작 시각. */
    public static OffsetDateTime startOfDay(LocalDate date) {
        return required(date, "date").atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
    }

    /** 기본 시간대 기준 다음 날짜의 시작 시각. 기간 검색의 end exclusive 값으로 사용한다. */
    public static OffsetDateTime startOfNextDay(LocalDate date) {
        return startOfDay(required(date, "date").plusDays(1));
    }

    /** 기본 시간대 기준 해당 월의 시작 시각. */
    public static OffsetDateTime startOfMonth(LocalDate date) {
        return startOfDay(required(date, "date").withDayOfMonth(1));
    }

    /** 기본 시간대 기준 다음 월의 시작 시각. 기간 검색의 end exclusive 값으로 사용한다. */
    public static OffsetDateTime startOfNextMonth(LocalDate date) {
        return startOfMonth(required(date, "date").plusMonths(1));
    }

    /** 기본 시간대 기준 해당 연도의 시작 시각. */
    public static OffsetDateTime startOfYear(LocalDate date) {
        return startOfDay(required(date, "date").withDayOfYear(1));
    }

    /** 기본 시간대 기준 다음 연도의 시작 시각. 기간 검색의 end exclusive 값으로 사용한다. */
    public static OffsetDateTime startOfNextYear(LocalDate date) {
        return startOfYear(required(date, "date").plusYears(1));
    }

    /** LocalDate에 년/월/주/일 단위를 더한다. 시간 단위는 허용하지 않는다. */
    public static LocalDate plus(LocalDate value, long amount, DateUnit unit) {
        requireDateUnit(unit, "LocalDate");
        return switch (unit) {
            case YEARS -> required(value, "value").plusYears(amount);
            case MONTHS -> required(value, "value").plusMonths(amount);
            case WEEKS -> required(value, "value").plusWeeks(amount);
            case DAYS -> required(value, "value").plusDays(amount);
            default -> throw unsupportedUnit(unit, "LocalDate");
        };
    }

    /** LocalDate에서 년/월/주/일 단위를 뺀다. 시간 단위는 허용하지 않는다. */
    public static LocalDate minus(LocalDate value, long amount, DateUnit unit) {
        return plus(value, -amount, unit);
    }

    /** LocalDateTime에 지정 단위를 더한다. */
    public static LocalDateTime plus(LocalDateTime value, long amount, DateUnit unit) {
        required(unit, "unit");
        return switch (unit) {
            case YEARS -> required(value, "value").plusYears(amount);
            case MONTHS -> required(value, "value").plusMonths(amount);
            case WEEKS -> required(value, "value").plusWeeks(amount);
            case DAYS -> required(value, "value").plusDays(amount);
            case HOURS -> required(value, "value").plusHours(amount);
            case MINUTES -> required(value, "value").plusMinutes(amount);
            case SECONDS -> required(value, "value").plusSeconds(amount);
        };
    }

    /** LocalDateTime에서 지정 단위를 뺀다. */
    public static LocalDateTime minus(LocalDateTime value, long amount, DateUnit unit) {
        return plus(value, -amount, unit);
    }

    /** OffsetDateTime에 지정 단위를 더한다. */
    public static OffsetDateTime plus(OffsetDateTime value, long amount, DateUnit unit) {
        required(unit, "unit");
        return switch (unit) {
            case YEARS -> required(value, "value").plusYears(amount);
            case MONTHS -> required(value, "value").plusMonths(amount);
            case WEEKS -> required(value, "value").plusWeeks(amount);
            case DAYS -> required(value, "value").plusDays(amount);
            case HOURS -> required(value, "value").plusHours(amount);
            case MINUTES -> required(value, "value").plusMinutes(amount);
            case SECONDS -> required(value, "value").plusSeconds(amount);
        };
    }

    /** OffsetDateTime에서 지정 단위를 뺀다. */
    public static OffsetDateTime minus(OffsetDateTime value, long amount, DateUnit unit) {
        return plus(value, -amount, unit);
    }

    /** begin/end가 모두 필수인 half-open 기간 검색 범위를 검증한다. */
    public static void validateRequiredRange(OffsetDateTime beginAt, OffsetDateTime endAt, String fieldName) {
        required(beginAt, rangeName(fieldName, "beginAt"));
        required(endAt, rangeName(fieldName, "endAt"));
        validateRange(beginAt, endAt, fieldName);
    }

    /** begin/end가 선택값인 half-open 기간 검색 범위를 검증한다. 둘 중 하나만 있어도 허용한다. */
    public static void validateOptionalRange(OffsetDateTime beginAt, OffsetDateTime endAt, String fieldName) {
        if (beginAt != null && endAt != null) {
            validateRange(beginAt, endAt, fieldName);
        }
    }

    /** beginAt이 endAt보다 앞서는지 검증한다. null 값은 호출부 정책에 맡긴다. */
    public static void validateRange(OffsetDateTime beginAt, OffsetDateTime endAt, String fieldName) {
        if (beginAt == null || endAt == null) {
            return;
        }
        if (!beginAt.isBefore(endAt)) {
            throw new IllegalArgumentException(rangeName(fieldName, "beginAt") + " must be before " + rangeName(fieldName, "endAt"));
        }
    }

    /** target이 [beginAt, endAt) 범위에 포함되는지 확인한다. */
    public static boolean isBetweenHalfOpen(OffsetDateTime target, OffsetDateTime beginAt, OffsetDateTime endAt) {
        required(target, "target");
        return (beginAt == null || !target.isBefore(beginAt))
                && (endAt == null || target.isBefore(endAt));
    }

    /** target이 [beginDate, endDate) 범위에 포함되는지 확인한다. */
    public static boolean isBetweenHalfOpen(LocalDate target, LocalDate beginDate, LocalDate endDate) {
        required(target, "target");
        return (beginDate == null || !target.isBefore(beginDate))
                && (endDate == null || target.isBefore(endDate));
    }

    /** target이 [beginDate, endDate] 범위에 포함되는지 확인한다. */
    public static boolean isBetweenClosed(LocalDate target, LocalDate beginDate, LocalDate endDate) {
        required(target, "target");
        return (beginDate == null || !target.isBefore(beginDate))
                && (endDate == null || !target.isAfter(endDate));
    }

    /** 두 LocalDate 중 더 이른 값을 반환한다. null은 다른 값보다 작은 비교 대상으로 쓰지 않는다. */
    public static LocalDate min(LocalDate first, LocalDate second) {
        if (first == null) return second;
        if (second == null) return first;
        return first.isBefore(second) ? first : second;
    }

    /** 두 LocalDate 중 더 늦은 값을 반환한다. null은 다른 값보다 큰 비교 대상으로 쓰지 않는다. */
    public static LocalDate max(LocalDate first, LocalDate second) {
        if (first == null) return second;
        if (second == null) return first;
        return first.isAfter(second) ? first : second;
    }

    /** 두 OffsetDateTime 중 더 이른 값을 반환한다. null은 다른 값보다 작은 비교 대상으로 쓰지 않는다. */
    public static OffsetDateTime min(OffsetDateTime first, OffsetDateTime second) {
        if (first == null) return second;
        if (second == null) return first;
        return first.isBefore(second) ? first : second;
    }

    /** 두 OffsetDateTime 중 더 늦은 값을 반환한다. null은 다른 값보다 큰 비교 대상으로 쓰지 않는다. */
    public static OffsetDateTime max(OffsetDateTime first, OffsetDateTime second) {
        if (first == null) return second;
        if (second == null) return first;
        return first.isAfter(second) ? first : second;
    }

    /** 두 날짜 사이의 일 수 차이를 계산한다. */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return required(endDate, "endDate").toEpochDay() - required(startDate, "startDate").toEpochDay();
    }

    /** 두 날짜 사이의 월 수 차이를 계산한다. */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        return Period.between(required(startDate, "startDate"), required(endDate, "endDate")).toTotalMonths();
    }

    /** 두 시각 사이의 Duration을 계산한다. */
    public static Duration durationBetween(OffsetDateTime startAt, OffsetDateTime endAt) {
        return Duration.between(required(startAt, "startAt"), required(endAt, "endAt"));
    }

    private static DateTimeFormatter formatter(DateFormatType formatType) {
        return required(formatType, "formatType").formatter();
    }

    private static TemporalAccessor parseByFormatType(String value, DateFormatType formatType) {
        if (formatType.isDateOnly()) {
            return parseLocalDate(value, formatType);
        }
        if (formatType.isDateTime()) {
            return parseLocalDateTime(value, formatType);
        }
        if (formatType.isOffsetDateTime()) {
            return parseOffsetDateTime(value, formatType);
        }
        if (formatType.isYearMonth()) {
            return parseYearMonth(value, formatType);
        }
        if (formatType.isTimeOnly()) {
            return parseLocalTime(value, formatType);
        }
        throw new IllegalArgumentException("Unsupported date format type: " + formatType);
    }

    private static void requireDateUnit(DateUnit unit, String targetType) {
        required(unit, "unit");
        if (unit == DateUnit.HOURS || unit == DateUnit.MINUTES || unit == DateUnit.SECONDS) {
            throw unsupportedUnit(unit, targetType);
        }
    }

    private static IllegalArgumentException unsupportedUnit(DateUnit unit, String targetType) {
        return new IllegalArgumentException(unit + " is not supported for " + targetType);
    }

    private static String requiredText(String value, String name) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static <T> T required(T value, String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String rangeName(String fieldName, String suffix) {
        if (isBlank(fieldName)) {
            return suffix;
        }
        return fieldName + "." + suffix;
    }
}
