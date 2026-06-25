package com.bwg.channel.backend.typebridge.customizer;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
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

            Class<?> paramType = param.getParameterType();
            ApiDto apiDto = paramType.getAnnotation(ApiDto.class);
            if (apiDto == null) continue;
            if (!Arrays.asList(apiDto.endpoints()).contains(endpointId)) continue;

            String baseName = resolveBaseName(paramType, apiDto);
            String schemaName = baseName + toPascalCase(endpointId) + "Request";
            replaceContentSchema(operation.getRequestBody() != null
                    ? operation.getRequestBody().getContent()
                    : null, schemaName);
        }

        // ── 200 response 교체 ────────────────────────────────────────────
        replaceResponseSchema(operation, handlerMethod);

        return operation;
    }

    private void replaceResponseSchema(Operation operation, HandlerMethod handlerMethod) {
        if (operation.getResponses() == null) return;
        ApiResponse response200 = operation.getResponses().get("200");
        if (response200 == null || response200.getContent() == null) return;

        // 반환 타입의 제네릭 인자에서 @ApiDto 확인
        Type genericReturn = handlerMethod.getMethod().getGenericReturnType();
        if (!(genericReturn instanceof ParameterizedType pt)) return;

        Type[] typeArgs = pt.getActualTypeArguments();
        if (typeArgs.length == 0) return;

        Class<?> typeArg = resolveApiDtoType(typeArgs[0]);
        if (typeArg == null) return;
        ApiDto apiDto = typeArg.getAnnotation(ApiDto.class);
        if (apiDto == null || !apiDto.generateResponse()) return;

        String dtoName      = typeArg.getSimpleName();
        String responseName = resolveBaseName(typeArg, apiDto) + "Response";

        // 현재 $ref에서 dtoName → responseName 으로 교체
        response200.getContent().forEach((mediaType, content) -> {
            if (content.getSchema() == null || content.getSchema().get$ref() == null) return;
            String currentRef = content.getSchema().get$ref();
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

    private void replaceContentSchema(io.swagger.v3.oas.models.media.Content content, String schemaName) {
        if (content == null) return;
        Schema<Object> ref = new Schema<>();
        ref.set$ref("#/components/schemas/" + schemaName);
        content.forEach((mediaType, mediaTypeObj) -> mediaTypeObj.setSchema(ref));
    }

    private String extractEndpointId(HandlerMethod handlerMethod) {
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
                ? clazz.getSimpleName().replaceAll("Dto$", "")
                : apiDto.name();
    }

    private String toPascalCase(String hyphenated) {
        return Arrays.stream(hyphenated.split("[-_]"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining());
    }
}
