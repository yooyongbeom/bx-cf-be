package com.bwg.channel.backend.systemsvc.commoncode.service;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 공통코드 그룹과 공통코드의 입력값을 검증하고 저장소 처리를 위임하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class CommonCodeServiceImpl implements CommonCodeService {

    private static final String ALL_GROUP_CD = "ALL";
    private static final String REF_TYPE_COMMON_CODE = "COMMON_CODE";

    private final CommonCodeRepository commonCodeRepository;

    @Override
    public ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups() {
        // 저장소에서 공통코드 그룹 목록 조회
        List<CommonCodeGroupResDto> result = commonCodeRepository.findCommonCodeGroups();
        // 조회 결과와 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createCommonCodeGroup(
            ApiRequest<CommonCodeGroupReqDto> paramDto,
            String userId
    ) {
        // 감사 컬럼과 변경 이력에는 Gateway가 검증한 사용자만 사용한다.
        String changedBy = requireUserId(userId);
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeGroupReqDto data = requireData(paramDto);
        // 등록 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(data.getGroupCd(), "groupCd"));
        data.setGroupNm(BusinessValidator.requireNonBlank(data.getGroupNm(), "groupNm"));
        // 공통코드 그룹 등록 처리
        commonCodeRepository.insertCommonCodeGroup(paramDto, changedBy);
        recordCommonCodeVersionChange(
                "CREATE",
                "common_code_groups",
                data.getGroupCd(),
                "공통코드 그룹 등록",
                changedBy
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCodeGroup(
            String groupCd,
            ApiRequest<CommonCodeGroupReqDto> paramDto,
            String userId
    ) {
        // 감사 컬럼과 변경 이력에는 Gateway가 검증한 사용자만 사용한다.
        String changedBy = requireUserId(userId);
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeGroupReqDto data = requireData(paramDto);
        // 경로 변수와 수정 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        data.setGroupNm(BusinessValidator.requireNonBlank(data.getGroupNm(), "groupNm"));
        // 공통코드 그룹 수정 처리
        commonCodeRepository.updateCommonCodeGroup(paramDto, changedBy);
        recordCommonCodeVersionChange(
                "UPDATE",
                "common_code_groups",
                data.getGroupCd(),
                "공통코드 그룹 수정",
                changedBy
        );
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<List<CommonCodeResDto>> getCommonCodes(String groupCd) {
        // 그룹 코드 검증 후 공통코드 목록 조회
        List<CommonCodeResDto> result = commonCodeRepository.findCommonCodes(
                BusinessValidator.requireNonBlank(groupCd, "groupCd")
        );
        // 조회 결과와 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    @Override
    public ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetails(ApiRequest<CommonCodeGroupReqDto> paramDto) {
        // 요청 본문의 groupCd는 필수이며 ALL이면 전체 그룹을 조회한다.
        CommonCodeGroupReqDto data = requireData(paramDto);
        String requiredGroupCd = BusinessValidator.requireNonBlank(data.getGroupCd(), "groupCd");

        List<CommonCodeGroupDetailResDto> groups = isAllGroup(requiredGroupCd)
                ? commonCodeRepository.findCommonCodeGroupDetails()
                : List.of(BusinessValidator.requireNonNull(
                        commonCodeRepository.findCommonCodeGroupDetail(requiredGroupCd),
                        "commonCodeGroup"
                ));

        // 그룹별 하위 공통코드를 묶어 payload를 항상 배열 형태로 조립한다.
        Map<String, List<CommonCodeResDto>> codesByGroupCd = commonCodeRepository
                .findCommonCodeDetails(isAllGroup(requiredGroupCd) ? null : requiredGroupCd)
                .stream()
                .collect(Collectors.groupingBy(
                        CommonCodeResDto::getGroupCd,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        groups.forEach(group -> group.setCodes(codesByGroupCd.getOrDefault(group.getGroupCd(), List.of())));
        return ApiResponse.success(groups, toPagination(groups));
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createCommonCode(
            String groupCd,
            ApiRequest<CommonCodeReqDto> paramDto,
            String userId
    ) {
        // 감사 컬럼과 변경 이력에는 Gateway가 검증한 사용자만 사용한다.
        String changedBy = requireUserId(userId);
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeReqDto data = requireData(paramDto);
        // 경로 변수와 등록 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        data.setCode(BusinessValidator.requireNonBlank(data.getCode(), "code"));
        data.setCodeNm(BusinessValidator.requireNonBlank(data.getCodeNm(), "codeNm"));
        // 공통코드 등록 처리
        commonCodeRepository.insertCommonCode(paramDto, changedBy);
        recordCommonCodeVersionChange(
                "CREATE",
                "common_codes",
                data.getGroupCd() + ":" + data.getCode(),
                "공통코드 등록",
                changedBy
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCode(
            String groupCd,
            String code,
            ApiRequest<CommonCodeReqDto> paramDto,
            String userId
    ) {
        // 감사 컬럼과 변경 이력에는 Gateway가 검증한 사용자만 사용한다.
        String changedBy = requireUserId(userId);
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeReqDto data = requireData(paramDto);
        // 경로 변수와 수정 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        data.setCode(BusinessValidator.requireNonBlank(code, "code"));
        data.setCodeNm(BusinessValidator.requireNonBlank(data.getCodeNm(), "codeNm"));
        // 공통코드 수정 처리
        commonCodeRepository.updateCommonCode(paramDto, changedBy);
        recordCommonCodeVersionChange(
                "UPDATE",
                "common_codes",
                data.getGroupCd() + ":" + data.getCode(),
                "공통코드 수정",
                changedBy
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> replaceCommonCodes(
            String groupCd,
            ApiRequest<CommonCodeReplaceReqDto> paramDto,
            String userId
    ) {
        // 그룹 수정, 코드 교체, 변경 이력에 동일한 검증 사용자를 적용한다.
        String changedBy = requireUserId(userId);
        // 경로의 그룹 코드를 먼저 정규화하고 실제 존재하는 공통코드 그룹인지 확인한다.
        String requiredGroupCd = BusinessValidator.requireNonBlank(groupCd, "groupCd");
        BusinessValidator.requireFound(
                commonCodeRepository.findCommonCodeGroupDetail(requiredGroupCd),
                "commonCodeGroup"
        );

        CommonCodeReplaceReqDto data = requireData(paramDto);
        // 그룹 정보와 코드 목록을 모두 검증한 뒤에만 데이터 변경을 시작한다.
        CommonCodeGroupReplaceReqDto requestedGroup = BusinessValidator.requireNonNull(data.getGroup(), "group");
        requestedGroup.setGroupNm(BusinessValidator.requireNonBlank(requestedGroup.getGroupNm(), "group.groupNm"));
        // 빈 목록은 전체 삭제 요청으로 해석하지 않고 필수값 누락으로 거부한다.
        List<CommonCodeReqDto> requestedItems = data.getItems();
        List<CommonCodeReqDto> items = BusinessValidator.requireNonNull(
                requestedItems == null || requestedItems.isEmpty() ? null : requestedItems,
                "items"
        );

        // 그룹 수정과 DELETE 전에 모든 항목을 검증하여 잘못된 요청이 기존 데이터를 변경하지 못하게 한다.
        Set<String> requestedCodes = new HashSet<>();
        for (CommonCodeReqDto code : items) {
            CommonCodeReqDto requiredCode = BusinessValidator.requireNonNull(code, "items.item");
            requiredCode.setCode(BusinessValidator.requireNonBlank(requiredCode.getCode(), "code"));
            requiredCode.setCodeNm(BusinessValidator.requireNonBlank(requiredCode.getCodeNm(), "codeNm"));
            requiredCode.setParentCodeId(null);
            if (!requestedCodes.add(requiredCode.getCode())) {
                throw duplicateCode(requiredCode.getCode());
            }
        }

        ApiRequest<CommonCodeGroupReqDto> groupUpdateRequest = toGroupUpdateRequest(
                requiredGroupCd,
                requestedGroup
        );
        // 검증 완료 후 그룹을 수정하고 기존 코드 목록을 새 항목으로 원자적으로 교체한다.
        commonCodeRepository.updateCommonCodeGroup(groupUpdateRequest, changedBy);
        commonCodeRepository.deleteCommonCodesByGroupCd(requiredGroupCd);
        commonCodeRepository.insertCommonCodes(requiredGroupCd, items, changedBy);
        recordCommonCodeVersionChange(
                "REPLACE",
                "common_code_groups",
                requiredGroupCd,
                "공통코드 그룹 및 코드 일괄 교체",
                changedBy
        );
        return ApiResponse.success(null);
    }

    private BwgBusinessException duplicateCode(String code) {
        // 동일 요청에 중복 코드가 있으면 DB 제약조건에 의존하지 않고 업무 오류로 반환한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("field", "items.code", "value", code))
                .build();
    }

    private ApiRequest<CommonCodeGroupReqDto> toGroupUpdateRequest(
            String groupCd,
            CommonCodeGroupReplaceReqDto requestedGroup
    ) {
        // 통합 교체 전용 그룹 모델을 기존 그룹 수정 저장소 계약으로 변환한다.
        CommonCodeGroupReqDto group = new CommonCodeGroupReqDto();
        group.setGroupCd(groupCd);
        group.setGroupNm(requestedGroup.getGroupNm());
        group.setGroupDesc(requestedGroup.getGroupDesc());
        group.setSystemYn(requestedGroup.getSystemYn());
        group.setUseYn(requestedGroup.getUseYn());
        group.setSortSeq(requestedGroup.getSortSeq());

        ApiRequest<CommonCodeGroupReqDto> request = new ApiRequest<>();
        request.setData(group);
        return request;
    }

    private void recordCommonCodeVersionChange(
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    ) {
        // 업무 데이터 변경과 같은 트랜잭션에서 기준정보 버전 이력과 최신 버전을 함께 갱신
        commonCodeRepository.insertReferenceDataVersionHistory(
                REF_TYPE_COMMON_CODE,
                changeType,
                targetTable,
                targetId,
                changeSummary,
                changedBy
        );
        commonCodeRepository.updateReferenceDataVersion(REF_TYPE_COMMON_CODE, changeSummary, changedBy);
    }

    private boolean isAllGroup(String groupCd) {
        // ALL은 전체 공통코드 그룹 상세 조회를 의미하는 예약 groupCd이다.
        return ALL_GROUP_CD.equalsIgnoreCase(groupCd);
    }

    private <T> T requireData(ApiRequest<T> request) {
        // 공통 요청 래퍼의 data 블록 검증
        return BusinessValidator.requireNonNull(request == null ? null : request.getData(), "data");
    }

    private String requireUserId(String userId) {
        // 내부 헤더 누락 시 감사 사용자를 비워 저장하지 않고 요청을 즉시 거부한다.
        return BusinessValidator.requireNonBlank(userId, "userId");
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
