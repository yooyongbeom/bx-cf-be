package com.bwg.channel.backend.typebridge.customizer;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ApiDto / @ApiField 어노테이션을 스캔하여 엔드포인트별 OpenAPI 스키마를 생성한다.
 *
 * 1. {Name}{Endpoint}Request 스키마 → components 등록 (엔드포인트별 필드/필수값 분리)
 * 2. {Name}Response 스키마 → components 등록
 * 3. 기존 ApiResponse«{Dto}» 스키마의 payload 참조를 {Name}Response로 교체한 변형 스키마 생성
 *
 * openapi-typescript는 /v3/api-docs를 읽어 각 엔드포인트별 올바른 TypeScript 타입을 자동 생성한다.
 *
 * <p>실행 순서 주의: springdoc은 GlobalOpenApiCustomizer와 OpenApiCustomizer를 하나의 리스트로 합쳐
 * 실행하므로, payload/변형 래퍼 스키마를 만드는 이 커스터마이저는 이를 인라인으로 펼치는
 * {@code ResponseWrapperSchemaCustomizer}보다 <b>반드시 먼저</b> 실행되어야 한다.
 * (먼저 실행되지 않으면 응답 스키마에서 payload가 통째로 누락된다.) 그래서 더 높은 우선순위를 부여한다.
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Component
public class TypeBridgeOpenApiCustomizer implements GlobalOpenApiCustomizer {

    private static final String BASE_PACKAGE = "com.bwg.channel.backend";

    /**
     * @ApiDto로 표시된 DTO의 엔드포인트별 요청/응답 스키마를 OpenAPI 컴포넌트에 등록한다.
     */
    @Override
    public void customise(OpenAPI openApi) {
        Components components = openApi.getComponents() != null
                ? openApi.getComponents() : new Components();
        if (components.getSchemas() == null) {
            components.setSchemas(new LinkedHashMap<>());
        }
        openApi.setComponents(components);

        Set<Class<?>> dtoClasses = scanDtoClasses();

        for (Class<?> clazz : dtoClasses) {
            ApiDto apiDto = clazz.getAnnotation(ApiDto.class);
            String baseName = resolveBaseName(clazz, apiDto);

            switch (apiDto.type()) {
                // 신규: 요청 전용 DTO → 엔드포인트별 Request 스키마
                case REQUEST -> {
                    for (String endpoint : apiDto.endpoints()) {
                        components.getSchemas().put(baseName + toPascalCase(endpoint) + "Request",
                                buildEndpointSchema(clazz, endpoint));
                    }
                }
                // 신규: 응답 전용 DTO → 엔드포인트별 Response 스키마 (필수/노출 엔드포인트별 제어)
                case RESPONSE -> {
                    for (String endpoint : apiDto.endpoints()) {
                        components.getSchemas().put(baseName + toPascalCase(endpoint) + "Response",
                                buildEndpointSchema(clazz, endpoint));
                    }
                }
                // 하위호환: 하나의 DTO가 요청/응답 겸용
                case LEGACY -> {
                    for (String endpoint : apiDto.endpoints()) {
                        components.getSchemas().put(baseName + toPascalCase(endpoint) + "Request",
                                buildRequestSchema(clazz, endpoint));
                    }
                    if (apiDto.generateResponse()) {
                        components.getSchemas().put(baseName + "Response", buildResponseSchema(clazz));
                    }
                }
            }
        }

        // (LEGACY 전용) ApiResponse«LoginDto» → ApiResponse«LoginResponse» 변형 스키마 생성
        createApiResponseVariants(components, dtoClasses);
    }

    // ── 스키마 빌드 ────────────────────────────────────────────────────────

    private Schema<?> buildRequestSchema(Class<?> clazz, String endpoint) {
        Schema<Object> schema = new Schema<>();
        schema.setType("object");

        Map<String, Schema> properties = new LinkedHashMap<>();
        List<String> requiredList = new ArrayList<>();

        for (Field field : getAllFields(clazz)) {
            ApiField af = field.getAnnotation(ApiField.class);
            if (af == null || af.hidden()) continue;
            if (af.responseOnly()) continue;
            if (Arrays.asList(af.exclude()).contains(endpoint)) continue;

            boolean isRequired = Arrays.asList(af.required()).contains(endpoint);
            boolean isOptional = Arrays.asList(af.optional()).contains(endpoint);
            if (!isRequired && !isOptional) continue;

            properties.put(field.getName(), toFieldSchema(field, af));
            if (isRequired) requiredList.add(field.getName());
        }

        schema.setProperties(properties);
        if (!requiredList.isEmpty()) schema.setRequired(requiredList);
        return schema;
    }

    /**
     * (REQUEST/RESPONSE 공통) 엔드포인트별 스키마를 만든다.
     * 필드는 해당 엔드포인트의 {@code required} 또는 {@code optional}에 있어야 노출되고,
     * {@code required}면 필수, {@code exclude}면 제외된다. (요청·응답 동일 규칙)
     */
    private Schema<?> buildEndpointSchema(Class<?> clazz, String endpoint) {
        Schema<Object> schema = new Schema<>();
        schema.setType("object");

        Map<String, Schema> properties = new LinkedHashMap<>();
        List<String> requiredList = new ArrayList<>();

        for (Field field : getAllFields(clazz)) {
            ApiField af = field.getAnnotation(ApiField.class);
            if (af == null || af.hidden()) continue;
            if (Arrays.asList(af.exclude()).contains(endpoint)) continue;

            boolean isRequired = Arrays.asList(af.required()).contains(endpoint);
            boolean isOptional = Arrays.asList(af.optional()).contains(endpoint);
            if (!isRequired && !isOptional) continue;

            properties.put(field.getName(), toFieldSchema(field, af));
            if (isRequired) requiredList.add(field.getName());
        }

        schema.setProperties(properties);
        if (!requiredList.isEmpty()) schema.setRequired(requiredList);
        return schema;
    }

    private Schema<?> buildResponseSchema(Class<?> clazz) {
        Schema<Object> schema = new Schema<>();
        schema.setType("object");

        Map<String, Schema> properties = new LinkedHashMap<>();
        for (Field field : getAllFields(clazz)) {
            ApiField af = field.getAnnotation(ApiField.class);
            if (af == null || af.hidden()) continue;
            if (Arrays.asList(af.exclude()).contains("response")) continue;
            properties.put(field.getName(), toFieldSchema(field, af));
        }

        schema.setProperties(properties);
        return schema;
    }

    // ── ApiResponse 변형 스키마 ────────────────────────────────────────────

    /**
     * SpringDoc이 생성한 래퍼 스키마(예: ApiResponse«XxxDto»)를 찾아
     * payload $ref만 XxxResponse로 교체한 변형 스키마를 등록한다.
     */
    private void createApiResponseVariants(Components components, Set<Class<?>> dtoClasses) {
        // 현재 등록된 스키마 목록을 복사해서 순회 (ConcurrentModification 방지)
        Map<String, Schema> snapshot = new LinkedHashMap<>(components.getSchemas());

        for (Class<?> clazz : dtoClasses) {
            ApiDto apiDto = clazz.getAnnotation(ApiDto.class);
            if (apiDto.type() != ApiType.LEGACY) continue;   // 신규 REQUEST/RESPONSE는 변형 스키마 불필요
            if (!apiDto.generateResponse()) continue;

            String dtoName      = clazz.getSimpleName();
            String baseName     = resolveBaseName(clazz, apiDto);
            String responseName = baseName + "Response";

            // dtoName을 포함하는 래퍼 스키마 탐색 (예: "ApiResponse«XxxDto»")
            snapshot.entrySet().stream()
                    .filter(e -> e.getKey().contains(dtoName) && !e.getKey().equals(dtoName))
                    .forEach(e -> {
                        String originalKey = e.getKey();
                        String newKey = originalKey.replace(dtoName, responseName);

                        if (!components.getSchemas().containsKey(newKey)) {
                            components.getSchemas().put(newKey,
                                    cloneSchemaRefs(e.getValue(), dtoName, responseName));
                        }
                    });
        }
    }

    /** schema 내부의 dtoName $ref를 responseName $ref로 교체한 복사본 반환 */
    private Schema<?> cloneSchemaRefs(Schema<?> original, String dtoName, String responseName) {
        if (original == null) return null;

        Schema<Object> copy = original instanceof ArraySchema ? new ArraySchema() : new Schema<>();
        copy.setType(original.getType());
        copy.setFormat(original.getFormat());
        copy.setDescription(original.getDescription());
        copy.setNullable(original.getNullable());

        if (original.get$ref() != null) {
            copy.set$ref(original.get$ref().replace(dtoName, responseName));
        }

        if (original instanceof ArraySchema originalArray && originalArray.getItems() != null) {
            ((ArraySchema) copy).setItems(cloneSchemaRefs(originalArray.getItems(), dtoName, responseName));
        }

        if (original.getProperties() != null) {
            Map<String, Schema> newProps = new LinkedHashMap<>();
            original.getProperties().forEach((propName, propSchema) ->
                    newProps.put(propName, cloneSchemaRefs(propSchema, dtoName, responseName)));
            copy.setProperties(newProps);
        }

        if (original.getRequired() != null) {
            copy.setRequired(new ArrayList<>(original.getRequired()));
        }
        return copy;
    }

    // ── 필드 스키마 변환 ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Schema<?> toFieldSchema(Field field, ApiField af) {
        Schema schema;

        Class<?> type = field.getType();
        if (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) {
            ArraySchema array = new ArraySchema();
            Type generic = field.getGenericType();
            if (generic instanceof ParameterizedType pt) {
                array.setItems(primitiveSchema((Class<?>) pt.getActualTypeArguments()[0], af));
            } else {
                array.setItems(new Schema<>().type("string"));
            }
            schema = array;
        } else {
            schema = primitiveSchema(type, af);
        }

        // 공통 메타데이터
        if (!af.description().isEmpty())  schema.setDescription(af.description());
        if (!af.example().isEmpty())       schema.setExample(af.example());
        if (!af.format().isEmpty())        schema.setFormat(af.format());
        if (af.nullable())                 schema.setNullable(true);
        if (!af.pattern().isEmpty())       schema.setPattern(af.pattern());
        if (!af.defaultValue().isEmpty())  schema.setDefault(af.defaultValue());
        if (af.minLength() >= 0)           schema.setMinLength(af.minLength());
        if (af.maxLength() >= 0)           schema.setMaxLength(af.maxLength());
        if (!af.minimum().isEmpty())       schema.setMinimum(new BigDecimal(af.minimum()));
        if (!af.maximum().isEmpty())       schema.setMaximum(new BigDecimal(af.maximum()));
        if (af.allowableValues().length > 0) schema.setEnum(Arrays.asList(af.allowableValues()));

        return schema;
    }

    private Schema<?> primitiveSchema(Class<?> type, ApiField af) {
        // format이 명시되어 있으면 string + format
        if (!af.format().isEmpty()) return new Schema<>().type("string");

        Schema<?> s = new Schema<>();
        if (String.class.isAssignableFrom(type)) {
            s.setType("string");
        } else if (int.class == type || Integer.class.isAssignableFrom(type)) {
            s.setType("integer"); s.setFormat("int32");
        } else if (long.class == type || Long.class.isAssignableFrom(type)) {
            s.setType("integer"); s.setFormat("int64");
        } else if (double.class == type || Double.class.isAssignableFrom(type)
                || float.class == type || Float.class.isAssignableFrom(type)
                || BigDecimal.class.isAssignableFrom(type)) {
            s.setType("number");
        } else if (boolean.class == type || Boolean.class.isAssignableFrom(type)) {
            s.setType("boolean");
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            s.setType("string"); s.setFormat("date-time");
        } else if (LocalDate.class.isAssignableFrom(type)) {
            s.setType("string"); s.setFormat("date");
        } else {
            s.setType("string");
        }
        return s;
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────

    private Set<Class<?>> scanDtoClasses() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ApiDto.class));

        Set<Class<?>> result = new LinkedHashSet<>();
        scanner.findCandidateComponents(BASE_PACKAGE).forEach(bd -> {
            try {
                result.add(Class.forName(bd.getBeanClassName()));
            } catch (ClassNotFoundException ignored) {}
        });
        return result;
    }

    private String resolveBaseName(Class<?> clazz, ApiDto apiDto) {
        return apiDto.name().isEmpty()
                ? clazz.getSimpleName().replaceAll("(Req|Res|Request|Response)?Dto$", "")
                : apiDto.name();
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(0, Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private String toPascalCase(String hyphenated) {
        return Arrays.stream(hyphenated.split("[-_]"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining());
    }
}
