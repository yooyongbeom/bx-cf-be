package com.bwg.channel.backend.securitycommon.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void createAccessTokenStoresSessionIdClaim() {
        JwtUtil jwtUtil = new JwtUtil();
        jwtUtil.secretKey = "01234567890123456789012345678901";
        ReflectionTestUtils.setField(jwtUtil, "accessTokenValidityMillis", 60000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenValidityMillis", 120000L);
        jwtUtil.init();

        String token = jwtUtil.createAccessToken("hong.gildong", List.of("USER"), "session-123");

        assertThat(jwtUtil.getSubject(token)).isEqualTo("hong.gildong");
        assertThat(jwtUtil.getSessionId(token)).isEqualTo("session-123");
    }
}
