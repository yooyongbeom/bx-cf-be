package com.bwg.channel.backend.common.aop;

import static org.assertj.core.api.Assertions.assertThat;

import com.bwg.channel.backend.common.constants.enums.BusinessTestErrorCode;
import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.exception.BwgException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class BwgAuthExceptionAdviceTest {

    private final BwgAuthExceptionAdvice advice = new BwgAuthExceptionAdvice();

    @Test
    void bwgExceptionUsesErrorCodeStatus() {
        BwgException exception = new TestBwgException(BusinessTestErrorCode.NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> response = advice.handleBwgException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(BusinessTestErrorCode.NOT_FOUND.getCode());
    }

    private static final class TestBwgException extends BwgException {
        private static final long serialVersionUID = 1L;

        private TestBwgException(BwgErrorCode code) {
            super(new TestBuilder(code));
        }
    }

    private static final class TestBuilder extends BwgException.Builder<TestBuilder> {
        private TestBuilder(BwgErrorCode code) {
            code(code).message(code.getMsg());
        }

        @Override
        public BwgException build() {
            return new TestBwgException(getCodeForBuild());
        }

        private BwgErrorCode getCodeForBuild() {
            return BusinessTestErrorCode.NOT_FOUND;
        }
    }
}
