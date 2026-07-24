package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
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
import java.time.OffsetDateTime;
import java.util.*;

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
            String baseName = TypeBridgeSupport.resolveBaseName(clazz, apiDto);

            switch (apiDto.type()) {
                // 신규: 요청 전용 DTO → 엔드포인트별 Request 스키마
                case REQUEST -> {
                    for (String endpoint : apiDto.endpoints()) {
                        components.getSchemas().put(baseName + TypeBridgeSupport.toPascalCase(endpoint) + "Request",
                                buildApiRequestSchema(clazz, endpoint));
                    }
                }
                // 신규: 응답 전용 DTO → 엔드포인트별 Response 스키마 (필수/노출 엔드포인트별 제어)
                case RESPONSE -> {
                    for (String endpoint : apiDto.endpoints()) {
                        components.getSchemas().put(baseName + TypeBridgeSupport.toPascalCase(endpoint) + "Response",
                                buildEndpointSchema(clazz, endpoint));
                    }
                }
                // 하위호환: 하나의 DTO가 요청/응답 겸용
                case LEGACY -> {
                    for (String endpoint : apiDto.endpoints()) {
                        components.getSchemas().put(baseName + TypeBridgeSupport.toPascalCase(endpoint) + "Request",
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

    private Schema<?> buildApiRequestSchema(Class<?> dataClass, String endpoint) {
        Schema<Object> schema = new Schema<>();
        schema.setType("object");

        Map<String, Schema> properties = new LinkedHashMap<>();
        List<String> requiredList = new ArrayList<>();

        for (Field field : TypeBridgeSupport.getAllFields(ApiRequest.class)) {
            ApiField af = field.getAnnotation(ApiField.class);
            if (af == null || af.hidden()) continue;

            if ("data".equals(field.getName())) {
                // ApiRequest<T>의 data 자리에 실제 요청 DTO의 엔드포인트별 스키마를 끼워 넣는다.
                Schema<?> dataSchema = buildEndpointSchema(dataClass, endpoint);
                if (dataSchema.getProperties() == null || dataSchema.getProperties().isEmpty()) continue;

                properties.put("data", dataSchema);
                if (dataSchema.getRequired() != null && !dataSchema.getRequired().isEmpty()) {
                    requiredList.add("data");
                }
                continue;
            }

            if (Arrays.asList(af.exclude()).contains(endpoint)) continue;

            boolean isRequired = Arrays.asList(af.required()).contains(endpoint);
            boolean isOptional = Arrays.asList(af.optional()).contains(endpoint);
            if (!isRequired && !isOptional) continue;

            properties.put(field.getName(), toFieldSchema(field, af, endpoint));
            if (isRequired) requiredList.add(field.getName());
        }

        schema.setProperties(properties);
        if (!requiredList.isEmpty()) schema.setRequired(requiredList);
        schema.setExample(exampleFor(schema));
        return schema;
    }

    private Schema<?> buildRequestSchema(Class<?> clazz, String endpoint) {
        Schema<Object> schema = new Schema<>();
        schema.setType("object");

        Map<String, Schema> properties = new LinkedHashMap<>();
        List<String> requiredList = new ArrayList<>();

        for (Field field : TypeBridgeSupport.getAllFields(clazz)) {
            ApiField af = field.getAnnotation(ApiField.class);
            if (af == null || af.hidden()) continue;
            // LEGACY DTO는 요청/응답 겸용이므로 responseOnly 필드는 요청 스키마에서 제외한다.
            if (af.responseOnly()) continue;
            if (Arrays.asList(af.exclude()).contains(endpoint)) continue;

            boolean isRequired = Arrays.asList(af.required()).contains(endpoint);
            boolean isOptional = Arrays.asList(af.optional()).contains(endpoint);
            if (!isRequired && !isOptional) continue;

            properties.put(field.getName(), toFieldSchema(field, af, endpoint));
            if (isRequired) requiredList.add(field.getName());
        }

        schema.setProperties(properties);
        if (!requiredList.isEmpty()) schema.setRequired(requiredList);
        schema.setExample(exampleFor(schema));
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

        for (Field field : TypeBridgeSupport.getAllFields(clazz)) {
            ApiField af = field.getAnnotation(ApiField.class);
            if (af == null || af.hidden()) continue;
            if (Arrays.asList(af.exclude()).contains(endpoint)) continue;

            boolean isRequired = Arrays.asList(af.required()).contains(endpoint);
            boolean isOptional = Arrays.asList(af.optional()).contains(endpoint);
            if (!isRequired && !isOptional) continue;

            properties.put(field.getName(), toFieldSchema(field, af, endpoint));
            if (isRequired) requiredList.add(field.getName());
        }

        schema.setProperties(properties);
        if (!requiredList.isEmpty()) schema.setRequired(requiredList);
        schema.setExample(exampleFor(schema));
        return schema;
    }

    private Schema<?> buildResponseSchema(Class<?> clazz) {
        Schema<Object> schema = new Schema<>();
        schema.setType("object");

        Map<String, Schema> properties = new LinkedHashMap<>();
        // LEGACY 응답은 엔드포인트별 분기 없이 response 컨텍스트 기준의 단일 스키마를 만든다.
        for (Field field : TypeBridgeSupport.getAllFields(clazz)) {
            ApiField af = field.getAnnotation(ApiField.class);
            if (af == null || af.hidden()) continue;
            if (Arrays.asList(af.exclude()).contains("response")) continue;
            properties.put(field.getName(), toFieldSchema(field, af, "response"));
        }

        schema.setProperties(properties);
        schema.setExample(exampleFor(schema));
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
            String baseName     = TypeBridgeSupport.resolveBaseName(clazz, apiDto);
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

        // springdoc이 만든 래퍼 구조는 유지하고, 내부 $ref 이름만 응답 전용 스키마로 바꾼다.
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
    private Schema<?> toFieldSchema(Field field, ApiField af, String endpoint) {
        Schema schema;

        Class<?> type = field.getType();
        if (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) {
            ArraySchema array = new ArraySchema();
            Type generic = field.getGenericType();
            // 제네릭 요소 타입을 알 수 있으면 해당 타입의 스키마를, 알 수 없으면 안전하게 string으로 둔다.
            if (generic instanceof ParameterizedType pt) {
                Type itemType = pt.getActualTypeArguments()[0];
                if (itemType instanceof Class<?> itemClass) {
                    array.setItems(fieldSchemaForClass(itemClass, af, endpoint));
                } else {
                    array.setItems(new Schema<>().type("string"));
                }
            } else {
                array.setItems(new Schema<>().type("string"));
            }
            schema = array;
        } else {
            schema = fieldSchemaForClass(type, af, endpoint);
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

    private Schema<?> fieldSchemaForClass(Class<?> type, ApiField af, String endpoint) {
        if (TypeBridgeSupport.hasApiFields(type)) {
            // 중첩 DTO도 같은 엔드포인트 규칙(required/optional/exclude)을 적용해 인라인 스키마로 만든다.
            return buildEndpointSchema(type, endpoint);
        }
        return primitiveSchema(type, af);
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
        } else if (LocalDateTime.class.isAssignableFrom(type) || OffsetDateTime.class.isAssignableFrom(type)) {
            s.setType("string"); s.setFormat("date-time");
        } else if (LocalDate.class.isAssignableFrom(type)) {
            s.setType("string"); s.setFormat("date");
        } else {
            s.setType("string");
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private Object exampleFor(Schema<?> schema) {
        if (schema == null) {
            return null;
        }
        if (schema.getExample() != null) {
            return schema.getExample();
        }
        if (schema instanceof ArraySchema arraySchema) {
            return List.of(exampleFor(arraySchema.getItems()));
        }
        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            // object/array 구조를 따라 내려가며 문서 예시도 실제 스키마 모양과 맞춘다.
            Map<String, Object> example = new LinkedHashMap<>();
            schema.getProperties().forEach((name, propertySchema) ->
                    example.put(name, exampleFor((Schema<?>) propertySchema)));
            return example;
        }

        String type = schema.getType();
        if ("integer".equals(type)) return 0;
        if ("number".equals(type)) return 0;
        if ("boolean".equals(type)) return true;
        return "string";
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────

    private Set<Class<?>> scanDtoClasses() {
        // 각 서비스 모듈의 DTO까지 찾기 위해 common이 아닌 backend 루트 패키지 전체를 스캔한다.
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

}
