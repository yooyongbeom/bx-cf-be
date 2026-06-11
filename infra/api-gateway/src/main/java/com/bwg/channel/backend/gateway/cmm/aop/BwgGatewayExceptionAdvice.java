package com.bwg.channel.backend.gateway.cmm.aop;

import com.bwg.channel.backend.gateway.cmm.constants.GatewayErrorCode;
import com.bwg.channel.backend.gateway.cmm.exception.BwgGatewayException;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class BwgGatewayExceptionAdvice {
    private static final Logger log = LoggerFactory.getLogger(BwgGatewayExceptionAdvice.class);
    @ExceptionHandler({BwgGatewayException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ApiResponse<Void>> bwgAuthExServerError(HttpServletRequest request, BwgGatewayException e) {
        // 빌더로 만든 예외에서 코드와 메시지를 추출
        String code = e.getCode() != null ? e.getCode().getCode() : GatewayErrorCode.SERVER_ERROR.getCode();
        String msg = e.getMessage() != null ? e.getMessage() : GatewayErrorCode.SERVER_ERROR.getMsg();

        ApiResponse<Void> res = ApiResponse.fail(code, msg);
        log.error("BWG Gateway Service BwgGatewayExeption result {} {} ", code, msg);

        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Optional: 다른 런타임 예외 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        ApiResponse<Void> res = ApiResponse.fail(GatewayErrorCode.SERVER_ERROR.getCode(), GatewayErrorCode.SERVER_ERROR.getMsg());
        log.error("BWG Gateway Service RuntimeException result : {}", e.getMessage());

        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
