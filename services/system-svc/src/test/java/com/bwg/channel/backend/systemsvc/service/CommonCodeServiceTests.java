package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.repository.CommonCodeRepository;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeService;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 공통코드 기준정보 서비스 조회 흐름 검증
 */
class CommonCodeServiceTests {

    private final CommonCodeRepository commonCodeRepository = mock(CommonCodeRepository.class);
    private final CommonCodeService commonCodeService = new CommonCodeServiceImpl(commonCodeRepository);

    @Test
    void returnsCommonCodeGroups() {
        CommonCodeGroupResDto group = new CommonCodeGroupResDto();
        group.setGroupCd("USE_YN");
        group.setGroupNm("사용 여부");

        when(commonCodeRepository.findCommonCodeGroups()).thenReturn(List.of(group));

        ApiResponse<List<CommonCodeGroupResDto>> response = commonCodeService.getCommonCodeGroups();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(CommonCodeGroupResDto::getGroupCd)
                .containsExactly("USE_YN");
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(1);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(1L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void returnsCommonCodesByGroupCode() {
        CommonCodeResDto code = new CommonCodeResDto();
        code.setGroupCd("USE_YN");
        code.setCode("Y");
        code.setCodeNm("사용");

        when(commonCodeRepository.findCommonCodes("USE_YN")).thenReturn(List.of(code));

        ApiResponse<List<CommonCodeResDto>> response = commonCodeService.getCommonCodes("USE_YN");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).extracting(CommonCodeResDto::getCode)
                .containsExactly("Y");
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(1);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(1L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }
}
