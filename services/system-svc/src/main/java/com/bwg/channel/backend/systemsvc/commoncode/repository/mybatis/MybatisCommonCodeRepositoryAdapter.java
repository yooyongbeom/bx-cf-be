package com.bwg.channel.backend.systemsvc.commoncode.repository.mybatis;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeCreateReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
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
    public int insertCommonCodeGroup(ApiRequest<CommonCodeCreateReqDto> paramDto, String createdBy) {
        // 요청 업무 데이터와 검증된 등록자를 분리하여 MyBatis Mapper에 전달한다.
        return commonCodeMapper.insertCommonCodeGroup(paramDto, createdBy);
    }

    @Override
    public int updateCommonCodeGroup(
            String groupCd,
            ApiRequest<CommonCodeReplaceReqDto> paramDto,
            String updatedBy
    ) {
        // 경로 그룹 코드, 평탄화 요청 데이터와 검증된 수정자를 Mapper에 전달한다.
        return commonCodeMapper.updateCommonCodeGroup(groupCd, paramDto, updatedBy);
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
    public int deleteCommonCodesByGroupCd(String groupCd) {
        // 교체 등록 전에 해당 그룹의 기존 공통코드를 한 번에 삭제한다.
        return commonCodeMapper.deleteCommonCodesByGroupCd(groupCd);
    }

    @Override
    public int deleteCommonCodeGroup(String groupCd) {
        // 하위 상세코드가 제거된 그룹을 MyBatis Mapper를 통해 삭제한다.
        return commonCodeMapper.deleteCommonCodeGroup(groupCd);
    }

    @Override
    public int insertCommonCodes(String groupCd, List<CommonCodeReqDto> codes, String createdBy) {
        // 검증된 전체 목록을 MyBatis 배치 INSERT SQL에 전달한다.
        return commonCodeMapper.insertCommonCodes(groupCd, codes, createdBy);
    }

}
