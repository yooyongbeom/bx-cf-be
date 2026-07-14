package com.bwg.channel.backend.mcicommon.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.bwg.channel.backend.mcicommon.constants.MciErrorCode;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MciExceptionTest {

    @Test
    void keepsTypedCodeSafeMessageAndDetails() {
        MciException exception = MciException.of(
                MciErrorCode.ADAPTER_NOT_REGISTERED,
                Collections.singletonMap("adapterName", "internalAdapter")
        );

        assertThat(exception.getCode()).isEqualTo(MciErrorCode.ADAPTER_NOT_REGISTERED);
        assertThat(exception.getMessage()).isEqualTo(MciErrorCode.ADAPTER_NOT_REGISTERED.getMsg());
        assertThat(exception.getMessage()).doesNotContain("internalAdapter");
        assertThat(exception.getDetails()).containsEntry("adapterName", "internalAdapter");
    }
}
