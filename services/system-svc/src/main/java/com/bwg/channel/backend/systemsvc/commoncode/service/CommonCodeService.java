package com.bwg.channel.backend.systemsvc.commoncode.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;

import java.util.List;

/**
 * 공통코드 그룹과 공통코드 관리 기능의 서비스 계약.
 */
public interface CommonCodeService {

    /**
     * 공통코드 그룹 목록 조회
     */
    ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups();

    /**
     * 공통코드 그룹 등록
     */
    ApiResponse<Void> createCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto, String userId);

    /**
     * 공통코드 그룹 수정
     */
    ApiResponse<Void> updateCommonCodeGroup(
            String groupCd,
            ApiRequest<CommonCodeGroupReqDto> paramDto,
            String userId
    );

    /**
     * 그룹 코드 기준 공통코드 목록 조회
     */
    ApiResponse<List<CommonCodeResDto>> getCommonCodes(String groupCd);

    /**
     * 공통코드 그룹과 하위 공통코드 목록 상세 조회
     */
    ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetails(ApiRequest<CommonCodeGroupReqDto> paramDto);

    /**
     * 공통코드 등록
     */
    ApiResponse<Void> createCommonCode(String groupCd, ApiRequest<CommonCodeReqDto> paramDto, String userId);

    /**
     * 공통코드 수정
     */
    ApiResponse<Void> updateCommonCode(
            String groupCd,
            String code,
            ApiRequest<CommonCodeReqDto> paramDto,
            String userId
    );

    /**
     * 공통코드 그룹 정보와 하위 공통코드 전체 교체
     */
    ApiResponse<Void> replaceCommonCodes(
            String groupCd,
            ApiRequest<CommonCodeReplaceReqDto> paramDto,
            String userId
    );
}
