package com.bwg.channel.backend.systemsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.bwg.channel.backend.systemsvc.commoncode.controller.CommonCodeController;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.menu.controller.MenuController;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import com.bwg.channel.backend.systemsvc.mci.controller.MciManagementController;
import com.bwg.channel.backend.systemsvc.mci.dto.MciTransactionReqDto;
import com.bwg.channel.backend.systemsvc.referencedata.controller.ReferenceDataVersionController;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionReqDto;
import io.swagger.v3.oas.annotations.Hidden;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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
    void menuDetailUsesPathVariablePostMapping() throws NoSuchMethodException {
        Method method = MenuController.class.getDeclaredMethod("getMenu", Long.class);
        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertThat(mapping.value()).containsExactly("/{menuId}/detail");
        assertThat(method.getParameters()[0].isAnnotationPresent(PathVariable.class)).isTrue();
        assertThat(method.getParameters()[0].isAnnotationPresent(RequestBody.class)).isFalse();
    }

    @Test
    void menuDeleteUsesPathVariablePostMapping() throws NoSuchMethodException {
        Method method = MenuController.class.getDeclaredMethod("deleteMenu", Long.class, String.class);
        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertThat(mapping.value()).containsExactly("/{menuId}/delete");
        assertThat(method.getParameters()[0].isAnnotationPresent(PathVariable.class)).isTrue();
        assertThat(method.getParameters()[0].isAnnotationPresent(RequestBody.class)).isFalse();
        assertInternalAuthUserHeader(method);
    }

    @Test
    void referenceDataVersionControllerUsesPostMappingsOnly() {
        assertPostOnly(ReferenceDataVersionController.class);
    }

    @Test
    void mciManagementControllerUsesPostMappingsOnly() {
        assertPostOnly(MciManagementController.class);
    }

    @Test
    void mciManagementControllerIsHiddenFromSwaggerUntilPersistenceIsImplemented() {
        assertThat(MciManagementController.class.isAnnotationPresent(Hidden.class))
                .as("MCI 관리 API는 아직 YAML 설계 단계라 Swagger에 실제 구현 API처럼 노출하면 안 된다")
                .isTrue();
    }

    @Test
    void commonCodeControllerRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("createCommonCodeGroup", ApiRequest.class, String.class),
                CommonCodeGroupReqDto.class
        );
        assertInternalAuthUserHeader(
                CommonCodeController.class.getDeclaredMethod("createCommonCodeGroup", ApiRequest.class, String.class)
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("updateCommonCodeGroup", String.class, ApiRequest.class, String.class),
                CommonCodeGroupReqDto.class
        );
        assertInternalAuthUserHeader(
                CommonCodeController.class.getDeclaredMethod("updateCommonCodeGroup", String.class, ApiRequest.class, String.class)
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("getCommonCodeGroupDetails", ApiRequest.class),
                CommonCodeGroupReqDto.class
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("createCommonCode", String.class, ApiRequest.class, String.class),
                CommonCodeReqDto.class
        );
        assertInternalAuthUserHeader(
                CommonCodeController.class.getDeclaredMethod("createCommonCode", String.class, ApiRequest.class, String.class)
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("updateCommonCode", String.class, String.class, ApiRequest.class, String.class),
                CommonCodeReqDto.class
        );
        assertInternalAuthUserHeader(
                CommonCodeController.class.getDeclaredMethod("updateCommonCode", String.class, String.class, ApiRequest.class, String.class)
        );
        assertRequestBodyType(
                CommonCodeController.class.getDeclaredMethod("replaceCommonCodes", String.class, ApiRequest.class, String.class),
                CommonCodeReplaceReqDto.class
        );
        assertInternalAuthUserHeader(
                CommonCodeController.class.getDeclaredMethod("replaceCommonCodes", String.class, ApiRequest.class, String.class)
        );
    }

    @Test
    void commonCodeCombinedListUsesRootListPostMapping() throws NoSuchMethodException {
        Method method = CommonCodeController.class.getDeclaredMethod(
                "getCommonCodeGroupDetails",
                ApiRequest.class
        );
        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertThat(mapping.value()).containsExactly("/list");
    }

    @Test
    void commonCodeReplaceUsesRootGroupPostMapping() throws NoSuchMethodException {
        Method method = CommonCodeController.class.getDeclaredMethod(
                "replaceCommonCodes",
                String.class,
                ApiRequest.class,
                String.class
        );
        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertThat(mapping.value()).containsExactly("/{groupCd}/replace");
        assertThat(method.getParameters()[0].isAnnotationPresent(PathVariable.class)).isTrue();
    }

    @Test
    void menuControllerRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertRequestBodyType(
                MenuController.class.getDeclaredMethod("createMenu", ApiRequest.class, String.class),
                MenuCreateReqDto.class
        );
        assertInternalAuthUserHeader(
                MenuController.class.getDeclaredMethod("createMenu", ApiRequest.class, String.class)
        );
        assertRequestBodyType(
                MenuController.class.getDeclaredMethod("updateMenu", Long.class, ApiRequest.class, String.class),
                MenuUpdateReqDto.class
        );
        assertInternalAuthUserHeader(
                MenuController.class.getDeclaredMethod("updateMenu", Long.class, ApiRequest.class, String.class)
        );
        assertRequestBodyType(
                MenuController.class.getDeclaredMethod("saveRoleMenus", Long.class, ApiRequest.class, String.class),
                RoleMenuSaveReqDto.class
        );
        assertInternalAuthUserHeader(
                MenuController.class.getDeclaredMethod("saveRoleMenus", Long.class, ApiRequest.class, String.class)
        );
    }

    @Test
    void referenceDataVersionControllerRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertRequestBodyType(
                ReferenceDataVersionController.class.getDeclaredMethod("getLatestReferenceDataVersions", ApiRequest.class),
                ReferenceDataVersionReqDto.class
        );
    }

    @Test
    void mciManagementControllerRequestBodiesUseApiRequestWrapper() throws NoSuchMethodException {
        assertRequestBodyType(
                MciManagementController.class.getDeclaredMethod("getMciTransactions", ApiRequest.class),
                MciTransactionReqDto.class
        );
        assertRequestBodyType(
                MciManagementController.class.getDeclaredMethod("getMciTransaction", String.class, ApiRequest.class),
                MciTransactionReqDto.class
        );
        assertRequestBodyType(
                MciManagementController.class.getDeclaredMethod("saveMciTransaction", ApiRequest.class),
                MciTransactionReqDto.class
        );
        assertRequestBodyType(
                MciManagementController.class.getDeclaredMethod("updateMciTransaction", String.class, ApiRequest.class),
                MciTransactionReqDto.class
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
            assertThat(method.isAnnotationPresent(DeleteMapping.class))
                    .as("%s.%s must not use DELETE", controllerType.getSimpleName(), method.getName())
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

    private void assertInternalAuthUserHeader(Method method) {
        assertThat(method.getParameters())
                .filteredOn(parameter -> parameter.isAnnotationPresent(RequestHeader.class))
                .singleElement()
                .satisfies(parameter -> assertThat(parameter.getAnnotation(RequestHeader.class).value())
                        .isEqualTo(InternalAuthHeaders.USER));
    }
}
