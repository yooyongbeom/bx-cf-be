package com.bwg.channel.backend.common.constants.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BwgErrorCodeContractTest {

    @Test
    void errorCodeExposesHttpStatus() {
        BwgErrorCode errorCode = new BwgErrorCode() {
            @Override
            public String getCode() {
                return "-9999";
            }

            @Override
            public String getMsg() {
                return "server error";
            }

            @Override
            public HttpStatus getStatus() {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        };

        assertThat(errorCode.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void commonErrorCodesDoNotContainDuplicateCodes() {
        assertThat(uniqueCodeCount(CommonErrorCode.values())).isEqualTo(CommonErrorCode.values().length);
    }

    private long uniqueCodeCount(BwgErrorCode[] errorCodes) {
        return Arrays.stream(errorCodes)
                .map(BwgErrorCode::getCode)
                .distinct()
                .count();
    }
}
