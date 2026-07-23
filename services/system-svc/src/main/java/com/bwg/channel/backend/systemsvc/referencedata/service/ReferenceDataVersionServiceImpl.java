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

    /**
     * 요청한 기준정보 유형에 따라 전체 또는 유형별 최신 버전 목록을 조회한다.
     *
     * <p>{@code refType}이 대소문자와 관계없이 {@code ALL}이면 전체 목록을 조회하고,
     * 그 외 값은 유형별 저장소 조회 조건으로 전달한다.</p>
     *
     * @param paramDto 조회할 기준정보 유형
     * @return 기준정보 최신 버전 목록과 페이지 정보가 포함된 응답
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         요청 데이터 또는 기준정보 유형이 없는 경우
     */
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

    /**
     * 기준정보 유형이 전체 조회를 나타내는 예약값인지 확인한다.
     *
     * @param refType 확인할 기준정보 유형
     * @return 대소문자와 관계없이 {@code ALL}이면 {@code true}
     */
    private boolean isAllRefType(String refType) {
        // ALL은 전체 기준정보 최신 버전 조회를 나타내는 예약 refType이다.
        return ALL_REF_TYPE.equalsIgnoreCase(refType);
    }

    /**
     * 공통 API 요청 래퍼에서 필수 {@code data} 영역을 추출한다.
     *
     * @param request 공통 API 요청 래퍼
     * @param <T> 요청 데이터 타입
     * @return null이 아닌 요청 데이터
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         요청 또는 {@code data}가 없는 경우
     */
    private <T> T requireData(ApiRequest<T> request) {
        // 공통 요청 래퍼의 data 블록 검증
        return BusinessValidator.requireNonNull(request == null ? null : request.getData(), "data");
    }

    /**
     * 전체 목록 조회 결과 크기를 기준으로 단일 페이지 메타데이터를 생성한다.
     *
     * @param result 페이지 정보를 계산할 조회 결과
     * @return 전체 결과를 한 페이지로 표현한 페이지 정보
     */
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
