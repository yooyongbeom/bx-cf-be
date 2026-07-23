package com.bwg.channel.backend.systemsvc.referencedata.repository;

import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;

import java.util.List;

/**
 * 기준정보 버전 데이터 접근을 추상화한 저장소 계약.
 */
public interface ReferenceDataVersionRepository {

    /**
     * 전체 기준정보 최신 버전 목록 조회
     */
    List<ReferenceDataVersionResDto> findLatestReferenceDataVersions();

    /**
     * 기준정보 유형별 최신 버전 조회
     */
    List<ReferenceDataVersionResDto> findLatestReferenceDataVersionsByRefType(String refType);

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
