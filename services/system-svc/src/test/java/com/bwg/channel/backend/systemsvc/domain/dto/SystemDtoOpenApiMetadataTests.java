package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemDtoOpenApiMetadataTests {

    private static final List<Class<?>> DTO_TYPES = List.of(
            CommonCodeDto.class,
            CommonCodeGroupDto.class,
            MenuActionDto.class,
            MenuDto.class,
            RoleMenuSaveDto.class
    );

    @Test
    void allSystemDtosHaveOpenApiMetadata() {
        for (Class<?> dtoType : DTO_TYPES) {
            assertThat(dtoType.getAnnotation(ApiDto.class))
                    .as("%s must have @ApiDto", dtoType.getSimpleName())
                    .isNotNull();

            for (var field : dtoType.getDeclaredFields()) {
                ApiField apiField = field.getAnnotation(ApiField.class);
                assertThat(apiField)
                        .as("%s.%s must have @ApiField", dtoType.getSimpleName(), field.getName())
                        .isNotNull();
                assertThat(apiField.description())
                        .as("%s.%s description", dtoType.getSimpleName(), field.getName())
                        .isNotBlank();
            }
        }
    }
}
