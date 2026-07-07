package com.bwg.channel.backend.systemsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.commoncode.controller.CommonCodeController;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.menu.controller.MenuController;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import com.bwg.channel.backend.systemsvc.referencedata.controller.ReferenceDataVersionController;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionReqDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기준정보 컨트롤러 POST 매핑 규칙 검증
 */
class SystemControllerMappingTests {

    @Test
    void commonCodeControllerUsesPostMappingsOnly() {
        assertPostOnly(CommonCodeController.class);
    }

    @Test
    void menuControllerUsesPostMappingsOnly() {
        assertPostOnly(MenuController.class);
    }

    @Test
    void referenceDataVersionControllerUsesPostMappingsOnly() {
        assertPostOnly(ReferenceDataVersionController.class);
    }

    @Test
    void commonCodeControllerRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("createCommonCodeGroup", ApiRequest.class),
                CommonCodeGroupReqDto.class
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("updateCommonCodeGroup", String.class, ApiRequest.class),
                CommonCodeGroupReqDto.class
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("getCommonCodeGroupDetails", ApiRequest.class),
                CommonCodeGroupReqDto.class
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("createCommonCode", String.class, ApiRequest.class),
                CommonCodeReqDto.class
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("updateCommonCode", String.class, String.class, ApiRequest.class),
                CommonCodeReqDto.class
        );
    }

    @Test
    void menuControllerRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertRequestBodyType(
                MenuController.class.getDeclaredMethod("createMenu", ApiRequest.class),
                MenuReqDto.class
        );
        assertRequestBodyType(
                MenuController.class.getDeclaredMethod("updateMenu", Long.class, ApiRequest.class),
                MenuReqDto.class
        );
        assertRequestBodyType(
                MenuController.class.getDeclaredMethod("saveRoleMenus", Long.class, ApiRequest.class),
                RoleMenuSaveReqDto.class
        );
    }

    @Test
    void referenceDataVersionControllerRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertRequestBodyType(
                ReferenceDataVersionController.class.getDeclaredMethod("getLatestReferenceDataVersions", ApiRequest.class),
                ReferenceDataVersionReqDto.class
        );
    }

    private void assertPostOnly(Class<?> controllerType) {
        for (Method method : controllerType.getDeclaredMethods()) {
            assertThat(method.isAnnotationPresent(GetMapping.class))
                    .as("%s.%s must not use GET", controllerType.getSimpleName(), method.getName())
                    .isFalse();
            assertThat(method.isAnnotationPresent(PutMapping.class))
                    .as("%s.%s must not use PUT", controllerType.getSimpleName(), method.getName())
                    .isFalse();
            assertThat(method.isAnnotationPresent(PostMapping.class))
                    .as("%s.%s must use POST", controllerType.getSimpleName(), method.getName())
                    .isTrue();
        }
    }

    private void assertRequestBodyType(Method method, Class<?> dataType) {
        Parameter bodyParameter = null;
        Type genericType = null;
        Parameter[] parameters = method.getParameters();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                bodyParameter = parameters[i];
                genericType = genericParameterTypes[i];
                break;
            }
        }

        assertThat(bodyParameter)
                .as("%s must have @RequestBody ApiRequest<%s>", method.getName(), dataType.getSimpleName())
                .isNotNull();
        assertThat(bodyParameter.getType()).isEqualTo(ApiRequest.class);
        assertThat(genericType).isInstanceOf(ParameterizedType.class);

        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        assertThat(parameterizedType.getRawType()).isEqualTo(ApiRequest.class);
        assertThat(parameterizedType.getActualTypeArguments()).containsExactly(dataType);
    }
}
