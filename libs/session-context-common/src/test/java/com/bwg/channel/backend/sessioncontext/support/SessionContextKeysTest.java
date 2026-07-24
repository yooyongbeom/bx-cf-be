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
}
