package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemCommonDtoInheritanceTests {

    @Test
    void writeDtosDoNotExposeSystemFields() {
        assertThat(List.of(
                CommonCodeGroupReqDto.class,
                CommonCodeGroupReplaceReqDto.class,
                CommonCodeReplaceReqDto.class,
                CommonCodeReqDto.class,
                MenuCreateReqDto.class,
                MenuUpdateReqDto.class,
                RoleMenuSaveReqDto.class
        )).allSatisfy(dtoType -> assertThat(dtoType.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("createdBy", "createdAt", "updatedBy", "updatedAt"));
    }

    @Test
    void responseDtosDeclareSystemFieldsDirectly() {
        assertThat(List.of(
                CommonCodeGroupResDto.class,
                CommonCodeResDto.class,
                MenuListResDto.class,
                MenuDetailResDto.class,
                MenuActionResDto.class
        )).allSatisfy(dtoType -> {
            assertApiField(dtoType, "createdBy");
            assertApiField(dtoType, "updatedBy");
            assertApiField(dtoType, "createdAt");
            assertApiField(dtoType, "updatedAt");
            assertThat(dtoType.getSuperclass()).isEqualTo(Object.class);
        });
    }

    @Test
    void legacySharedMenuDtosAreRemoved() {
        assertThatThrownBy(() -> Class.forName(
                "com.bwg.channel.backend.systemsvc.menu.dto.MenuReqDto"
        )).isInstanceOf(ClassNotFoundException.class);
        assertThatThrownBy(() -> Class.forName(
                "com.bwg.channel.backend.systemsvc.menu.dto.MenuResDto"
        )).isInstanceOf(ClassNotFoundException.class);
        assertThatThrownBy(() -> Class.forName(
                "com.bwg.channel.backend.systemsvc.menu.dto.MenuDeleteReqDto"
        )).isInstanceOf(ClassNotFoundException.class);
    }

    private static void assertApiField(Class<?> dtoType, String fieldName) {
        try {
            ApiField apiField = dtoType.getDeclaredField(fieldName).getAnnotation(ApiField.class);
            assertThat(apiField)
                    .as("%s.%s must have @ApiField", dtoType.getSimpleName(), fieldName)
                    .isNotNull();
        } catch (NoSuchFieldException e) {
            throw new AssertionError(dtoType.getSimpleName() + " must declare " + fieldName, e);
        }
    }
}
