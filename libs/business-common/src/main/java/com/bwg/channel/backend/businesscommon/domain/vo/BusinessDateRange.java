package com.bwg.channel.backend.businesscommon.domain.vo;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;

import java.time.LocalDate;
import java.util.Map;

/**
 * 시작일과 종료일을 포함 경계로 다루는 불변 업무 날짜 범위
 *
 * <p>종료일 null은 종료 제한이 없는 열린 범위</p>
 */
public record BusinessDateRange(LocalDate startDate, LocalDate endDate) {

    public BusinessDateRange {
        if (startDate == null) {
            throw new BwgBusinessException.Builder()
                    .code(BusinessErrorCode.REQUIRED_VALUE_MISSING)
                    .message(BusinessErrorCode.REQUIRED_VALUE_MISSING.getMsg())
                    .details(Map.of("field", "startDate"))
                    .build();
        }

        if (endDate != null && startDate.isAfter(endDate)) {
            throw new BwgBusinessException.Builder()
                    .code(BusinessErrorCode.INVALID_DATE_RANGE)
                    .message(BusinessErrorCode.INVALID_DATE_RANGE.getMsg())
                    .details(Map.of("startDate", startDate, "endDate", endDate))
                    .build();
        }
    }

    public static BusinessDateRange of(LocalDate startDate, LocalDate endDate) {
        return new BusinessDateRange(startDate, endDate);
    }

    public static BusinessDateRange from(LocalDate startDate) {
        return new BusinessDateRange(startDate, null);
    }

    public boolean isOpenEnded() {
        return endDate == null;
    }

    /**
     * 포함 경계 기준 날짜 범위 포함 여부
     */
    public boolean contains(LocalDate date) {
        if (date == null || date.isBefore(startDate)) {
            return false;
        }
        return endDate == null || !date.isAfter(endDate);
    }
}
