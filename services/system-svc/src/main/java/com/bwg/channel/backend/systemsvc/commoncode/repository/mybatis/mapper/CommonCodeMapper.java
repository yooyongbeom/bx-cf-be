package com.bwg.channel.backend.systemsvc.commoncode.repository.mybatis.mapper;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 공통코드 SQL 매핑을 담당하는 MyBatis Mapper.
 */
@Mapper
public interface CommonCodeMapper {

    /**
     * 공통코드 그룹 목록 조회 SQL 매핑
     */
    List<CommonCodeGroupResDto> findCommonCodeGroups();

    /**
     * 공통코드 그룹 등록 SQL 매핑
     */
    int insertCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto);

    /**
     * 공통코드 그룹 수정 SQL 매핑
     */
    int updateCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto);

    /**
     * 그룹 코드 기준 공통코드 목록 조회 SQL 매핑
     */
    List<CommonCodeResDto> findCommonCodes(@Param("groupCd") String groupCd);

    /**
     * 공통코드 그룹 상세 정보 조회 SQL 매핑
     */
    CommonCodeGroupDetailResDto findCommonCodeGroupDetail(@Param("groupCd") String groupCd);

    /**
     * 전체 공통코드 그룹 상세 정보 조회 SQL 매핑
     */
    List<CommonCodeGroupDetailResDto> findCommonCodeGroupDetails();

    /**
     * 그룹 코드 기준 공통코드 상세 항목 목록 조회 SQL 매핑
     */
    List<CommonCodeResDto> findCommonCodeDetails(@Param("groupCd") String groupCd);

    /**
     * 공통코드 등록 SQL 매핑
     */
    int insertCommonCode(ApiRequest<CommonCodeReqDto> paramDto);

    /**
     * 공통코드 수정 SQL 매핑
     */
    int updateCommonCode(ApiRequest<CommonCodeReqDto> paramDto);

    /**
     * 그룹 코드 기준 기존 공통코드 전체 삭제 SQL 매핑
     */
    int deleteCommonCodesByGroupCd(@Param("groupCd") String groupCd);

    /**
     * 교체 대상 공통코드 목록 일괄 등록 SQL 매핑
     */
    int insertCommonCodes(
            @Param("groupCd") String groupCd,
            @Param("codes") List<CommonCodeReqDto> codes,
            @Param("createdBy") String createdBy
    );

    /**
     * 기준정보 버전 변경 이력 등록 SQL 매핑
     */
    int insertReferenceDataVersionHistory(
            @Param("refType") String refType,
            @Param("changeType") String changeType,
            @Param("targetTable") String targetTable,
            @Param("targetId") String targetId,
            @Param("changeSummary") String changeSummary,
            @Param("changedBy") String changedBy
    );

    /**
     * 기준정보 최신 버전 갱신 SQL 매핑
     */
    int updateReferenceDataVersion(
            @Param("refType") String refType,
            @Param("remark") String remark,
            @Param("changedBy") String changedBy
    );
}
