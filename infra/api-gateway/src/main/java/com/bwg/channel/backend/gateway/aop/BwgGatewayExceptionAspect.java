package com.bwg.channel.backend.gateway.aop;

import com.bwg.channel.backend.gateway.constants.GatewayErrorCode;
import com.bwg.channel.backend.gateway.exception.BwgGatewayException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BwgGatewayExceptionAspect {
    /**
     * ...controller, service
     */
    @Around(
            "execution(* com.bwg.channel.backend.gateway.controller..*(..)) || " +
                    "execution(* com.bwg.channel.backend.gateway.service..*(..))"
    )
    public Object wrapGatewayException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 실제 메서드 실행
            return joinPoint.proceed();
        } catch (BwgGatewayException e) {
            // 이미 BwgAuthException이면 그대로 던짐
            throw e;
        } catch (Exception e) {
            // 다른 모든 예외를 자동으로 BwgAuthException으로 변환
            throw new BwgGatewayException.Builder()
                    .code(GatewayErrorCode.SERVER_ERROR)
                    .message(e.getMessage())
                    .cause(e)
                    .build();
        }
    }
}
