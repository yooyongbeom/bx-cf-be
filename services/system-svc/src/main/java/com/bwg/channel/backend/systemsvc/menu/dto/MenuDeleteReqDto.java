package com.bwg.channel.backend.systemsvc.menu.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/** 메뉴 삭제 요청 모델 */
@Alias("MenuDeleteReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "MenuDelete", endpoints = {"delete"})
public class MenuDeleteReqDto {

    @ApiField(description = "삭제 요청자 ID", example = "admin", required = {"delete"})
    private String deletedBy;
}
