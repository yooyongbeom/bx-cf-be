package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 역할별 메뉴 권한 저장 요청 모델 */
@Data
@ApiDto(type = ApiType.REQUEST, name = "RoleMenuSave", endpoints = {"save"})
public class RoleMenuSaveReqDto {

    @ApiField(description = "역할에 부여할 메뉴 ID 목록", required = {"save"})
    private List<Long> menuIds = new ArrayList<>();

    @ApiField(description = "생성자 ID", example = "admin", optional = {"save"})
    private String createdBy;
}
