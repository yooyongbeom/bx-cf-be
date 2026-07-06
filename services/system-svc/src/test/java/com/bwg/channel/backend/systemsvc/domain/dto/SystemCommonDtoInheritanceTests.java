package com.bwg.channel.backend.systemsvc.domain.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemCommonDtoInheritanceTests {

    @Test
    void writeDtosUseAuditRequestBaseClass() throws Exception {
        Class<?> auditRequestType = Class.forName("com.bwg.channel.backend.common.domain.dto.BaseAuditReqDto");

        assertThat(List.of(
                CommonCodeGroupReqDto.class,
                CommonCodeReqDto.class,
                MenuReqDto.class,
                RoleMenuSaveReqDto.class
        )).allSatisfy(dtoType -> assertThat(auditRequestType.isAssignableFrom(dtoType))
                .as("%s should extend BaseAuditReqDto", dtoType.getSimpleName())
                .isTrue());
    }

    @Test
    void responseDtosUseAuditResponseBaseClass() throws Exception {
        Class<?> auditResponseType = Class.forName("com.bwg.channel.backend.common.domain.dto.BaseAuditResDto");

        assertThat(List.of(
                CommonCodeGroupResDto.class,
                CommonCodeResDto.class,
                MenuResDto.class,
                MenuActionResDto.class
        )).allSatisfy(dtoType -> assertThat(auditResponseType.isAssignableFrom(dtoType))
                .as("%s should extend BaseAuditResDto", dtoType.getSimpleName())
                .isTrue());
    }
}
