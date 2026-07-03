package com.bwg.channel.backend.securitycommon.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AuthErrorCodeContractTest {

    @Test
    void authErrorCodesUseSecurityRangeAndStatus() {
        assertThat(AuthErrorCode.INVALID_TOKEN.getCode()).startsWith("-1");
        assertThat(AuthErrorCode.INVALID_TOKEN.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(AuthErrorCode.ACCESS_DENIED.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void authErrorCodesDoNotContainDuplicateCodes() {
        long uniqueCount = Arrays.stream(AuthErrorCode.values())
                .map(AuthErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(AuthErrorCode.values().length);
    }
}
