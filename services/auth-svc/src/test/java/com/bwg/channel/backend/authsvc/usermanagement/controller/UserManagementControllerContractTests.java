package com.bwg.channel.backend.authsvc.usermanagement.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

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
    void everyEndpointRequiresGatewayRolesHeader() {
        for (Method method : UserManagementController.class.getDeclaredMethods()) {
            assertThat(method.getParameters())
                    .filteredOn(parameter -> parameter.isAnnotationPresent(RequestHeader.class))
                    .anySatisfy(parameter -> assertThat(parameter.getAnnotation(RequestHeader.class).value())
                            .isEqualTo(InternalAuthHeaders.ROLES));
        }
    }

    private static void assertPath(String methodName, Class<?>[] parameterTypes, String path)
            throws Exception {
        Method method = UserManagementController.class.getDeclaredMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(PostMapping.class).value()).containsExactly(path);
    }
}
