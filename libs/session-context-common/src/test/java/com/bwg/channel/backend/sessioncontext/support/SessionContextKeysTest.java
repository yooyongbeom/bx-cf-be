package com.bwg.channel.backend.sessioncontext.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionContextKeysTest {

    @Test
    void createsSessionKeyWithStablePrefix() {
        assertThat(SessionContextKeys.sessionKey("abc-123"))
                .isEqualTo("session:abc-123");
    }

    @Test
    void exposesOnlySessionKeysAsScanPattern() {
        assertThat(SessionContextKeys.sessionPattern()).isEqualTo("session:*");
    }

    @Test
    void createsUserSessionBlockKeyOutsideSessionScanNamespace() {
        String blockKey = SessionContextKeys.userSessionBlockKey("user-1");

        // 사용자 tombstone은 세션 SCAN 대상과 겹치지 않아 강제 로그아웃 탐색에서 제외되어야 한다.
        assertThat(blockKey)
                .isEqualTo("session-blocked:user-1")
                .doesNotStartWith("session:");
    }
}
