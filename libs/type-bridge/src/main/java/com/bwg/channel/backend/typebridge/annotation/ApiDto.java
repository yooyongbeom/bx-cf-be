package com.bwg.channel.backend.typebridge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TypeScript 타입 생성 대상 DTO 클래스에 붙이는 어노테이션.
 *
 * 사용 예:
 * <pre>
 * {@literal @}ApiDto(name = "Login", endpoints = {"login", "erp-login"})
 * public class LoginDto { ... }
 * </pre>
 *
 * → LoginLoginRequest, LoginErpLoginRequest, LoginResponse 인터페이스 생성
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDto {

    /** TypeScript 인터페이스 기본 이름 (미지정 시 클래스명에서 Dto 제거) */
    String name() default "";

    /** 이 DTO가 요청으로 사용되는 엔드포인트 ID 목록 */
    String[] endpoints() default {};

    /** Response 인터페이스도 생성할지 여부 */
    boolean generateResponse() default true;
}
