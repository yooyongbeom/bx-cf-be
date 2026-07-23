package com.bwg.channel.backend.systemsvc.referencedata.repository.mybatis.mapper;

import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 기준정보 버전 SQL 매핑을 담당하는 MyBatis Mapper.
 */
@Mapper
public interface ReferenceDataVersionMapper {

    /**
     * 전체 기준정보 최신 버전 목록 조회 SQL 매핑
     */
    List<ReferenceDataVersionResDto> findLatestReferenceDataVersions();

    /**
     * 기준정보 유형별 최신 버전 조회 SQL 매핑
     */
    List<ReferenceDataVersionResDto> findLatestReferenceDataVersionsByRefType(@Param("refType") String refType);

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
