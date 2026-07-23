package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.systemsvc.referencedata.repository.ReferenceDataVersionRepository;
import com.bwg.channel.backend.systemsvc.referencedata.service.ReferenceDataVersionService;
import com.bwg.channel.backend.systemsvc.referencedata.service.ReferenceDataVersionServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 기준정보 버전 변경 이력과 최신 버전 갱신의 공통 서비스 흐름 검증
 */
class ReferenceDataVersionServiceTests {

    private final ReferenceDataVersionRepository referenceDataVersionRepository =
            mock(ReferenceDataVersionRepository.class);
    private final ReferenceDataVersionService referenceDataVersionService =
            new ReferenceDataVersionServiceImpl(referenceDataVersionRepository);

    @Test
    void recordsHistoryBeforeUpdatingLatestVersion() {
        when(referenceDataVersionRepository.insertReferenceDataVersionHistory(
                "COMMON_CODE",
                "CREATE",
                "common_code_groups",
                "USE_YN",
                "공통코드 그룹 및 코드 통합 등록",
                "jwt-user"
        )).thenReturn(1);
        when(referenceDataVersionRepository.updateReferenceDataVersion(
                "COMMON_CODE",
                "공통코드 그룹 및 코드 통합 등록",
                "jwt-user"
        )).thenReturn(1);

        referenceDataVersionService.versionChange(
                "COMMON_CODE",
                "CREATE",
                "common_code_groups",
                "USE_YN",
                "공통코드 그룹 및 코드 통합 등록",
                "jwt-user"
        );

        InOrder inOrder = inOrder(referenceDataVersionRepository);
        inOrder.verify(referenceDataVersionRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE",
                "CREATE",
                "common_code_groups",
                "USE_YN",
                "공통코드 그룹 및 코드 통합 등록",
                "jwt-user"
        );
        inOrder.verify(referenceDataVersionRepository).updateReferenceDataVersion(
                "COMMON_CODE",
                "공통코드 그룹 및 코드 통합 등록",
                "jwt-user"
        );
    }

    @Test
    void rejectsVersionChangeWhenHistoryInsertAffectsNoRows() {
        assertServerError(() -> referenceDataVersionService.versionChange(
                "MENU",
                "UPDATE",
                "menus",
                "7",
                "메뉴 수정",
                "jwt-user"
        ));

        verify(referenceDataVersionRepository, never()).updateReferenceDataVersion(
                "MENU",
                "메뉴 수정",
                "jwt-user"
        );
    }

    @Test
    void rejectsVersionChangeWhenLatestVersionUpdateAffectsNoRows() {
        when(referenceDataVersionRepository.insertReferenceDataVersionHistory(
                "MENU",
                "UPDATE",
                "menus",
                "7",
                "메뉴 수정",
                "jwt-user"
        )).thenReturn(1);

        assertServerError(() -> referenceDataVersionService.versionChange(
                "MENU",
                "UPDATE",
                "menus",
                "7",
                "메뉴 수정",
                "jwt-user"
        ));

        verify(referenceDataVersionRepository).updateReferenceDataVersion(
                "MENU",
                "메뉴 수정",
                "jwt-user"
        );
    }

    /**
     * 기준정보 버전 SQL 반영 건수 불일치를 공통 서버 오류로 검증한다.
     *
     * @param invocation 검증할 버전 변경 호출
     */
    private void assertServerError(Runnable invocation) {
        assertThatThrownBy(invocation::run)
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.SERVER_ERROR);
    }
}
