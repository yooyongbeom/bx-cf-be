package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TypeBridge 커스터마이저가 공통으로 사용하는 DTO 메타데이터 처리 기능을 제공한다.
 */
final class TypeBridgeSupport {

    private TypeBridgeSupport() {
    }

    /**
     * 상속 계층의 필드를 부모부터 자식 순서로 반환한다.
     *
     * @param type 필드를 조회할 DTO 타입
     * @return 부모 필드를 앞에 배치한 전체 필드 목록
     */
    static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            // 상속 DTO의 문서와 예제가 선언 구조를 자연스럽게 따르도록 부모 필드를 앞에 둔다.
            fields.addAll(0, Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * 상속 필드를 포함해 {@link ApiField}가 선언된 DTO인지 확인한다.
     *
     * @param type 확인할 DTO 타입
     * @return 문서화 대상 필드가 하나 이상이면 {@code true}
     */
    static boolean hasApiFields(Class<?> type) {
        return getAllFields(type).stream()
                .anyMatch(field -> field.getAnnotation(ApiField.class) != null);
    }

    /**
     * 명시된 이름을 우선하고, 없으면 DTO 클래스명의 요청·응답 접미사를 제거해 기본 이름을 만든다.
     *
     * @param type 이름을 계산할 DTO 타입
     * @param apiDto DTO 메타데이터
     * @return OpenAPI 스키마 기본 이름
     */
    static String resolveBaseName(Class<?> type, ApiDto apiDto) {
        return apiDto.name().isEmpty()
                ? type.getSimpleName().replaceAll("(Req|Res|Request|Response)?Dto$", "")
                : apiDto.name();
    }

    /**
     * 하이픈 또는 밑줄로 구분된 endpoint ID를 PascalCase 스키마 이름 조각으로 변환한다.
     *
     * @param value 변환할 endpoint ID
     * @return PascalCase 문자열
     */
    static String toPascalCase(String value) {
        return Arrays.stream(value.split("[-_]"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining());
    }
}
