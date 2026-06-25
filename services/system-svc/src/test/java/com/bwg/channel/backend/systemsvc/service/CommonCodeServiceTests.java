package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupDto;
import com.bwg.channel.backend.systemsvc.repository.SystemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 공통코드 기준정보 서비스 조회 흐름 검증
 */
class CommonCodeServiceTests {

    private final SystemRepository systemRepository = mock(SystemRepository.class);
    private final CommonCodeService commonCodeService = new CommonCodeServiceImpl(systemRepository);

    @Test
    void returnsCommonCodeGroups() {
        CommonCodeGroupDto group = new CommonCodeGroupDto();
        group.setGroupCd("USE_YN");
        group.setGroupNm("사용 여부");

        when(systemRepository.findCommonCodeGroups()).thenReturn(List.of(group));

        ApiResponse<List<CommonCodeGroupDto>> response = commonCodeService.getCommonCodeGroups();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(CommonCodeGroupDto::getGroupCd)
                .containsExactly("USE_YN");
    }

    @Test
    void returnsCommonCodesByGroupCode() {
        CommonCodeDto code = new CommonCodeDto();
        code.setGroupCd("USE_YN");
        code.setCode("Y");
        code.setCodeNm("사용");

        when(systemRepository.findCommonCodes("USE_YN")).thenReturn(List.of(code));

        ApiResponse<List<CommonCodeDto>> response = commonCodeService.getCommonCodes("USE_YN");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(CommonCodeDto::getCode)
                .containsExactly("Y");
    }
}
