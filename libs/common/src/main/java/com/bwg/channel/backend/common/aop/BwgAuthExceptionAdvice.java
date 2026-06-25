package com.bwg.channel.backend.common.aop;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.exception.BwgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BwgAuthExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger(BwgAuthExceptionAdvice.class);

    @ExceptionHandler(BwgException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBwgException(BwgException e) {
        BwgErrorCode errorCode = e.getCode();
        String code = errorCode != null ? errorCode.getCode() : BwgException.FALLBACK_CODE;
        String msg  = e.getMessage()  != null ? e.getMessage()  : BwgException.FALLBACK_MSG;
        HttpStatus status = resolveStatus(code);

        if (status.is5xxServerError()) {
            log.error("BwgException [{}] {}", code, msg, e);
        } else {
            log.warn("BwgException [{}] {}", code, msg);
        }
        return new ResponseEntity<>(ApiResponse.fail(code, msg), status);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    protected ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
        log.warn("ValidationException: {}", e.getMessage());
        return new ResponseEntity<>(
            ApiResponse.fail("-1001", "필수값이 없거나 유효하지 않습니다"),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return new ResponseEntity<>(
            ApiResponse.fail("-2003", "Json String to VO Parsing Error"),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
            ApiResponse.fail(BwgException.FALLBACK_CODE, BwgException.FALLBACK_MSG),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private HttpStatus resolveStatus(String code) {
        return switch (code) {
            case "-1001", "-1002", "-2003", "-2004", "-5001", "-5002", "-5003", "-5004" -> HttpStatus.BAD_REQUEST;
            case "-1003", "-1004" -> HttpStatus.UNAUTHORIZED;
            case "-1005" -> HttpStatus.FORBIDDEN;
            case "-4001", "-5101", "-5102" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
