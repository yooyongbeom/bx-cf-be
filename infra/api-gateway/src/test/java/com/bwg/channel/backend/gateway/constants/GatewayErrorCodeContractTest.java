package com.bwg.channel.backend.gateway.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GatewayErrorCodeContractTest {

    @Test
    void gatewayErrorCodesUseGatewayRangeAndStatus() {
        assertThat(GatewayErrorCode.SERVER_ERROR.getCode()).startsWith("-9");
        assertThat(GatewayErrorCode.SERVER_ERROR.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void gatewayErrorCodesDoNotContainDuplicateCodes() {
        long uniqueCount = Arrays.stream(GatewayErrorCode.values())
                .map(GatewayErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(GatewayErrorCode.values().length);
    }
}
