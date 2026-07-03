package com.bwg.channel.backend.common.aop;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;
import com.bwg.channel.backend.common.constants.enums.CommonErrorCode;
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
        BwgErrorCode errorCode = e.getCode() != null ? e.getCode() : CommonErrorCode.SERVER_ERROR;
        String code = errorCode.getCode();
        String msg = e.getMessage() != null ? e.getMessage() : errorCode.getMsg();
        HttpStatus status = errorCode.getStatus();

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
                ApiResponse.fail(
                        CommonErrorCode.REQUIRED_VALUE_MISSING.getCode(),
                        CommonErrorCode.REQUIRED_VALUE_MISSING.getMsg()
                ),
                CommonErrorCode.REQUIRED_VALUE_MISSING.getStatus()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return new ResponseEntity<>(
                ApiResponse.fail(
                        CommonErrorCode.JSON_STR_TO_VO_PARSING_ERROR.getCode(),
                        CommonErrorCode.JSON_STR_TO_VO_PARSING_ERROR.getMsg()
                ),
                CommonErrorCode.JSON_STR_TO_VO_PARSING_ERROR.getStatus()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                ApiResponse.fail(CommonErrorCode.SERVER_ERROR.getCode(), CommonErrorCode.SERVER_ERROR.getMsg()),
                CommonErrorCode.SERVER_ERROR.getStatus()
        );
    }
}
