package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeResDto;
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

    @Override
    public ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups() {
        return ApiResponse.success(systemRepository.findCommonCodeGroups());
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createCommonCodeGroup(CommonCodeGroupReqDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(paramDto.getGroupCd(), "groupCd"));
        paramDto.setGroupNm(BusinessValidator.requireNonBlank(paramDto.getGroupNm(), "groupNm"));
        systemRepository.insertCommonCodeGroup(paramDto);
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCodeGroup(String groupCd, CommonCodeGroupReqDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        paramDto.setGroupNm(BusinessValidator.requireNonBlank(paramDto.getGroupNm(), "groupNm"));
        systemRepository.updateCommonCodeGroup(paramDto);
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<List<CommonCodeResDto>> getCommonCodes(String groupCd) {
        return ApiResponse.success(systemRepository.findCommonCodes(BusinessValidator.requireNonBlank(groupCd, "groupCd")));
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createCommonCode(String groupCd, CommonCodeReqDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        paramDto.setCode(BusinessValidator.requireNonBlank(paramDto.getCode(), "code"));
        paramDto.setCodeNm(BusinessValidator.requireNonBlank(paramDto.getCodeNm(), "codeNm"));
        systemRepository.insertCommonCode(paramDto);
        return ApiResponse.success(null);
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> updateCommonCode(String groupCd, String code, CommonCodeReqDto paramDto) {
        paramDto.setGroupCd(BusinessValidator.requireNonBlank(groupCd, "groupCd"));
        paramDto.setCode(BusinessValidator.requireNonBlank(code, "code"));
        paramDto.setCodeNm(BusinessValidator.requireNonBlank(paramDto.getCodeNm(), "codeNm"));
        systemRepository.updateCommonCode(paramDto);
        return ApiResponse.success(null);
    }
}
