package com.bwg.channel.backend.common.openapi.customizer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 공통 응답 래퍼 스키마({@code ApiResponse*}, {@code CommonResponse*})를 swagger "Schemas" 목록에서
 * 완전히 제거한다. 다만 응답 모델의 정확도는 잃지 않도록, 각 응답의 래퍼 {@code $ref}를
 * <b>공통 응답 구조를 인라인으로 펼친 객체</b>(success/code/msg/requestId + 타입별 payload)로 교체한다.
 * <p>
 * 결과: Schemas 목록에는 도메인 DTO만 남고(래퍼 이름은 어디에도 노출되지 않음),
 * 각 API 응답 문서에는 공통 응답 구조 + 타입별 payload 가 그대로 표시된다.
 * <p>
 * 이 커스터마이저는 TypeBridge가 생성한 payload/변형 래퍼 스키마를 참조하므로,
 * {@code TypeBridgeOpenApiCustomizer} 이후에 실행되어야 한다(가장 낮은 우선순위로 마지막에 실행).
 * springdoc은 두 커스터마이저를 하나의 리스트로 합쳐 실행하는데 순서를 보장하지 않으므로 {@code @Order}로 고정한다.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class ResponseWrapperSchemaCustomizer implements OpenApiCustomizer {

    private static final String REF_PREFIX = "#/components/schemas/";

    // springdoc/swagger-core가 컬렉션 제네릭을 명명할 때 쓰는 접두사
    // (예: ApiResponse<List<ProductDto>> → "ApiResponseListProductDto")
    private static final String[] COLLECTION_PREFIXES = {"List", "Set", "Collection", "Iterable"};

    @Override
    public void customise(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (components == null || components.getSchemas() == null) {
            return;
        }
        Map<String, Schema> schemas = components.getSchemas();

        // 제거 대상(공통 응답 래퍼) 식별 + payload 스키마 추출 (응답 인라인 교체용)
        Map<String, Schema> wrapperPayload = new HashMap<>();
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            if (isWrapper(entry.getKey())) {
                Schema payload = entry.getValue().getProperties() == null
                        ? null
                        : (Schema) entry.getValue().getProperties().get("payload");
                wrapperPayload.put(entry.getKey(), payload);
            }
        }
        // 응답의 래퍼 $ref -> 공통 응답 구조 인라인 객체로 교체 (참조 깨짐 방지 + 공통 응답 구조 유지)
        if (openApi.getPaths() != null) {
            openApi.getPaths().values().forEach(path -> rewritePath(path, wrapperPayload, schemas));
        }

        // 문서에서 더 이상 도달할 수 없는 래퍼/원본 DTO만 제거한다.
        // 아직 남아 있는 $ref는 보존해 OpenAPI 참조 무결성을 깨뜨리지 않는다.
        removeUnreferencedDocumentationNoise(openApi, schemas);
    }

    private void rewritePath(PathItem path, Map<String, Schema> wrapperPayload, Map<String, Schema> componentSchemas) {
        for (Operation op : path.readOperations()) {
            ApiResponses responses = op.getResponses();
            if (responses == null) {
                continue;
            }
            for (ApiResponse resp : responses.values()) {
                Content content = resp.getContent();
                if (content == null) {
                    continue;
                }
                for (MediaType mediaType : content.values()) {
                    String wrapper = refName(mediaType.getSchema());
                    if (wrapper != null && isWrapper(wrapper)) {
                        // 우선 실제 래퍼의 payload를 사용하고, springdoc이 제거한 경우 이름에서 복원한다.
                        Schema payload = wrapperPayload.get(wrapper);
                        if (payload == null) {
                            payload = inferPayloadSchema(wrapper, componentSchemas);
                        }
                        mediaType.setSchema(inlineCommonResponse(payload));
                    }
                }
            }
        }
    }

    /** 공통 응답 구조(success/code/msg/requestId) + 타입별 payload 를 인라인으로 펼친 응답 스키마 */
    private Schema<?> inlineCommonResponse(Schema<?> payload) {
        ObjectSchema commonResponse = new ObjectSchema();
        commonResponse.setDescription("공통 응답 구조");
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("success", new BooleanSchema().description("성공 여부"));
        props.put("code", new StringSchema().description("응답 코드"));
        props.put("msg", new StringSchema().description("응답 메시지"));
        props.put("requestId", new StringSchema().description("요청 추적 ID"));
        if (payload != null) {
            props.put("payload", payload);
        }
        commonResponse.setProperties(props);
        return commonResponse;
    }

    private boolean isWrapper(String name) {
        return name != null && (name.startsWith("ApiResponse") || name.startsWith("CommonResponse"));
    }

    private String refName(Schema<?> schema) {
        if (schema == null || schema.get$ref() == null) {
            return null;
        }
        String ref = schema.get$ref();
        return ref.startsWith(REF_PREFIX) ? ref.substring(REF_PREFIX.length()) : null;
    }

    /**
     * 래퍼 이름에서 payload 스키마를 추론한다.
     * <p>
     * springdoc의 {@code removeBrokenReferenceDefinitions}가 커스터마이저 실행 전에 원본 래퍼 스키마
     * ({@code ApiResponseXxxDto})를 제거해버리므로, 실제로는 이 이름 기반 추론이 payload를 복원하는 핵심 경로다.
     * 단건({@code ApiResponseProductResponse})뿐 아니라 컬렉션({@code ApiResponseListProductResponse})도
     * 배열 스키마로 올바르게 복원한다.
     */
    private Schema<?> inferPayloadSchema(String wrapperName, Map<String, Schema> componentSchemas) {
        String payloadName = null;
        if (wrapperName.startsWith("ApiResponse")) {
            payloadName = wrapperName.substring("ApiResponse".length());
        } else if (wrapperName.startsWith("CommonResponse")) {
            payloadName = wrapperName.substring("CommonResponse".length());
        }

        if (payloadName == null || payloadName.isBlank()) {
            return null;
        }

        // 단일 payload: {Name}Response 스키마가 그대로 존재하면 참조
        if (componentSchemas.containsKey(payloadName)) {
            return new Schema<>().$ref(REF_PREFIX + payloadName);
        }

        // 컬렉션 payload: "List{Element}" 형태면 접두사를 벗겨 요소를 배열로 감싼다.
        for (String prefix : COLLECTION_PREFIXES) {
            if (payloadName.startsWith(prefix)) {
                String elementName = payloadName.substring(prefix.length());
                if (!elementName.isBlank() && componentSchemas.containsKey(elementName)) {
                    ArraySchema array = new ArraySchema();
                    array.setItems(new Schema<>().$ref(REF_PREFIX + elementName));
                    return array;
                }
            }
        }
        return null;
    }

    private void removeUnreferencedDocumentationNoise(OpenAPI openApi, Map<String, Schema> schemas) {
        // path에서 시작해 실제 요청/응답이 참조하는 스키마만 남긴다.
        Set<String> reachable = reachableSchemasFromPaths(openApi, schemas);
        schemas.entrySet().removeIf(entry ->
                isDocumentationNoise(entry.getKey()) && !reachable.contains(entry.getKey()));
    }

    private Set<String> reachableSchemasFromPaths(OpenAPI openApi, Map<String, Schema> schemas) {
        Set<String> reachable = new HashSet<>();
        ArrayDeque<String> pending = new ArrayDeque<>();

        // OpenAPI paths에 직접 걸린 requestBody/response 스키마를 그래프 탐색의 시작점으로 삼는다.
        if (openApi.getPaths() != null) {
            openApi.getPaths().values().forEach(path ->
                    path.readOperations().forEach(operation -> collectOperationRefs(operation, pending)));
        }

        while (!pending.isEmpty()) {
            String name = pending.removeFirst();
            if (!reachable.add(name)) {
                continue;
            }

            // 문서 노이즈는 도달 여부만 기록하고, 그 내부 참조를 따라가며 불필요한 DTO를 되살리지는 않는다.
            Schema schema = schemas.get(name);
            if (schema != null && !isDocumentationNoise(name)) {
                collectSchemaRefs(schema, pending);
            }
        }

        return reachable;
    }

    private void collectOperationRefs(Operation operation, ArrayDeque<String> pending) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            operation.getRequestBody().getContent().values().forEach(mediaType ->
                    collectSchemaRefs(mediaType.getSchema(), pending));
        }

        ApiResponses responses = operation.getResponses();
        if (responses != null) {
            responses.values().forEach(response -> {
                Content content = response.getContent();
                if (content != null) {
                    content.values().forEach(mediaType -> collectSchemaRefs(mediaType.getSchema(), pending));
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void collectSchemaRefs(Schema<?> schema, ArrayDeque<String> pending) {
        if (schema == null) {
            return;
        }

        // $ref, 배열 item, object property, 조합 스키마(allOf/oneOf/anyOf)를 모두 따라간다.
        String refName = refName(schema);
        if (refName != null) {
            pending.add(refName);
        }

        if (schema instanceof ArraySchema arraySchema) {
            collectSchemaRefs(arraySchema.getItems(), pending);
        }

        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(property ->
                    collectSchemaRefs((Schema<?>) property, pending));
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> additionalSchema) {
            collectSchemaRefs(additionalSchema, pending);
        }

        if (schema.getAllOf() != null) {
            schema.getAllOf().forEach(child -> collectSchemaRefs((Schema<?>) child, pending));
        }
        if (schema.getOneOf() != null) {
            schema.getOneOf().forEach(child -> collectSchemaRefs((Schema<?>) child, pending));
        }
        if (schema.getAnyOf() != null) {
            schema.getAnyOf().forEach(child -> collectSchemaRefs((Schema<?>) child, pending));
        }
    }

    private boolean isDocumentationNoise(String name) {
        if (name == null) {
            return false;
        }
        // springdoc이 중간 산출물로 만든 래퍼/원본 DTO 이름은 실제 path에서 쓰이지 않으면 Schemas에서 숨긴다.
        return name.startsWith("ApiRequest")
                || name.startsWith("ApiResponse")
                || name.startsWith("CommonResponse")
                || name.endsWith("ReqDto")
                || name.endsWith("ResDto")
                || name.equals("FilterReqDto")
                || name.equals("PaginationReqDto")
                || name.equals("PaginationResDto")
                || name.equals("SortReqDto");
    }
}
