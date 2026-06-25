package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupDto;
import com.bwg.channel.backend.systemsvc.repository.SystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공통코드 그룹과 코드의 입력값을 검증하고 저장소 처리를 위임하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class CommonCodeServiceImpl implements CommonCodeService {

    private final SystemRepository systemRepository;

    /**
     * 공통코드 그룹 목록을 조회해 공통 응답으로 반환한다.
     */
    @Override
    public ApiResponse<List<CommonCodeGroupDto>> getCommonCodeGroups() {
        return ApiResponse.success(systemRepository.findCommonCodeGroups());
    }

    /**
     * 필수값을 검증한 뒤 공통코드 그룹을 등록한다.
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createCommonCodeGroup(CommonCodeGroupDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(paramDto.getGroupCd(), "groupCd"));
        paramDto.setGroupNm(BusinessValidator.requireNonBlank(paramDto.getGroupNm(), "groupNm"));
        systemRepository.insertCommonCodeGroup(paramDto);
        return ApiResponse.success(null);
    }

    /**
     * 경로의 그룹 코드를 적용하고 공통코드 그룹명을 수정한다.
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCodeGroup(String groupCd, CommonCodeGroupDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        paramDto.setGroupNm(BusinessValidator.requireNonBlank(paramDto.getGroupNm(), "groupNm"));
        systemRepository.updateCommonCodeGroup(paramDto);
        return ApiResponse.success(null);
    }

    /**
     * 그룹 코드를 검증한 뒤 해당 그룹의 공통코드를 조회한다.
     */
    @Override
    public ApiResponse<List<CommonCodeDto>> getCommonCodes(String groupCd) {
        return ApiResponse.success(systemRepository.findCommonCodes(BusinessValidator.requireNonBlank(groupCd, "groupCd")));
    }

    /**
     * 그룹 코드와 코드 필수값을 검증한 뒤 공통코드를 등록한다.
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createCommonCode(String groupCd, CommonCodeDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        paramDto.setCode(BusinessValidator.requireNonBlank(paramDto.getCode(), "code"));
        paramDto.setCodeNm(BusinessValidator.requireNonBlank(paramDto.getCodeNm(), "codeNm"));
        systemRepository.insertCommonCode(paramDto);
        return ApiResponse.success(null);
    }

    /**
     * 그룹 코드와 코드 값을 경로 기준으로 고정한 뒤 공통코드를 수정한다.
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCode(String groupCd, String code, CommonCodeDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        paramDto.setCode(BusinessValidator.requireNonBlank(code, "code"));
        paramDto.setCodeNm(BusinessValidator.requireNonBlank(paramDto.getCodeNm(), "codeNm"));
        systemRepository.updateCommonCode(paramDto);
        return ApiResponse.success(null);
    }
}
