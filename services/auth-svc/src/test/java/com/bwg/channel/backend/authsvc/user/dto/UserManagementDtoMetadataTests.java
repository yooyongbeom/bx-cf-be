package com.bwg.channel.backend.authsvc.user.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserManagementDtoMetadataTests {

    private static final List<Class<?>> DTO_TYPES = List.of(
            UserCreateReqDto.class,
            UserUpdateReqDto.class,
            UserListResDto.class,
            UserDetailResDto.class
    );

    @Test
    void allUserManagementDtosExposeOpenApiMetadata() {
        for (Class<?> dtoType : DTO_TYPES) {
            assertThat(dtoType.getAnnotation(ApiDto.class)).isNotNull();
            assertThat(dtoType.getAnnotation(ApiDto.class).name()).isEqualTo("User");
            for (Field field : dtoType.getDeclaredFields()) {
                assertThat(field.getAnnotation(ApiField.class))
                        .as("%s.%s", dtoType.getSimpleName(), field.getName())
                        .isNotNull();
            }
        }
    }

    @Test
    void commandDtosDoNotAcceptKeysTokensRolesOrSystemFields() {
        assertThat(UserCreateReqDto.class.getDeclaredFields())
                .extracting(Field::getName)
                .containsExactlyInAnyOrder("usrId", "usrNm", "positDivName", "deptName", "usrPwd")
                .doesNotContain("roles", "createdBy", "createdAt", "updatedBy", "updatedAt",
                        "refreshToken", "refreshTokenExpiresAt");
        assertThat(UserUpdateReqDto.class.getDeclaredFields())
                .extracting(Field::getName)
                .containsExactlyInAnyOrder("usrNm", "positDivName", "deptName")
                .doesNotContain("usrId", "usrPwd", "roles", "createdBy", "createdAt",
                        "updatedBy", "updatedAt");
    }

    @Test
    void responseDtosNeverExposePasswordsOrTokens() {
        assertThat(List.of(UserListResDto.class, UserDetailResDto.class))
                .allSatisfy(dtoType -> assertThat(dtoType.getDeclaredFields())
                        .extracting(Field::getName)
                        .doesNotContain("usrPwd", "refreshToken", "refreshTokenExpiresAt"));
    }
}
