package com.bwg.channel.backend.typebridge.customizer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.method.HandlerMethod;

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

    static class NestedController {
        @PostMapping("/groups/{groupCd}/codes/create")
        void create() {
        }
    }
}
