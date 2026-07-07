package com.bwg.channel.backend.systemsvc.referencedata.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionReqDto;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;
import com.bwg.channel.backend.systemsvc.referencedata.repository.ReferenceDataVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 기준정보 최신 버전 조회 요청을 검증하고 저장소 조회를 위임하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class ReferenceDataVersionServiceImpl implements ReferenceDataVersionService {

    private static final String ALL_REF_TYPE = "ALL";

    private final ReferenceDataVersionRepository referenceDataVersionRepository;

    @Override
    public ApiResponse<List<ReferenceDataVersionResDto>> getLatestReferenceDataVersions(
            ApiRequest<ReferenceDataVersionReqDto> paramDto
    ) {
        // 요청 본문의 기준정보 유형을 필수값으로 검증
        ReferenceDataVersionReqDto data = requireData(paramDto);
        String refType = BusinessValidator.requireNonBlank(data.getRefType(), "refType");

        // ALL 요청은 전체 기준정보 최신 버전을, 그 외에는 해당 기준정보 유형만 조회
        List<ReferenceDataVersionResDto> result = isAllRefType(refType)
                ? referenceDataVersionRepository.findLatestReferenceDataVersions()
                : referenceDataVersionRepository.findLatestReferenceDataVersionsByRefType(refType);

        return ApiResponse.success(result, toPagination(result));
    }

    private boolean isAllRefType(String refType) {
        // ALL은 전체 기준정보 최신 버전 조회를 나타내는 예약 refType이다.
        return ALL_REF_TYPE.equalsIgnoreCase(refType);
    }

    private <T> T requireData(ApiRequest<T> request) {
        // 공통 요청 래퍼의 data 블록 검증
        return BusinessValidator.requireNonNull(request == null ? null : request.getData(), "data");
    }

    private PaginationResDto toPagination(List<?> result) {
        // 현재 전체 목록 응답 기준으로 페이지 메타데이터 생성
        int totalCount = result == null ? 0 : result.size();
        PaginationResDto pagination = new PaginationResDto();
        pagination.setPage(1);
        pagination.setSize(totalCount);
        pagination.setTotalCount((long) totalCount);
        pagination.setTotalPages(totalCount == 0 ? 0 : 1);
        return pagination;
    }
}
