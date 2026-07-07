package com.bwg.channel.backend.systemsvc.referencedata.repository.mybatis;

import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;
import com.bwg.channel.backend.systemsvc.referencedata.repository.ReferenceDataVersionRepository;
import com.bwg.channel.backend.systemsvc.referencedata.repository.mybatis.mapper.ReferenceDataVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ReferenceDataVersionRepository 계약을 MyBatis Mapper 호출로 연결하는 어댑터.
 */
@Repository("mybatisReferenceDataVersionRepositoryAdapter")
@RequiredArgsConstructor
public class MybatisReferenceDataVersionRepositoryAdapter implements ReferenceDataVersionRepository {

    private final ReferenceDataVersionMapper referenceDataVersionMapper;

    @Override
    public List<ReferenceDataVersionResDto> findLatestReferenceDataVersions() {
        // MyBatis Mapper를 통해 전체 기준정보 최신 버전 목록 조회
        return referenceDataVersionMapper.findLatestReferenceDataVersions();
    }

    @Override
    public List<ReferenceDataVersionResDto> findLatestReferenceDataVersionsByRefType(String refType) {
        // MyBatis Mapper를 통해 기준정보 유형별 최신 버전 조회
        return referenceDataVersionMapper.findLatestReferenceDataVersionsByRefType(refType);
    }
}
