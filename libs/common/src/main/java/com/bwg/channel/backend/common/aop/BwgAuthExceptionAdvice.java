package com.bwg.channel.backend.common.aop;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.exception.BwgException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BwgAuthExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger(BwgAuthExceptionAdvice.class);

    @ExceptionHandler(BwgException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ApiResponse<Void>> handleBwgException(HttpServletRequest request, BwgException e) {
        BwgErrorCode errorCode = e.getCode();
        String code = errorCode != null ? errorCode.getCode() : BwgException.FALLBACK_CODE;
        String msg  = e.getMessage()  != null ? e.getMessage()  : BwgException.FALLBACK_MSG;

        log.error("BwgException [{}] {}", code, msg);
        return new ResponseEntity<>(ApiResponse.fail(code, msg), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage());
        return new ResponseEntity<>(
            ApiResponse.fail(BwgException.FALLBACK_CODE, BwgException.FALLBACK_MSG),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
