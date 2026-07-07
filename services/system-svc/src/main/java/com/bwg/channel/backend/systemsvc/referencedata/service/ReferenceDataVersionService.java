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
     * 기준정보 최신 버전 목록 조회
     */
    ApiResponse<List<ReferenceDataVersionResDto>> getLatestReferenceDataVersions(
            ApiRequest<ReferenceDataVersionReqDto> paramDto
    );
}
