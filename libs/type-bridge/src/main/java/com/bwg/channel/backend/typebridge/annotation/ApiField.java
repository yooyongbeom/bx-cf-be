package com.bwg.channel.backend.typebridge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DTO 필드의 엔드포인트별 필수/선택/제외 규칙 및 OpenAPI 스키마 메타데이터를 정의한다.
 *
 * <pre>
 * {@literal @}ApiField(description = "비밀번호", format = "password",
 *           required = {"login"}, exclude = {"erp-login"})
 * private String usrPwd;
 *
 * {@literal @}ApiField(description = "액세스 토큰", responseOnly = true)
 * private String accessToken;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiField {

    // ── 공통 메타데이터 (@Schema 대체) ──────────────────────────────────────

    /** 필드 설명 */
    String description() default "";

    /** 예시 값 */
    String example() default "";

    /** OpenAPI format (예: "email", "password", "date-time", "uri") */
    String format() default "";

    /** null 허용 여부 */
    boolean nullable() default false;

    /** 최솟값 (숫자 타입) */
    String minimum() default "";

    /** 최댓값 (숫자 타입) */
    String maximum() default "";

    /** 최소 길이 (문자열 타입, -1 = 미지정) */
    int minLength() default -1;

    /** 최대 길이 (문자열 타입, -1 = 미지정) */
    int maxLength() default -1;

    /** 정규식 패턴 */
    String pattern() default "";

    /** 기본값 */
    String defaultValue() default "";

    /** 허용 값 목록 (enum) */
    String[] allowableValues() default {};

    /** true이면 모든 스키마(Request/Response)에서 제외 */
    boolean hidden() default false;

    // ── 엔드포인트별 가시성 제어 ────────────────────────────────────────────

    /** 이 엔드포인트들에서 필수 */
    String[] required() default {};

    /** 이 엔드포인트들에서 선택 */
    String[] optional() default {};

    /** 이 엔드포인트들에서 완전히 제외 */
    String[] exclude() default {};

    /** true이면 Response 스키마에만 포함, 모든 Request에서 제외 */
    boolean responseOnly() default false;
}
