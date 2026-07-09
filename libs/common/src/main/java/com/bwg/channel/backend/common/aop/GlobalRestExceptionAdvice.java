package com.bwg.channel.backend.common.aop;

import com.bwg.channel.backend.common.constants.enums.BwgErrorCode;
import com.bwg.channel.backend.common.constants.enums.CommonErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.exception.BwgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.TimeoutException;

/**
 * REST boundary에서 밖으로 나가는 예외를 공통 {@link ApiResponse}로 변환한다.
 * <p>
 * 중간 레이어의 일반 예외를 강제로 업무 예외로 감싸지 않고, 최종 HTTP boundary에서 예외 성격에 맞는
 * 공통 오류 코드와 상태로 응답한다.
 */
@RestControllerAdvice
public class GlobalRestExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionAdvice.class);

    /**
     * 업무 예외는 예외에 담긴 오류 코드를 우선 사용하고, 코드가 비어 있으면 서버 오류로 처리한다.
     */
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

    /** Bean Validation/바인딩 오류는 프론트에서 동일하게 처리할 수 있도록 공통 필수값 오류로 매핑한다. */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    protected ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
        log.warn("ValidationException: {}", e.getMessage());
        return response(CommonErrorCode.REQUIRED_VALUE_MISSING);
    }

    /** JSON 문법 오류나 타입 불일치처럼 요청 본문을 읽을 수 없는 경우의 공통 응답 처리. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return response(CommonErrorCode.JSON_STR_TO_VO_PARSING_ERROR);
    }

    /** DB query timeout은 일반 DB 오류보다 구체적인 timeout 응답으로 분류한다. */
    @ExceptionHandler(QueryTimeoutException.class)
    protected ResponseEntity<ApiResponse<Void>> handleQueryTimeoutException(QueryTimeoutException e) {
        log.error("QueryTimeoutException: {}", e.getMessage(), e);
        return response(CommonErrorCode.DB_TIMEOUT_ERROR);
    }

    /** DB 접근 중 발생한 시스템 오류를 공통 DB 오류로 변환한다. */
    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
        log.error("DataAccessException: {}", e.getMessage(), e);
        return response(CommonErrorCode.DB_ACCESS_ERROR);
    }

    /** 외부 API, 비동기 처리 등에서 전파된 timeout을 공통 timeout 응답으로 변환한다. */
    @ExceptionHandler(TimeoutException.class)
    protected ResponseEntity<ApiResponse<Void>> handleTimeoutException(TimeoutException e) {
        log.error("TimeoutException: {}", e.getMessage(), e);
        return response(CommonErrorCode.TIMEOUT_ERROR);
    }

    /** 위에서 분류되지 않은 모든 예외는 내부 서버 오류로 응답 포맷을 유지한다. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return response(CommonErrorCode.SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> response(BwgErrorCode errorCode) {
        return new ResponseEntity<>(
                ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()),
                errorCode.getStatus()
        );
    }
}
