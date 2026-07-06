package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.domain.dto.BaseAuditReqDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/** 역할별 메뉴 권한 저장 요청 모델 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiDto(type = ApiType.REQUEST, name = "RoleMenuSave", endpoints = {"save"})
public class RoleMenuSaveReqDto extends BaseAuditReqDto {

    @ApiField(description = "역할에 부여할 메뉴 ID 목록", required = {"save"})
    private List<Long> menuIds = new ArrayList<>();

}
