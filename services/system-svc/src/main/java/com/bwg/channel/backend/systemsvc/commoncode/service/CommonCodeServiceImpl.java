package com.bwg.channel.backend.systemsvc.commoncode.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public ApiResponse<Void> createCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeGroupReqDto data = requireData(paramDto);
        // 등록 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(data.getGroupCd(), "groupCd"));
        data.setGroupNm(BusinessValidator.requireNonBlank(data.getGroupNm(), "groupNm"));
        // 공통코드 그룹 등록 처리
        commonCodeRepository.insertCommonCodeGroup(paramDto);
        recordCommonCodeVersionChange(
                "CREATE",
                "common_code_groups",
                data.getGroupCd(),
                "공통코드 그룹 등록",
                data.getCreatedBy()
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCodeGroup(String groupCd, ApiRequest<CommonCodeGroupReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeGroupReqDto data = requireData(paramDto);
        // 경로 변수와 수정 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        data.setGroupNm(BusinessValidator.requireNonBlank(data.getGroupNm(), "groupNm"));
        // 공통코드 그룹 수정 처리
        commonCodeRepository.updateCommonCodeGroup(paramDto);
        recordCommonCodeVersionChange(
                "UPDATE",
                "common_code_groups",
                data.getGroupCd(),
                "공통코드 그룹 수정",
                data.getCreatedBy()
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
    public ApiResponse<Void> createCommonCode(String groupCd, ApiRequest<CommonCodeReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeReqDto data = requireData(paramDto);
        // 경로 변수와 등록 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        data.setCode(BusinessValidator.requireNonBlank(data.getCode(), "code"));
        data.setCodeNm(BusinessValidator.requireNonBlank(data.getCodeNm(), "codeNm"));
        // 공통코드 등록 처리
        commonCodeRepository.insertCommonCode(paramDto);
        recordCommonCodeVersionChange(
                "CREATE",
                "common_codes",
                data.getGroupCd() + ":" + data.getCode(),
                "공통코드 등록",
                data.getCreatedBy()
        );
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCode(String groupCd, String code, ApiRequest<CommonCodeReqDto> paramDto) {
        // 요청 본문 데이터 필수 여부 검증
        CommonCodeReqDto data = requireData(paramDto);
        // 경로 변수와 수정 필수값 검증
        data.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        data.setCode(BusinessValidator.requireNonBlank(code, "code"));
        data.setCodeNm(BusinessValidator.requireNonBlank(data.getCodeNm(), "codeNm"));
        // 공통코드 수정 처리
        commonCodeRepository.updateCommonCode(paramDto);
        recordCommonCodeVersionChange(
                "UPDATE",
                "common_codes",
                data.getGroupCd() + ":" + data.getCode(),
                "공통코드 수정",
                data.getCreatedBy()
        );
        return ApiResponse.success(null);
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
