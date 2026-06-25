package com.bwg.channel.backend.businesscommon.validation;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.constants.enums.UseYn;
import com.bwg.channel.backend.businesscommon.domain.vo.BusinessDateRange;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;

import java.time.LocalDate;
import java.util.Map;

/**
 * 여러 서비스에서 공유할 수 있는 업무 입력값 검증 메서드
 *
 * <p>서비스 고유 규칙은 각 서비스에 두고, 여기에는 필수값, Y/N 플래그,
 * 날짜 범위처럼 서비스 간에 반복되는 원시 검증만 배치</p>
 */
public final class BusinessValidator {

    private BusinessValidator() {
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw requiredValueMissing(fieldName);
        }
        return value;
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw requiredValueMissing(fieldName);
        }
        return value.trim();
    }

    public static UseYn requireUseYn(String value, String fieldName) {
        requireNonBlank(value, fieldName);
        try {
            return UseYn.fromCode(value);
        } catch (BwgBusinessException e) {
            // 호출자가 실패 필드를 알 수 있도록 필드 정보 보강
            throw new BwgBusinessException.Builder()
                    .code(BusinessErrorCode.INVALID_YN_VALUE)
                    .message(BusinessErrorCode.INVALID_YN_VALUE.getMsg())
                    .details(Map.of("field", normalizeFieldName(fieldName), "value", value))
                    .build();
        }
    }

    public static BusinessDateRange requireDateRange(LocalDate startDate, LocalDate endDate, String fieldName) {
        if (startDate == null) {
            throw requiredValueMissing(fieldName + ".startDate");
        }
        try {
            return BusinessDateRange.of(startDate, endDate);
        } catch (BwgBusinessException e) {
            // 범위 규칙은 값 객체에 맡기고, 이 계층에서는 필드 문맥 보강
            throw new BwgBusinessException.Builder()
                    .code(BusinessErrorCode.INVALID_DATE_RANGE)
                    .message(BusinessErrorCode.INVALID_DATE_RANGE.getMsg())
                    .details(Map.of("field", normalizeFieldName(fieldName), "startDate", startDate, "endDate", endDate))
                    .build();
        }
    }

    private static BwgBusinessException requiredValueMissing(String fieldName) {
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.REQUIRED_VALUE_MISSING)
                .message(BusinessErrorCode.REQUIRED_VALUE_MISSING.getMsg())
                .details(Map.of("field", normalizeFieldName(fieldName)))
                .build();
    }

    private static String normalizeFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return "unknown";
        }
        return fieldName.trim();
    }
}
