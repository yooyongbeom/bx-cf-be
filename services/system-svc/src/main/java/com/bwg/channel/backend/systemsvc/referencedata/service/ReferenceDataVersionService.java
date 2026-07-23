package com.bwg.channel.backend.systemsvc.referencedata.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionReqDto;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;

import java.util.List;

/**
 * 기준정보 버전 조회 서비스 계약.
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
}
