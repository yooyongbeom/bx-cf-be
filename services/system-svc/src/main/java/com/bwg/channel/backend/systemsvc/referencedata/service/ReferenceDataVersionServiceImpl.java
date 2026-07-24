package com.bwg.channel.backend.systemsvc.referencedata.service;

import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.util.PageUtil;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionReqDto;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;
import com.bwg.channel.backend.systemsvc.referencedata.repository.ReferenceDataVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 기준정보 최신 버전 조회와 변경 이력 기록을 담당하는 서비스 구현체.
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
        ReferenceDataVersionReqDto data = BusinessValidator.requireData(paramDto);
        String refType = BusinessValidator.requireNonBlank(data.getRefType(), "refType");

        // ALL 요청은 전체 기준정보 최신 버전을, 그 외에는 해당 기준정보 유형만 조회
        List<ReferenceDataVersionResDto> result = isAllRefType(refType)
                ? referenceDataVersionRepository.findLatestReferenceDataVersions()
                : referenceDataVersionRepository.findLatestReferenceDataVersionsByRefType(refType);

        return ApiResponse.success(result, PageUtil.singlePage(result));
    }

    /**
     * 변경 이력을 먼저 등록한 뒤 동일한 기준정보 유형의 최신 버전을 갱신한다.
     *
     * <p>호출한 업무 서비스의 MyBatis 트랜잭션에 반드시 참여하며, 두 SQL 중 하나라도
     * 예상 반영 건수와 다르면 예외를 발생시켜 업무 데이터 변경까지 함께 롤백한다.</p>
     *
     * @param refType 기준정보 유형
     * @param changeType 변경 유형
     * @param targetTable 변경 대상 테이블
     * @param targetId 변경 대상 식별자
     * @param changeSummary 변경 내용 요약
     * @param changedBy 변경 사용자 ID
     * @throws BwgBusinessException 버전 이력 또는 최신 버전 갱신 건수가 예상과 다른 경우
     */
    @Override
    @Transactional(
            transactionManager = "mybatisMainTransactionManager",
            propagation = Propagation.MANDATORY
    )
    public void versionChange(
            String refType,
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    ) {
        // 이력을 먼저 기록해 변경 전후 버전을 보존한 뒤 최신 버전을 같은 트랜잭션에서 갱신한다.
        BusinessValidator.requireAffectedRows(
                referenceDataVersionRepository.insertReferenceDataVersionHistory(
                        refType,
                        changeType,
                        targetTable,
                        targetId,
                        changeSummary,
                        changedBy
                ),
                1,
                "insertReferenceDataVersionHistory"
        );
        BusinessValidator.requireAffectedRows(
                referenceDataVersionRepository.updateReferenceDataVersion(
                        refType,
                        changeSummary,
                        changedBy
                ),
                1,
                "updateReferenceDataVersion"
        );
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

}
