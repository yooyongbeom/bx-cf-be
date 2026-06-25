package com.bwg.channel.backend.typebridge.customizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

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
