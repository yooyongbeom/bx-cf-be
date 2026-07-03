package com.bwg.channel.backend.businesscommon.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BusinessErrorCodeContractTest {

    @Test
    void businessErrorCodesUseBusinessCommonRangeAndStatus() {
        assertThat(BusinessErrorCode.INVALID_DATE_RANGE.getCode()).startsWith("-3");
        assertThat(BusinessErrorCode.INVALID_DATE_RANGE.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void businessErrorCodesDoNotContainDuplicateCodes() {
        long uniqueCount = Arrays.stream(BusinessErrorCode.values())
                .map(BusinessErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(BusinessErrorCode.values().length);
    }
}
