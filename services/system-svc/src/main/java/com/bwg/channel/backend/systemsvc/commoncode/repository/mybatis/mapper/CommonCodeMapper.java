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
}
