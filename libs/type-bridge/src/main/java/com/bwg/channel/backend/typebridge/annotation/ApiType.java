package com.bwg.channel.backend.typebridge.annotation;

/**
 * {@link ApiDto}가 요청/응답 중 어느 방향의 스키마를 생성하는지 구분한다.
 *
 * <ul>
 *   <li>{@link #REQUEST}  – 엔드포인트별 {@code {Name}{Endpoint}Request} 스키마 생성</li>
 *   <li>{@link #RESPONSE} – 엔드포인트별 {@code {Name}{Endpoint}Response} 스키마 생성</li>
 *   <li>{@link #LEGACY}   – (하위호환) 하나의 DTO가 요청/응답을 겸하는 기존 방식.
 *       {@code endpoints}로 Request를, {@code generateResponse}로 단일 Response를 생성한다.
 *       신규 코드는 REQUEST/RESPONSE 분리 사용을 권장한다.</li>
 * </ul>
 */
public enum ApiType {
    REQUEST,
    RESPONSE,
    LEGACY
}
