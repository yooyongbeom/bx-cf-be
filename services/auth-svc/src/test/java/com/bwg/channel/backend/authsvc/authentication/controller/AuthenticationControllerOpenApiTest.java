package com.bwg.channel.backend.authsvc.authentication.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.openapi.OpenApiSupport;
import com.bwg.channel.backend.common.openapi.customizer.ResponseWrapperSchemaCustomizer;
import com.bwg.channel.backend.common.openapi.typebridge.customizer.TypeBridgeOpenApiCustomizer;
import com.bwg.channel.backend.common.openapi.typebridge.customizer.TypeBridgeOperationCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationControllerOpenApiTest {

    @Test
    void logoutRequiresBearerAuthInOpenApi() throws NoSuchMethodException {
        Method logout = AuthenticationController.class.getDeclaredMethod(
                "logout",
                String.class,
                String.class,
                jakarta.servlet.http.HttpServletResponse.class
        );

        assertThat(logout.getAnnotationsByType(SecurityRequirement.class))
                .extracting(SecurityRequirement::name)
                .contains(OpenApiSupport.BEARER_SCHEME);
    }

    @Test
    void loginRequestBodyUsesEndpointOnlyDataWrapper() throws NoSuchMethodException {
        Operation operation = new Operation()
                .requestBody(new RequestBody().content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiRequestLoginReqDto")))));
        HandlerMethod handlerMethod = new HandlerMethod(
                new AuthenticationController(null, null),
                AuthenticationController.class.getDeclaredMethod("login", ApiRequest.class, HttpServletResponse.class));

        new TypeBridgeOperationCustomizer().customize(operation, handlerMethod);

        MediaType mediaType = operation.getRequestBody().getContent().get("application/json");
        assertThat(mediaType.getSchema().get$ref()).isEqualTo("#/components/schemas/AuthLoginRequest");
        assertThat(mediaType.getExample()).isEqualTo(Map.of(
                "data", Map.of(
                        "usrId", "hong.gildong",
                        "usrPwd", "string"
                )
        ));
    }

    @Test
    void authLoginRequestSchemaShowsOnlyLoginDataAndHidesRawModels() {
        OpenAPI openApi = new OpenAPI();
        new TypeBridgeOpenApiCustomizer().customise(openApi);
        openApi.schema("ApiRequestLoginReqDto", new Schema<>().type("object"));
        openApi.schema("LoginReqDto", new Schema<>().type("object"));
        openApi.schema("FilterReqDto", new Schema<>().type("object"));
        openApi.schema("PaginationReqDto", new Schema<>().type("object"));
        openApi.schema("SortReqDto", new Schema<>().type("object"));
        openApi.setPaths(new Paths().addPathItem("/login", new PathItem().post(new Operation()
                .requestBody(new RequestBody().content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/AuthLoginRequest"))))))));

        new ResponseWrapperSchemaCustomizer().customise(openApi);

        Schema<?> authLoginRequest = openApi.getComponents().getSchemas().get("AuthLoginRequest");
        assertThat(authLoginRequest.getProperties().keySet()).containsExactly("data");
        Schema<?> data = (Schema<?>) authLoginRequest.getProperties().get("data");
        assertThat(data.getProperties().keySet()).containsExactly("usrId", "usrPwd");
        assertThat(openApi.getComponents().getSchemas())
                .doesNotContainKeys("ApiRequestLoginReqDto", "LoginReqDto",
                        "FilterReqDto", "PaginationReqDto", "SortReqDto");
    }
}
