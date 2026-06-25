package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupDto;

import java.util.List;

/**
 * 공통코드 그룹과 코드 관리 기능의 서비스 계약.
 */
public interface CommonCodeService {

    /**
     * 공통코드 그룹 목록을 조회한다.
     */
    ApiResponse<List<CommonCodeGroupDto>> getCommonCodeGroups();

    /**
     * 공통코드 그룹을 등록한다.
     */
    ApiResponse<Void> createCommonCodeGroup(CommonCodeGroupDto paramDto);

    /**
     * 공통코드 그룹을 수정한다.
     */
    ApiResponse<Void> updateCommonCodeGroup(String groupCd, CommonCodeGroupDto paramDto);

    /**
     * 특정 그룹의 공통코드 목록을 조회한다.
     */
    ApiResponse<List<CommonCodeDto>> getCommonCodes(String groupCd);

    /**
     * 특정 그룹에 공통코드를 등록한다.
     */
    ApiResponse<Void> createCommonCode(String groupCd, CommonCodeDto paramDto);

    /**
     * 특정 그룹의 공통코드를 수정한다.
     */
    ApiResponse<Void> updateCommonCode(String groupCd, String code, CommonCodeDto paramDto);
}
