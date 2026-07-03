package com.bwg.channel.backend.gateway.aop;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.gateway.constants.GatewayErrorCode;
import com.bwg.channel.backend.gateway.exception.BwgGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BwgGatewayExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger(BwgGatewayExceptionAdvice.class);

    @ExceptionHandler(BwgGatewayException.class)
    protected ResponseEntity<ApiResponse<Void>> bwgGatewayException(BwgGatewayException e) {
        GatewayErrorCode errorCode = e.getCode() != null ? e.getCode() : GatewayErrorCode.SERVER_ERROR;
        String code = errorCode.getCode();
        String msg = e.getMessage() != null ? e.getMessage() : errorCode.getMsg();

        ApiResponse<Void> res = ApiResponse.fail(code, msg);
        log.error("BWG Gateway Service BwgGatewayException result {} {}", code, msg, e);

        return new ResponseEntity<>(res, errorCode.getStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        ApiResponse<Void> res = ApiResponse.fail(
                GatewayErrorCode.SERVER_ERROR.getCode(),
                GatewayErrorCode.SERVER_ERROR.getMsg()
        );
        log.error("BWG Gateway Service RuntimeException result : {}", e.getMessage(), e);

        return new ResponseEntity<>(res, GatewayErrorCode.SERVER_ERROR.getStatus());
    }
}
