package com.bwg.channel.backend.systemsvc.referencedata.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionReqDto;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;

import java.util.List;

/**
 * 기준정보 버전 조회와 변경을 담당하는 서비스 계약.
 */
public interface ReferenceDataVersionService {

    /**
     * 요청한 기준정보 유형의 최신 버전 목록을 조회한다.
     *
     * <p>{@code refType}이 {@code ALL}이면 전체 기준정보 유형을 조회한다.</p>
     *
     * @param paramDto 조회할 기준정보 유형
     * @return 기준정보 최신 버전 목록과 페이지 정보가 포함된 응답
     */
    ApiResponse<List<ReferenceDataVersionResDto>> getLatestReferenceDataVersions(
            ApiRequest<ReferenceDataVersionReqDto> paramDto
    );

    /**
     * 기준정보 변경 이력을 등록하고 해당 유형의 최신 버전을 갱신한다.
     *
     * @param refType 기준정보 유형
     * @param changeType 변경 유형
     * @param targetTable 변경 대상 테이블
     * @param targetId 변경 대상 식별자
     * @param changeSummary 변경 내용 요약
     * @param changedBy 변경 사용자 ID
     */
    void versionChange(
            String refType,
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    );
}
