package com.bwg.channel.backend.common.aop;

import com.bwg.channel.backend.common.exception.BwgException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BwgExceptionAspect {

    @Around(
        "(execution(* com.bwg.channel.backend..controller..*(..)) || " +
        " execution(* com.bwg.channel.backend..service..*(..))    || " +
        " execution(* com.bwg.channel.backend..repository..*(..))" +
        ") && !within(com.bwg.channel.backend.common..*)" +
        "&& !within(com.bwg.channel.backend.authcore..*)"
    )
    public Object wrapException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BwgException e) {
            throw e;
        } catch (Exception e) {
            throw new BwgException.Builder<BwgException.Builder<?>>() {
                @Override
                public BwgException build() {
                    return new BwgException(this) {};
                }
            }.message(e.getMessage()).cause(e).build();
        }
    }
}
