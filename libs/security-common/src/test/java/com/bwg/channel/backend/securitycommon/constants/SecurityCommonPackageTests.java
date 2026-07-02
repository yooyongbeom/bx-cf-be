package com.bwg.channel.backend.securitycommon.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityCommonPackageTests {

    @Test
    void authErrorCodeIsExposedFromSecurityCommonPackage() {
        assertThat(AuthErrorCode.UNAUTHORIZED_CLIENT.getCode()).isEqualTo("-1003");
    }
}
