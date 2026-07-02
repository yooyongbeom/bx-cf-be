package com.bwg.channel.backend.common.openapi.typebridge.annotation;

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
 *
 * <p>요청/응답 DTO를 분리하는 신규 방식에서는 {@code type}으로 방향을 지정하고, 각 방향의 DTO가
 * 각자 {@code endpoints}와 {@code @ApiField}의 {@code required/optional/exclude}로 엔드포인트별
 * 필수/노출을 제어한다. {@code type}을 지정하지 않으면 기존 방식({@link ApiType#LEGACY})으로 동작한다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDto {

    /**
     * 이 DTO가 요청/응답 중 어느 방향인지. 지정하지 않으면 하나의 DTO가 요청/응답을 겸하는
     * 기존 방식({@link ApiType#LEGACY})으로 동작한다.
     */
    ApiType type() default ApiType.LEGACY;

    /** TypeScript 인터페이스 기본 이름 (미지정 시 클래스명에서 Req/Res/Dto 접미사 제거) */
    String name() default "";

    /** 이 DTO가 사용되는 엔드포인트 ID 목록 (요청/응답 방향은 {@code type}에 따름) */
    String[] endpoints() default {};

    /** (LEGACY 전용) 단일 Response 스키마 생성 여부. REQUEST/RESPONSE 타입에서는 무시된다. */
    boolean generateResponse() default true;
}
