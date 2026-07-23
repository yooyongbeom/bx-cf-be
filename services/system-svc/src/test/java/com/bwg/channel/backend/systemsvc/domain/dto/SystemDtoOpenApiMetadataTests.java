package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeCreateReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemDtoOpenApiMetadataTests {

    // API 경계 DTO(요청/응답)만 @ApiDto/@ApiField 메타데이터를 갖는다. (내부 도메인 DTO는 제외)
    private static final List<Class<?>> DTO_TYPES = List.of(
            CommonCodeCreateReqDto.class,
            CommonCodeGroupResDto.class,
            CommonCodeGroupDetailResDto.class,
            CommonCodeReplaceReqDto.class,
            CommonCodeReqDto.class,
            CommonCodeResDto.class,
            MenuCreateReqDto.class,
            MenuUpdateReqDto.class,
            MenuListResDto.class,
            MenuDetailResDto.class,
            MenuActionResDto.class,
            RoleMenuSaveReqDto.class
    );

    @Test
    void commandDtosDoNotExposeSystemFields() {
        assertThat(List.of(
                CommonCodeCreateReqDto.class,
                CommonCodeReplaceReqDto.class,
                CommonCodeReqDto.class,
                MenuCreateReqDto.class,
                MenuUpdateReqDto.class,
                RoleMenuSaveReqDto.class
        )).allSatisfy(dtoType -> assertThat(dtoType.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("createdBy", "createdAt", "updatedBy", "updatedAt"));

        assertThat(MenuCreateReqDto.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("menuId");
        assertThat(MenuUpdateReqDto.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("menuId", "menuCd");
    }

    @Test
    void commonCodeDtosDocumentEmptyCodeListsAndDetailAuditFields() throws NoSuchFieldException {
        ApiField createCodes = CommonCodeCreateReqDto.class
                .getDeclaredField("codes")
                .getAnnotation(ApiField.class);
        ApiField replaceCodes = CommonCodeReplaceReqDto.class
                .getDeclaredField("codes")
                .getAnnotation(ApiField.class);

        assertThat(createCodes.description()).contains("빈 배열이면 그룹만 등록");
        assertThat(replaceCodes.description()).contains("빈 배열이면 기존 상세코드 전체 삭제");

        for (String fieldName : List.of("createdBy", "updatedBy", "createdAt", "updatedAt")) {
            ApiField apiField = CommonCodeResDto.class
                    .getDeclaredField(fieldName)
                    .getAnnotation(ApiField.class);
            assertThat(apiField.optional())
                    .as("CommonCodeResDto.%s detail endpoint", fieldName)
                    .contains("detail");
        }
    }

    @Test
    void menuCrudDtosUseMenuAsOpenApiBaseName() {
        assertThat(List.of(
                MenuCreateReqDto.class,
                MenuUpdateReqDto.class,
                MenuListResDto.class,
                MenuDetailResDto.class
        )).allSatisfy(dtoType -> assertThat(dtoType.getAnnotation(ApiDto.class).name())
                .as("%s OpenAPI base name", dtoType.getSimpleName())
                .isEqualTo("Menu"));
    }

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
