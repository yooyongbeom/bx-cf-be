package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.openapi.customizer.ResponseWrapperSchemaCustomizer;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TypeBridgeOpenApiCustomizerTests {

    @Test
    void apiFieldDescriptionIsAppliedToGeneratedSchemas() {
        OpenAPI openApi = new OpenAPI();

        new TypeBridgeOpenApiCustomizer().customise(openApi);

        Schema<?> schema = openApi.getComponents().getSchemas().get("TypeBridgeTestCreateRequest");
        Schema<?> name = (Schema<?>) schema.getProperties().get("name");

        assertThat(name.getDescription()).isEqualTo("테스트 이름");
    }

    @Test
    void offsetDateTimeIsGeneratedAsDateTimeString() {
        OpenAPI openApi = new OpenAPI();

        new TypeBridgeOpenApiCustomizer().customise(openApi);

        Schema<?> schema = openApi.getComponents().getSchemas().get("DateTimeDetailResponse");
        Schema<?> changedAt = (Schema<?>) schema.getProperties().get("changedAt");

        assertThat(changedAt.getType()).isEqualTo("string");
        assertThat(changedAt.getFormat()).isEqualTo("date-time");
    }

    @Test
    void nestedMappingUsesTerminalEndpointId() throws NoSuchMethodException {
        TypeBridgeOperationCustomizer customizer = new TypeBridgeOperationCustomizer();
        HandlerMethod handlerMethod = new HandlerMethod(
                new NestedController(),
                NestedController.class.getDeclaredMethod("create")
        );

        String endpointId = ReflectionTestUtils.invokeMethod(customizer, "extractEndpointId", handlerMethod);

        assertThat(endpointId).isEqualTo("create");
    }

    @Test
    void listResponseUsesApiFieldGeneratedResponseSchema() throws NoSuchMethodException {
        TypeBridgeOperationCustomizer customizer = new TypeBridgeOperationCustomizer();
        Operation operation = new Operation()
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse()
                        .content(new Content().addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ApiResponseListTypeBridgeTestDto"))))));
        HandlerMethod handlerMethod = new HandlerMethod(
                new ListResponseController(),
                ListResponseController.class.getDeclaredMethod("list")
        );

        customizer.customize(operation, handlerMethod);

        Schema<?> schema = operation.getResponses()
                .get("200")
                .getContent()
                .get("application/json")
                .getSchema();
        assertThat(schema.get$ref()).isEqualTo("#/components/schemas/ApiResponseListTypeBridgeTestResponse");
    }

    @Test
    void apiResponseVariantReplacesArrayPayloadItemSchema() {
        OpenAPI openApi = new OpenAPI();
        Schema<Object> wrapper = new Schema<>();
        Schema<Object> payload = new ArraySchema()
                .items(new Schema<>().$ref("#/components/schemas/TypeBridgeTestDto"));
        wrapper.addProperty("payload", payload);
        openApi.schema("ApiResponseListTypeBridgeTestDto", wrapper);

        new TypeBridgeOpenApiCustomizer().customise(openApi);

        Schema<?> variant = openApi.getComponents().getSchemas().get("ApiResponseListTypeBridgeTestResponse");
        ArraySchema variantPayload = (ArraySchema) variant.getProperties().get("payload");
        assertThat(variantPayload.getItems().get$ref()).isEqualTo("#/components/schemas/TypeBridgeTestResponse");
    }

    /**
     * 재현 테스트: ResponseWrapperSchemaCustomizer가 TypeBridge 글로벌 커스터마이저보다
     * 먼저 실행되면(=순서 보장 실패), payload 스키마/변형 래퍼가 아직 없어서
     * 응답 스키마에서 payload 프로퍼티가 통째로 누락된다(=응답 example에 payload 안 나옴).
     */
    @Test
    void wrongOrderDropsResponsePayload() throws NoSuchMethodException {
        OpenAPI openApi = springdocLikeListOpenApi();
        applyOperationCustomizer(openApi);

        // 잘못된 순서: 래퍼 정리를 먼저 실행
        new ResponseWrapperSchemaCustomizer().customise(openApi);
        new TypeBridgeOpenApiCustomizer().customise(openApi);

        Schema<?> responseSchema = responseSchemaOf(openApi);
        assertThat(responseSchema.getProperties()).doesNotContainKey("payload");
    }

    /**
     * 올바른 순서(TypeBridge 먼저)면 payload가 유지되고, 배열 payload의 item이
     * 생성된 Response 스키마를 참조한다.
     */
    @Test
    void correctOrderKeepsResponsePayload() throws NoSuchMethodException {
        OpenAPI openApi = springdocLikeListOpenApi();

        // 올바른 순서: TypeBridge 글로벌 → operation → wrapper 정리
        new TypeBridgeOpenApiCustomizer().customise(openApi);
        applyOperationCustomizer(openApi);
        new ResponseWrapperSchemaCustomizer().customise(openApi);

        Schema<?> responseSchema = responseSchemaOf(openApi);
        assertThat(responseSchema.getProperties()).containsKey("payload");
        Schema<?> payload = (Schema<?>) responseSchema.getProperties().get("payload");
        assertThat(payload).isInstanceOf(ArraySchema.class);
        assertThat(((ArraySchema) payload).getItems().get$ref())
                .isEqualTo("#/components/schemas/TypeBridgeTestResponse");
    }

    /**
     * 순서 보장: TypeBridgeOpenApiCustomizer가 ResponseWrapperSchemaCustomizer보다
     * 먼저 정렬되어야 한다(@Order로 강제).
     */
    @Test
    void typeBridgeCustomizerIsOrderedBeforeResponseWrapper() {
        List<OpenApiCustomizer> customizers = new ArrayList<>(List.of(
                new ResponseWrapperSchemaCustomizer(),
                new TypeBridgeOpenApiCustomizer()));
        AnnotationAwareOrderComparator.sort(customizers);

        assertThat(customizers.get(0)).isInstanceOf(TypeBridgeOpenApiCustomizer.class);
        assertThat(customizers.get(1)).isInstanceOf(ResponseWrapperSchemaCustomizer.class);
    }

    /**
     * 회귀 테스트: springdoc의 removeBrokenReferenceDefinitions가 원본 래퍼/DTO 스키마를 모두 제거한 뒤
     * (components에는 생성된 {Name}Response 스키마만 남고, 응답은 ApiResponse{Name}Response / ApiResponseList{Name}Response
     * 를 참조) ResponseWrapperSchemaCustomizer가 payload를 이름 기반으로 복원해야 한다.
     * 단건은 객체 참조, 목록은 배열로 복원되어야 한다.
     */
    @Test
    void inferPayloadRestoresListAsArrayAfterBrokenRefRemoval() {
        OpenAPI openApi = new OpenAPI();
        // removeBroken 이후 상태: 래퍼/DTO는 사라지고 Response 스키마만 남음
        openApi.schema("TypeBridgeTestResponse", new Schema<>().type("object"));

        openApi.setPaths(new Paths()
                .addPathItem("/list", new PathItem().post(operationRef("ApiResponseListTypeBridgeTestResponse")))
                .addPathItem("/get", new PathItem().post(operationRef("ApiResponseTypeBridgeTestResponse"))));

        new ResponseWrapperSchemaCustomizer().customise(openApi);

        Schema<?> listPayload = (Schema<?>) responseSchemaOfPath(openApi, "/list").getProperties().get("payload");
        assertThat(listPayload).isInstanceOf(ArraySchema.class);
        assertThat(((ArraySchema) listPayload).getItems().get$ref())
                .isEqualTo("#/components/schemas/TypeBridgeTestResponse");

        Schema<?> getPayload = (Schema<?>) responseSchemaOfPath(openApi, "/get").getProperties().get("payload");
        assertThat(getPayload.get$ref()).isEqualTo("#/components/schemas/TypeBridgeTestResponse");
    }

    private Operation operationRef(String wrapperName) {
        return new Operation().responses(new ApiResponses().addApiResponse("200", new ApiResponse()
                .content(new Content().addMediaType("*/*", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/" + wrapperName))))));
    }

    private Schema<?> responseSchemaOfPath(OpenAPI openApi, String path) {
        return openApi.getPaths().get(path).getPost()
                .getResponses().get("200").getContent().get("*/*").getSchema();
    }

    /** springdoc이 ApiResponse«List«TypeBridgeTestDto»» 를 만든 직후 상태를 흉내낸 OpenAPI */
    private OpenAPI springdocLikeListOpenApi() {
        Schema<Object> wrapper = new Schema<>();
        wrapper.setType("object");
        wrapper.addProperty("payload", new ArraySchema()
                .items(new Schema<>().$ref("#/components/schemas/TypeBridgeTestDto")));

        OpenAPI openApi = new OpenAPI();
        openApi.schema("ApiResponseListTypeBridgeTestDto", wrapper);
        openApi.schema("TypeBridgeTestDto", new Schema<>().type("object"));

        Operation operation = new Operation()
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse()
                        .content(new Content().addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ApiResponseListTypeBridgeTestDto"))))));
        openApi.setPaths(new Paths().addPathItem("/list", new PathItem().post(operation)));
        return openApi;
    }

    private void applyOperationCustomizer(OpenAPI openApi) throws NoSuchMethodException {
        Operation operation = openApi.getPaths().get("/list").getPost();
        HandlerMethod handlerMethod = new HandlerMethod(
                new ListResponseController(),
                ListResponseController.class.getDeclaredMethod("list"));
        new TypeBridgeOperationCustomizer().customize(operation, handlerMethod);
    }

    private Schema<?> responseSchemaOf(OpenAPI openApi) {
        return openApi.getPaths().get("/list").getPost()
                .getResponses().get("200")
                .getContent().get("application/json").getSchema();
    }

    // ── 신규: 요청/응답 DTO 분리 + @ApiDto(type=...) ──────────────────────────

    /** RESPONSE 타입 DTO는 엔드포인트별 {Name}{Endpoint}Response 스키마를 필드/필수 다르게 생성한다. */
    @Test
    void responseTypeGeneratesPerEndpointResponseSchemas() {
        OpenAPI openApi = new OpenAPI();
        new TypeBridgeOpenApiCustomizer().customise(openApi);
        var schemas = openApi.getComponents().getSchemas();

        Schema<?> listRes = schemas.get("SampleListResponse");
        assertThat(listRes.getProperties().keySet()).containsExactlyInAnyOrder("id", "value");
        assertThat(listRes.getRequired()).containsExactly("id");                 // value는 list에서 선택

        Schema<?> detailRes = schemas.get("SampleDetailResponse");
        assertThat(detailRes.getProperties().keySet()).containsExactlyInAnyOrder("id", "detailDesc", "value");
        assertThat(detailRes.getRequired()).containsExactlyInAnyOrder("id", "detailDesc", "value");
    }

    /** REQUEST 타입 DTO는 엔드포인트별 {Name}{Endpoint}Request 스키마를 생성한다. */
    @Test
    void requestTypeGeneratesApiRequestWrapperSchema() {
        OpenAPI openApi = new OpenAPI();
        new TypeBridgeOpenApiCustomizer().customise(openApi);

        Schema<?> listReq = openApi.getComponents().getSchemas().get("SampleListRequest");
        assertThat(listReq.getProperties().keySet())
                .containsExactly("pagination", "filter", "sort", "data");
        Schema<?> data = (Schema<?>) listReq.getProperties().get("data");
        assertThat(data.getProperties().keySet()).containsExactly("name");
    }

    @Test
    void nonListApiRequestWrapperOnlyShowsEndpointDataAndExample() {
        OpenAPI openApi = new OpenAPI();
        new TypeBridgeOpenApiCustomizer().customise(openApi);

        Schema<?> loginReq = openApi.getComponents().getSchemas().get("WrappedLoginRequest");

        assertThat(loginReq.getProperties().keySet()).containsExactly("data");
        Schema<?> data = (Schema<?>) loginReq.getProperties().get("data");
        assertThat(data.getProperties().keySet()).containsExactly("usrId", "usrPwd");
        assertThat(loginReq.getExample()).isEqualTo(Map.of(
                "data", Map.of(
                        "usrId", "sample-user",
                        "usrPwd", "string"
                )
        ));
    }

    @Test
    void operationCustomizerUsesApiRequestGenericForRequestBodySchema() throws NoSuchMethodException {
        TypeBridgeOperationCustomizer customizer = new TypeBridgeOperationCustomizer();
        Operation operation = new Operation()
                .requestBody(new RequestBody().content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiRequestWrappedReqDto")))));
        HandlerMethod handlerMethod = new HandlerMethod(
                new WrappedReqController(),
                WrappedReqController.class.getDeclaredMethod("login", ApiRequest.class));

        customizer.customize(operation, handlerMethod);

        MediaType mediaType = operation.getRequestBody().getContent().get("application/json");
        assertThat(mediaType.getSchema().get$ref()).isEqualTo("#/components/schemas/WrappedLoginRequest");
        assertThat(mediaType.getExample()).isEqualTo(Map.of(
                "data", Map.of(
                        "usrId", "sample-user",
                        "usrPwd", "string"
                )
        ));
    }

    @Test
    void operationCustomizerPreservesNestedCollectionShapeInRequestExample() throws NoSuchMethodException {
        TypeBridgeOperationCustomizer customizer = new TypeBridgeOperationCustomizer();
        Operation operation = new Operation()
                .requestBody(new RequestBody().content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiRequestCollectionReqDto")))));
        HandlerMethod handlerMethod = new HandlerMethod(
                new CollectionReqController(),
                CollectionReqController.class.getDeclaredMethod("create", ApiRequest.class));

        customizer.customize(operation, handlerMethod);

        MediaType mediaType = operation.getRequestBody().getContent().get("application/json");
        assertThat(mediaType.getSchema().get$ref()).isEqualTo("#/components/schemas/CollectionCreateRequest");
        assertThat(mediaType.getExample()).isEqualTo(Map.of(
                "data", Map.of(
                        "groupCd", "USE_YN",
                        "codes", List.of(Map.of("code", "Y"))
                )
        ));
    }

    @Test
    void operationCustomizerIncludesInheritedFieldsInRequestExample() throws NoSuchMethodException {
        TypeBridgeOperationCustomizer customizer = new TypeBridgeOperationCustomizer();
        Operation operation = new Operation()
                .requestBody(new RequestBody().content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiRequestInheritedReqDto")))));
        HandlerMethod handlerMethod = new HandlerMethod(
                new InheritedReqController(),
                InheritedReqController.class.getDeclaredMethod("create", ApiRequest.class));

        customizer.customize(operation, handlerMethod);

        MediaType mediaType = operation.getRequestBody().getContent().get("application/json");
        assertThat(mediaType.getSchema().get$ref()).isEqualTo("#/components/schemas/InheritedCreateRequest");
        assertThat(mediaType.getExample()).isEqualTo(Map.of(
                "data", Map.of(
                        "baseValue", "base-value",
                        "childValue", "child-value"
                )
        ));
    }

    @Test
    void responseWrapperCleanupHidesUnreferencedRawAndCommonSchemas() {
        OpenAPI openApi = new OpenAPI();
        openApi.schema("ApiRequestWrappedReqDto", new Schema<>().type("object"));
        openApi.schema("WrappedReqDto", new Schema<>().type("object"));
        openApi.schema("FilterReqDto", new Schema<>().type("object"));
        openApi.schema("PaginationReqDto", new Schema<>().type("object"));
        openApi.schema("SortReqDto", new Schema<>().type("object"));
        openApi.schema("WrappedLoginRequest", new Schema<>().type("object"));
        openApi.setPaths(new Paths().addPathItem("/login", new PathItem().post(new Operation()
                .requestBody(new RequestBody().content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/WrappedLoginRequest"))))))));

        new ResponseWrapperSchemaCustomizer().customise(openApi);

        assertThat(openApi.getComponents().getSchemas())
                .containsKey("WrappedLoginRequest")
                .doesNotContainKeys("ApiRequestWrappedReqDto", "WrappedReqDto",
                        "FilterReqDto", "PaginationReqDto", "SortReqDto");
    }

    /** OperationCustomizer는 RESPONSE 타입 반환에 대해 엔드포인트별 응답 스키마를 참조하도록 교체한다. */
    @Test
    void operationCustomizerPicksPerEndpointResponseSchema() throws NoSuchMethodException {
        Operation operation = new Operation()
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse()
                        .content(new Content().addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ApiResponseSampleResDto"))))));
        HandlerMethod handlerMethod = new HandlerMethod(
                new SampleResController(),
                SampleResController.class.getDeclaredMethod("detail"));

        new TypeBridgeOperationCustomizer().customize(operation, handlerMethod);

        Schema<?> schema = operation.getResponses().get("200")
                .getContent().get("application/json").getSchema();
        assertThat(schema.get$ref()).isEqualTo("#/components/schemas/ApiResponseSampleDetailResponse");
    }

    static class SampleResController {
        @PostMapping("/detail/{id}")
        com.bwg.channel.backend.common.domain.dto.ApiResponse<SampleResDto> detail() {
            return null;
        }
    }

    static class WrappedReqController {
        @PostMapping("/login")
        void login(@org.springframework.web.bind.annotation.RequestBody ApiRequest<WrappedReqDto> req) {
        }
    }

    static class CollectionReqController {
        @PostMapping("/create")
        void create(@org.springframework.web.bind.annotation.RequestBody ApiRequest<CollectionReqDto> req) {
        }
    }

    static class InheritedReqController {
        @PostMapping("/create")
        void create(@org.springframework.web.bind.annotation.RequestBody ApiRequest<InheritedReqDto> req) {
        }
    }

    static class InheritedBaseReqDto {

        @ApiField(description = "공통 값", example = "base-value", required = {"create"})
        private String baseValue;
    }

    @ApiDto(type = ApiType.REQUEST, name = "Inherited", endpoints = {"create"})
    static class InheritedReqDto extends InheritedBaseReqDto {

        @ApiField(description = "상세 값", example = "child-value", required = {"create"})
        private String childValue;
    }

    static class NestedController {
        @PostMapping("/groups/{groupCd}/codes/create")
        void create() {
        }
    }

    static class ListResponseController {
        @PostMapping("/list")
        com.bwg.channel.backend.common.domain.dto.ApiResponse<List<TypeBridgeTestDto>> list() {
            return null;
        }
    }
}
