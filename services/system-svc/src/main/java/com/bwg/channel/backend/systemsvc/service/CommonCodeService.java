package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeResDto;

import java.util.List;

/**
 * 공통코드 그룹과 코드 관리 기능의 서비스 계약.
 */
public interface CommonCodeService {

    ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups();

    ApiResponse<Void> createCommonCodeGroup(CommonCodeGroupReqDto paramDto);

    ApiResponse<Void> updateCommonCodeGroup(String groupCd, CommonCodeGroupReqDto paramDto);

    ApiResponse<List<CommonCodeResDto>> getCommonCodes(String groupCd);

    ApiResponse<Void> createCommonCode(String groupCd, CommonCodeReqDto paramDto);

    ApiResponse<Void> updateCommonCode(String groupCd, String code, CommonCodeReqDto paramDto);
}
