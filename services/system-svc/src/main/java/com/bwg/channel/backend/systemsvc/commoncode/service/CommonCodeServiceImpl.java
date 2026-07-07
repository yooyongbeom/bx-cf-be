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

import java.util.List;

/**
 * 공통코드 그룹과 공통코드의 입력값을 검증하고 저장소 처리를 위임하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class CommonCodeServiceImpl implements CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;

    @Override
    public ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups() {
        // 저장소에서 공통코드 그룹 목록 조회
        List<CommonCodeGroupResDto> result = commonCodeRepository.findCommonCodeGroups();
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
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
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<List<CommonCodeResDto>> getCommonCodes(String groupCd) {
        // 그룹 코드 검증 후 공통코드 목록 조회
        List<CommonCodeResDto> result = commonCodeRepository.findCommonCodes(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        // 조회 결과에 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, toPagination(result));
    }

    @Override
    public ApiResponse<CommonCodeGroupDetailResDto> getCommonCodeGroupDetail(String groupCd) {
        // 그룹 코드 검증 후 공통코드 그룹 상세 정보 조회
        String requiredGroupCd = BusinessValidator.requireNonBlank(groupCd, "groupCd");
        CommonCodeGroupDetailResDto result = BusinessValidator.requireNonNull(
                commonCodeRepository.findCommonCodeGroupDetail(requiredGroupCd),
                "commonCodeGroup"
        );
        // 공통코드 그룹에 하위 코드 목록을 조립
        result.setCodes(commonCodeRepository.findCommonCodeDetails(requiredGroupCd));
        return ApiResponse.success(result);
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
        return ApiResponse.success(null);
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
