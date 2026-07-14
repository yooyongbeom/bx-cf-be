package com.bwg.channel.backend.mcicommon.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class MciErrorCodeContractTest {

    @Test
    void mciErrorCodesUseMciRangeAndExpectedStatuses() {
        assertThat(Arrays.stream(MciErrorCode.values()))
                .allSatisfy(errorCode -> assertThat(errorCode.getCode()).startsWith("-6"));
        assertThat(MciErrorCode.REQUEST_HEADER_REQUIRED.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(MciErrorCode.TRANSACTION_CODE_REQUIRED.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(MciErrorCode.TRANSACTION_NOT_REGISTERED.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(MciErrorCode.TRANSACTION_DISABLED.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(MciErrorCode.CHANNEL_NOT_ALLOWED.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(MciErrorCode.ADAPTER_NOT_REGISTERED.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(MciErrorCode.MAPPER_NOT_REGISTERED.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(MciErrorCode.SERVER_ERROR.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void mciErrorCodesAreUniqueAndResolvable() {
        long uniqueCount = Arrays.stream(MciErrorCode.values())
                .map(MciErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(MciErrorCode.values().length);
        assertThat(MciErrorCode.fromCode("-6101")).isEqualTo(MciErrorCode.TRANSACTION_NOT_REGISTERED);
        assertThatThrownBy(() -> MciErrorCode.fromCode("-6998"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown MciErrorCode");
    }
}
