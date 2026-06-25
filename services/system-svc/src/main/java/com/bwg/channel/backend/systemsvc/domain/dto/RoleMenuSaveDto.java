package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 역할에 부여할 메뉴 권한 목록을 저장할 때 사용하는 DTO.
 */
@Data
@ApiDto(name = "RoleMenuSave", endpoints = {"save"}, generateResponse = false)
public class RoleMenuSaveDto {
    @ApiField(description = "역할에 부여할 메뉴 ID 목록", required = {"save"})
    private List<Long> menuIds = new ArrayList<>();

    @ApiField(description = "생성자 ID", example = "admin", optional = {"save"})
    private String createdBy;
}
