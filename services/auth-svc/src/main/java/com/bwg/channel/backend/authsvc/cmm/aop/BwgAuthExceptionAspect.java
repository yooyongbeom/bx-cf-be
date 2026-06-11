package com.bwg.channel.backend.authsvc.cmm.aop;

import com.bwg.channel.backend.common.constants.enums.AuthErrorCode;
import com.bwg.channel.backend.common.exception.BwgAuthException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BwgAuthExceptionAspect {
    /**
     * ...controller, service, repository 패키지 아래 모든 메서드 대상
     */
    @Around(
            "execution(* com.bwg.channel.backend.authsvc.controller..*(..)) || " +
                    "execution(* com.bwg.channel.backend.authsvc.service..*(..)) || " +
                    "execution(* com.bwg.channel.backend.authsvc.repository..*(..))"
    )
    public Object wrapAuthException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 실제 메서드 실행
            return joinPoint.proceed();
        } catch (BwgAuthException e) {
            // 이미 BwgAuthException이면 그대로 던짐
            throw e;
        } catch (Exception e) {
            // 다른 모든 예외를 자동으로 BwgAuthException으로 변환
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.SERVER_ERROR)
                    .message(e.getMessage())
                    .cause(e)
                    .build();
        }
    }
}
