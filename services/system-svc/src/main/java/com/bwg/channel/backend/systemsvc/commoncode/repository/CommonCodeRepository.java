package com.bwg.channel.backend.systemsvc.commoncode.repository;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;

import java.util.List;

/**
 * 공통코드 데이터 접근을 추상화한 저장소 계약.
 */
public interface CommonCodeRepository {

    /**
     * 공통코드 그룹 목록 조회
     */
    List<CommonCodeGroupResDto> findCommonCodeGroups();

    /**
     * 공통코드 그룹 등록
     */
    int insertCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto);

    /**
     * 공통코드 그룹 수정
     */
    int updateCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto);

    /**
     * 그룹 코드 기준 공통코드 목록 조회
     */
    List<CommonCodeResDto> findCommonCodes(String groupCd);

    /**
     * 공통코드 그룹 상세 정보 조회
     */
    CommonCodeGroupDetailResDto findCommonCodeGroupDetail(String groupCd);

    List<CommonCodeGroupDetailResDto> findCommonCodeGroupDetails();

    /**
     * 그룹 코드 기준 공통코드 상세 항목 목록 조회
     */
    List<CommonCodeResDto> findCommonCodeDetails(String groupCd);

    /**
     * 공통코드 등록
     */
    int insertCommonCode(ApiRequest<CommonCodeReqDto> paramDto);

    /**
     * 공통코드 수정
     */
    int updateCommonCode(ApiRequest<CommonCodeReqDto> paramDto);

    /**
     * 그룹 코드에 속한 기존 공통코드 전체 삭제
     */
    int deleteCommonCodesByGroupCd(String groupCd);

    /**
     * 그룹 코드에 속할 공통코드 목록 일괄 등록
     */
    int insertCommonCodes(String groupCd, List<CommonCodeReqDto> codes, String createdBy);

    /**
     * 기준정보 버전 변경 이력 등록
     */
    int insertReferenceDataVersionHistory(
            String refType,
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    );

    /**
     * 기준정보 최신 버전 갱신
     */
    int updateReferenceDataVersion(String refType, String remark, String changedBy);
}
