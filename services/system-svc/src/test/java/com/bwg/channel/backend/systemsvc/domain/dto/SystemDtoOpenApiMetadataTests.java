package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemDtoOpenApiMetadataTests {

    // API 경계 DTO(요청/응답)만 @ApiDto/@ApiField 메타데이터를 갖는다. (내부 도메인 DTO는 제외)
    private static final List<Class<?>> DTO_TYPES = List.of(
            CommonCodeGroupReqDto.class,
            CommonCodeGroupResDto.class,
            CommonCodeReqDto.class,
            CommonCodeResDto.class,
            MenuReqDto.class,
            MenuResDto.class,
            MenuActionResDto.class,
            RoleMenuSaveReqDto.class
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
