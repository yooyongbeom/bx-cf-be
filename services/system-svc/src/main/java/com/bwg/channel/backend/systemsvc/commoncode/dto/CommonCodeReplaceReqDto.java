package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.ArrayList;
import java.util.List;

/**
 * 그룹에 속한 공통코드 전체를 교체하기 위한 요청 모델.
 *
 * <p>교체 등록 시 계층 관계는 구성하지 않으므로 각 코드의 {@code parentCodeId}는
 * 저장 SQL에서 항상 {@code NULL}로 처리한다.</p>
 */
@Alias("CommonCodeReplaceReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "CommonCodeReplace", endpoints = {"replace"})
public class CommonCodeReplaceReqDto {

    @ApiField(description = "교체할 공통코드 목록", required = {"replace"})
    private List<CommonCodeReqDto> codes = new ArrayList<>();

    @ApiField(description = "변경 요청자 ID", example = "admin", optional = {"replace"})
    private String createdBy;
}
