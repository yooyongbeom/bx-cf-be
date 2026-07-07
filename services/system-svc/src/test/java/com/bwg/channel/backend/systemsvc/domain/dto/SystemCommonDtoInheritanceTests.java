package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuActionResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.RoleMenuSaveReqDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemCommonDtoInheritanceTests {

    @Test
    void writeDtosDeclareAuditWriterFieldsDirectly() {
        assertThat(List.of(
                CommonCodeGroupReqDto.class,
                CommonCodeReqDto.class,
                MenuReqDto.class,
                RoleMenuSaveReqDto.class
        )).allSatisfy(dtoType -> {
            assertApiField(dtoType, "createdBy");
            assertThat(dtoType.getSuperclass()).isEqualTo(Object.class);
        });
    }

    @Test
    void responseDtosDeclareAuditFieldsDirectly() {
        assertThat(List.of(
                CommonCodeGroupResDto.class,
                CommonCodeResDto.class,
                MenuResDto.class,
                MenuActionResDto.class
        )).allSatisfy(dtoType -> {
            assertApiField(dtoType, "createdBy");
            assertApiField(dtoType, "updatedBy");
            assertApiField(dtoType, "createdAt");
            assertApiField(dtoType, "updatedAt");
            assertThat(dtoType.getSuperclass()).isEqualTo(Object.class);
        });
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
