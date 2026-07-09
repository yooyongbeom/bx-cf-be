package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 각 API 오퍼레이션의 requestBody 및 200 response 스키마 참조를 엔드포인트별 named 스키마로 교체한다.
 *
 * - requestBody  → {Name}{Endpoint}Request $ref
 * - 200 response → ApiResponse«{Name}Response» $ref
 */
@Component
public class TypeBridgeOperationCustomizer implements OperationCustomizer {

    /**
     * HandlerMethod의 요청/응답 DTO 메타데이터를 기준으로 오퍼레이션 스키마 참조를 교체한다.
     */
    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        String endpointId = extractEndpointId(handlerMethod);
        if (endpointId == null) return operation;

        // ── requestBody 교체 ─────────────────────────────────────────────
        for (MethodParameter param : handlerMethod.getMethodParameters()) {
            if (param.getParameterAnnotation(RequestBody.class) == null) continue;

            // ApiRequest<T> 또는 @ApiDto DTO를 찾아 전역 커스터마이저가 만든 Request 스키마명과 맞춘다.
            Class<?> paramType = resolveRequestApiDtoType(param.getGenericParameterType());
            if (paramType == null) continue;

            ApiDto apiDto = paramType.getAnnotation(ApiDto.class);
            if (apiDto == null) continue;
            if (apiDto.type() == ApiType.RESPONSE) continue;   // 응답 전용 DTO는 요청 바디로 쓰지 않음
            if (!Arrays.asList(apiDto.endpoints()).contains(endpointId)) continue;

            String baseName = resolveBaseName(paramType, apiDto);
            String schemaName = baseName + toPascalCase(endpointId) + "Request";
            replaceContentSchema(operation.getRequestBody() != null
                    ? operation.getRequestBody().getContent()
                    : null, schemaName, buildApiRequestExample(paramType, endpointId));
        }

        // ── 200 response 교체 ────────────────────────────────────────────
        replaceResponseSchema(operation, handlerMethod, endpointId);

        return operation;
    }

    private void replaceResponseSchema(Operation operation, HandlerMethod handlerMethod, String endpointId) {
        if (operation.getResponses() == null) return;
        ApiResponse response200 = operation.getResponses().get("200");
        if (response200 == null || response200.getContent() == null) return;

        // 반환 타입의 제네릭 인자에서 @ApiDto 확인 (예: ApiResponse<T>, ApiResponse<List<T>>)
        Type genericReturn = handlerMethod.getMethod().getGenericReturnType();
        if (!(genericReturn instanceof ParameterizedType pt)) return;

        Type[] typeArgs = pt.getActualTypeArguments();
        if (typeArgs.length == 0) return;

        Class<?> typeArg = resolveApiDtoType(typeArgs[0]);
        if (typeArg == null) return;
        ApiDto apiDto = typeArg.getAnnotation(ApiDto.class);
        if (apiDto == null) return;

        String dtoName = typeArg.getSimpleName();
        String responseName;
        if (apiDto.type() == ApiType.RESPONSE) {
            // 신규: 엔드포인트별 응답 스키마 (해당 엔드포인트가 등록돼 있어야 교체)
            if (!Arrays.asList(apiDto.endpoints()).contains(endpointId)) return;
            responseName = resolveBaseName(typeArg, apiDto) + toPascalCase(endpointId) + "Response";
        } else if (apiDto.type() == ApiType.LEGACY) {
            if (!apiDto.generateResponse()) return;
            responseName = resolveBaseName(typeArg, apiDto) + "Response";
        } else {
            return;   // REQUEST 타입이 반환 타입인 경우는 무시
        }

        // 현재 $ref에서 dtoName → responseName 으로 교체
        response200.getContent().forEach((mediaType, content) -> {
            if (content.getSchema() == null || content.getSchema().get$ref() == null) return;
            String currentRef = content.getSchema().get$ref();
            // springdoc이 만든 래퍼명은 유지하고 payload 부분의 DTO 이름만 교체한다.
            String newRef = currentRef.replace(dtoName, responseName);
            if (!newRef.equals(currentRef)) {
                Schema<Object> replaced = new Schema<>();
                replaced.set$ref(newRef);
                content.setSchema(replaced);
            }
        });
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────

    private Class<?> resolveApiDtoType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz.getAnnotation(ApiDto.class) != null ? clazz : null;
        }

        // List<@ApiDto> 같은 컬렉션 응답은 요소 타입까지 내려가서 DTO 메타데이터를 찾는다.
        if (type instanceof ParameterizedType pt) {
            Type rawType = pt.getRawType();
            if (rawType instanceof Class<?> rawClass
                    && Collection.class.isAssignableFrom(rawClass)
                    && pt.getActualTypeArguments().length > 0) {
                return resolveApiDtoType(pt.getActualTypeArguments()[0]);
            }
        }

        return null;
    }

    private Class<?> resolveRequestApiDtoType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz.getAnnotation(ApiDto.class) != null ? clazz : null;
        }

        // 공통 요청 래퍼 ApiRequest<T>는 실제 data 타입인 T를 기준으로 스키마를 교체한다.
        if (type instanceof ParameterizedType pt) {
            Type rawType = pt.getRawType();
            if (rawType == ApiRequest.class && pt.getActualTypeArguments().length > 0) {
                return resolveApiDtoType(pt.getActualTypeArguments()[0]);
            }
        }

        return null;
    }

    private Map<String, Object> buildApiRequestExample(Class<?> dataType, String endpoint) {
        Map<String, Object> request = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        Arrays.stream(dataType.getDeclaredFields()).forEach(field -> {
            var apiField = field.getAnnotation(com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField.class);
            if (apiField == null || apiField.hidden() || apiField.responseOnly()) return;
            if (Arrays.asList(apiField.exclude()).contains(endpoint)) return;
            boolean isRequired = Arrays.asList(apiField.required()).contains(endpoint);
            boolean isOptional = Arrays.asList(apiField.optional()).contains(endpoint);
            if (!isRequired && !isOptional) return;
            // @ApiField 예시가 있으면 우선 사용하고, 없으면 타입별 기본 예시로 문서 형태만 보장한다.
            data.put(field.getName(), !apiField.example().isEmpty() ? apiField.example() : defaultExample(field.getType()));
        });
        if (!data.isEmpty()) {
            request.put("data", data);
        }
        return request;
    }

    private Object defaultExample(Class<?> type) {
        if (int.class == type || Integer.class.isAssignableFrom(type)
                || long.class == type || Long.class.isAssignableFrom(type)) {
            return 0;
        }
        if (double.class == type || Double.class.isAssignableFrom(type)
                || float.class == type || Float.class.isAssignableFrom(type)
                || java.math.BigDecimal.class.isAssignableFrom(type)) {
            return 0;
        }
        if (boolean.class == type || Boolean.class.isAssignableFrom(type)) {
            return true;
        }
        return "string";
    }

    private void replaceContentSchema(io.swagger.v3.oas.models.media.Content content, String schemaName, Object example) {
        if (content == null) return;
        Schema<Object> ref = new Schema<>();
        ref.set$ref("#/components/schemas/" + schemaName);
        // JSON 외 media type이 있어도 같은 request schema/example을 일괄 적용한다.
        content.forEach((mediaType, mediaTypeObj) -> {
            mediaTypeObj.setSchema(ref);
            mediaTypeObj.setExample(example);
        });
    }

    private String extractEndpointId(HandlerMethod handlerMethod) {
        // 메서드 레벨 mapping path의 마지막 고정 segment를 endpoint id로 사용한다.
        PostMapping post = handlerMethod.getMethodAnnotation(PostMapping.class);
        if (post != null && post.value().length > 0)
            return toEndpointId(post.value()[0]);

        GetMapping get = handlerMethod.getMethodAnnotation(GetMapping.class);
        if (get != null && get.value().length > 0)
            return toEndpointId(get.value()[0]);

        PutMapping put = handlerMethod.getMethodAnnotation(PutMapping.class);
        if (put != null && put.value().length > 0)
            return toEndpointId(put.value()[0]);

        DeleteMapping del = handlerMethod.getMethodAnnotation(DeleteMapping.class);
        if (del != null && del.value().length > 0)
            return toEndpointId(del.value()[0]);

        RequestMapping req = handlerMethod.getMethodAnnotation(RequestMapping.class);
        if (req != null && req.value().length > 0)
            return toEndpointId(req.value()[0]);

        return null;
    }

    private String toEndpointId(String mappingPath) {
        String normalized = mappingPath.replaceFirst("^/", "").replaceFirst("/$", "");
        if (normalized.isBlank()) return null;

        String[] segments = normalized.split("/");
        // /users/{id}/detail 처럼 path variable이 섞이면 뒤에서부터 의미 있는 segment를 찾는다.
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];
            if (!segment.isBlank() && !isPathVariable(segment)) {
                return segment;
            }
        }
        return normalized;
    }

    private boolean isPathVariable(String segment) {
        return segment.startsWith("{") && segment.endsWith("}");
    }

    private String resolveBaseName(Class<?> clazz, ApiDto apiDto) {
        return apiDto.name().isEmpty()
                ? clazz.getSimpleName().replaceAll("(Req|Res|Request|Response)?Dto$", "")
                : apiDto.name();
    }

    private String toPascalCase(String hyphenated) {
        return Arrays.stream(hyphenated.split("[-_]"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining());
    }
}
