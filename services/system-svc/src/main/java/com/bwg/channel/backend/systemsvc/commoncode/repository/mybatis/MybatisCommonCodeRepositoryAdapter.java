package com.bwg.channel.backend.systemsvc.commoncode.repository.mybatis;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.repository.CommonCodeRepository;
import com.bwg.channel.backend.systemsvc.commoncode.repository.mybatis.mapper.CommonCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CommonCodeRepository 계약을 MyBatis Mapper 호출로 연결하는 어댑터.
 */
@Repository("mybatisCommonCode")
@RequiredArgsConstructor
public class MybatisCommonCodeRepositoryAdapter implements CommonCodeRepository {

    private final CommonCodeMapper commonCodeMapper;

    @Override
    public List<CommonCodeGroupResDto> findCommonCodeGroups() {
        // MyBatis Mapper를 통해 공통코드 그룹 목록 조회
        return commonCodeMapper.findCommonCodeGroups();
    }

    @Override
    public int insertCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto) {
        // MyBatis Mapper를 통해 공통코드 그룹 등록
        return commonCodeMapper.insertCommonCodeGroup(paramDto);
    }

    @Override
    public int updateCommonCodeGroup(ApiRequest<CommonCodeGroupReqDto> paramDto) {
        // MyBatis Mapper를 통해 공통코드 그룹 수정
        return commonCodeMapper.updateCommonCodeGroup(paramDto);
    }

    @Override
    public List<CommonCodeResDto> findCommonCodes(String groupCd) {
        // MyBatis Mapper를 통해 그룹별 공통코드 목록 조회
        return commonCodeMapper.findCommonCodes(groupCd);
    }

    @Override
    public CommonCodeGroupDetailResDto findCommonCodeGroupDetail(String groupCd) {
        // MyBatis Mapper를 통해 공통코드 그룹 상세 정보 조회
        return commonCodeMapper.findCommonCodeGroupDetail(groupCd);
    }

    @Override
    public List<CommonCodeGroupDetailResDto> findCommonCodeGroupDetails() {
        // MyBatis Mapper를 통해 전체 공통코드 그룹 상세 정보 조회
        return commonCodeMapper.findCommonCodeGroupDetails();
    }

    @Override
    public List<CommonCodeResDto> findCommonCodeDetails(String groupCd) {
        // MyBatis Mapper를 통해 공통코드 상세 항목 목록 조회
        return commonCodeMapper.findCommonCodeDetails(groupCd);
    }

    @Override
    public int insertCommonCode(ApiRequest<CommonCodeReqDto> paramDto) {
        // MyBatis Mapper를 통해 공통코드 등록
        return commonCodeMapper.insertCommonCode(paramDto);
    }

    @Override
    public int updateCommonCode(ApiRequest<CommonCodeReqDto> paramDto) {
        // MyBatis Mapper를 통해 공통코드 수정
        return commonCodeMapper.updateCommonCode(paramDto);
    }

    @Override
    public int insertReferenceDataVersionHistory(
            String refType,
            String changeType,
            String targetTable,
            String targetId,
            String changeSummary,
            String changedBy
    ) {
        // MyBatis Mapper를 통해 기준정보 버전 변경 이력 등록
        return commonCodeMapper.insertReferenceDataVersionHistory(
                refType,
                changeType,
                targetTable,
                targetId,
                changeSummary,
                changedBy
        );
    }

    @Override
    public int updateReferenceDataVersion(String refType, String remark, String changedBy) {
        // MyBatis Mapper를 통해 기준정보 최신 버전 갱신
        return commonCodeMapper.updateReferenceDataVersion(refType, remark, changedBy);
    }
}
