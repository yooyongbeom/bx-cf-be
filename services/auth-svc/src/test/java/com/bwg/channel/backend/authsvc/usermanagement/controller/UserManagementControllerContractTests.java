package com.bwg.channel.backend.authsvc.usermanagement.controller;

import com.bwg.channel.backend.authsvc.usermanagement.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserUpdateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.service.UserManagementService;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserManagementControllerContractTests {

    @Test
    void controllerUsesUsersBasePathAndPostOnlyEndpoints() {
        assertThat(UserManagementController.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/users");
        assertThat(UserManagementController.class.getDeclaredMethods())
                .allSatisfy(method -> assertThat(method.getAnnotation(PostMapping.class))
                        .as(method.getName())
                        .isNotNull());
    }

    @Test
    void endpointsFollowCrudPathConvention() throws Exception {
        assertPath("getUsers", new Class<?>[]{String.class}, "/list");
        assertPath("getUser", new Class<?>[]{String.class, String.class}, "/{userId}/detail");
        assertPath("createUser",
                new Class<?>[]{ApiRequest.class, String.class, String.class}, "/create");
        assertPath("updateUser",
                new Class<?>[]{String.class, ApiRequest.class, String.class, String.class},
                "/{userId}/update");
        assertPath("deleteUser",
                new Class<?>[]{String.class, String.class, String.class}, "/{userId}/delete");
    }

    @Test
    void everyEndpointBindsOptionalGatewayRolesHeader() {
        for (Method method : UserManagementController.class.getDeclaredMethods()) {
            assertThat(method.getParameters())
                    .filteredOn(parameter -> parameter.isAnnotationPresent(RequestHeader.class))
                    .anySatisfy(parameter ->
                            assertOptionalRequestHeader(parameter, InternalAuthHeaders.ROLES));
        }
    }

    @Test
    void mutationEndpointsBindGatewayActorHeaderAndRequestBodies() throws Exception {
        Method createUser = UserManagementController.class.getDeclaredMethod(
                "createUser", ApiRequest.class, String.class, String.class
        );
        Method updateUser = UserManagementController.class.getDeclaredMethod(
                "updateUser", String.class, ApiRequest.class, String.class, String.class
        );
        Method deleteUser = UserManagementController.class.getDeclaredMethod(
                "deleteUser", String.class, String.class, String.class
        );

        // 변경 요청은 Gateway가 주입한 작업자 헤더와 요청 본문을 각각의 서비스 인자 위치에 바인딩한다.
        assertOptionalRequestHeader(createUser.getParameters()[1], InternalAuthHeaders.USER);
        assertRequestBody(createUser.getParameters()[0]);
        assertOptionalRequestHeader(updateUser.getParameters()[2], InternalAuthHeaders.USER);
        assertRequestBody(updateUser.getParameters()[1]);
        assertOptionalRequestHeader(deleteUser.getParameters()[1], InternalAuthHeaders.USER);
    }

    @Test
    void userTargetEndpointsBindUserIdPathVariable() throws Exception {
        Method getUser = UserManagementController.class.getDeclaredMethod(
                "getUser", String.class, String.class
        );
        Method updateUser = UserManagementController.class.getDeclaredMethod(
                "updateUser", String.class, ApiRequest.class, String.class, String.class
        );
        Method deleteUser = UserManagementController.class.getDeclaredMethod(
                "deleteUser", String.class, String.class, String.class
        );

        assertPathVariable(getUser.getParameters()[0]);
        assertPathVariable(updateUser.getParameters()[0]);
        assertPathVariable(deleteUser.getParameters()[0]);
    }

    @Test
    void controllerDelegatesArgumentsToServiceInContractOrder() {
        UserManagementService service = mock(UserManagementService.class);
        UserManagementController controller = new UserManagementController(service);
        String userId = "target-user";
        String actor = "admin-user";
        String roles = "ROLE_ADMIN";
        ApiRequest<UserCreateReqDto> createRequest = new ApiRequest<>();
        ApiRequest<UserUpdateReqDto> updateRequest = new ApiRequest<>();

        controller.getUsers(roles);
        controller.getUser(userId, roles);
        controller.createUser(createRequest, actor, roles);
        controller.updateUser(userId, updateRequest, actor, roles);
        controller.deleteUser(userId, actor, roles);

        verify(service).getUsers(roles);
        verify(service).getUser(userId, roles);
        verify(service).createUser(createRequest, actor, roles);
        verify(service).updateUser(userId, updateRequest, actor, roles);
        verify(service).deleteUser(userId, actor, roles);
    }

    private static void assertPath(String methodName, Class<?>[] parameterTypes, String path)
            throws Exception {
        Method method = UserManagementController.class.getDeclaredMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(PostMapping.class).value()).containsExactly(path);
    }

    private static void assertOptionalRequestHeader(Parameter parameter, String headerName) {
        assertThat(parameter.isAnnotationPresent(RequestHeader.class)).isTrue();
        assertThat(parameter.getAnnotation(RequestHeader.class).value()).isEqualTo(headerName);
        // 내부 헤더 누락도 MVC가 선제 차단하지 않고 서비스의 명시적 권한/필수값 검증까지 전달한다.
        assertThat(parameter.getAnnotation(RequestHeader.class).required()).isFalse();
    }

    private static void assertRequestBody(Parameter parameter) {
        assertThat(parameter.isAnnotationPresent(RequestBody.class)).isTrue();
    }

    private static void assertPathVariable(Parameter parameter) {
        assertThat(parameter.isAnnotationPresent(PathVariable.class)).isTrue();
    }
}
