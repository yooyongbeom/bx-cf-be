package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.repository.CommonCodeRepository;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeService;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Test
    void createsCommonCodeGroupWithAuthenticatedUser() {
        CommonCodeGroupReqDto data = new CommonCodeGroupReqDto();
        data.setGroupCd(" USE_YN ");
        data.setGroupNm(" 사용 여부 ");
        ApiRequest<CommonCodeGroupReqDto> request = request(data);

        ApiResponse<Void> response = commonCodeService.createCommonCodeGroup(request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        verify(commonCodeRepository).insertCommonCodeGroup(request, "jwt-user");
        verify(commonCodeRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE", "CREATE", "common_code_groups", "USE_YN", "공통코드 그룹 등록", "jwt-user"
        );
    }

    @Test
    void updatesCommonCodeGroupWithAuthenticatedUser() {
        CommonCodeGroupReqDto data = new CommonCodeGroupReqDto();
        data.setGroupNm(" 사용 여부 ");
        ApiRequest<CommonCodeGroupReqDto> request = request(data);

        ApiResponse<Void> response = commonCodeService.updateCommonCodeGroup(" USE_YN ", request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        verify(commonCodeRepository).updateCommonCodeGroup(request, "jwt-user");
        verify(commonCodeRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE", "UPDATE", "common_code_groups", "USE_YN", "공통코드 그룹 수정", "jwt-user"
        );
    }

    @Test
    void createsCommonCodeWithAuthenticatedUser() {
        CommonCodeReqDto data = commonCode(" Y ", " 사용 ");
        ApiRequest<CommonCodeReqDto> request = request(data);

        ApiResponse<Void> response = commonCodeService.createCommonCode(" USE_YN ", request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        verify(commonCodeRepository).insertCommonCode(request, "jwt-user");
        verify(commonCodeRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE", "CREATE", "common_codes", "USE_YN:Y", "공통코드 등록", "jwt-user"
        );
    }

    @Test
    void updatesCommonCodeWithAuthenticatedUser() {
        CommonCodeReqDto data = commonCode(null, " 사용 ");
        ApiRequest<CommonCodeReqDto> request = request(data);

        ApiResponse<Void> response = commonCodeService.updateCommonCode(
                " USE_YN ", " Y ", request, "jwt-user"
        );

        assertThat(response.isSuccess()).isTrue();
        verify(commonCodeRepository).updateCommonCode(request, "jwt-user");
        verify(commonCodeRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE", "UPDATE", "common_codes", "USE_YN:Y", "공통코드 수정", "jwt-user"
        );
    }

    @Test
    void rejectsBlankAuthenticatedUserBeforeWrite() {
        CommonCodeGroupReqDto data = new CommonCodeGroupReqDto();
        data.setGroupCd("USE_YN");
        data.setGroupNm("사용 여부");

        assertRequiredValueMissing(() -> commonCodeService.createCommonCodeGroup(request(data), " "));

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void replacesCommonCodeGroupAndItemsInUpdateDeleteInsertVersionOrder() {
        CommonCodeGroupDetailResDto group = new CommonCodeGroupDetailResDto();
        group.setGroupCd("USE_YN");
        when(commonCodeRepository.findCommonCodeGroupDetail("USE_YN")).thenReturn(group);

        CommonCodeReqDto useCode = commonCode(" Y ", " 사용 ");
        CommonCodeReqDto unusedCode = commonCode("N", "미사용");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(useCode, unusedCode));

        ApiResponse<Void> response = commonCodeService.replaceCommonCodes(" USE_YN ", request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        assertThat(useCode.getCode()).isEqualTo("Y");
        assertThat(useCode.getCodeNm()).isEqualTo("사용");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ApiRequest<CommonCodeGroupReqDto>> groupRequestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        InOrder inOrder = inOrder(commonCodeRepository);
        inOrder.verify(commonCodeRepository).findCommonCodeGroupDetail("USE_YN");
        inOrder.verify(commonCodeRepository).updateCommonCodeGroup(groupRequestCaptor.capture(), eq("jwt-user"));
        inOrder.verify(commonCodeRepository).deleteCommonCodesByGroupCd("USE_YN");
        inOrder.verify(commonCodeRepository).insertCommonCodes(
                "USE_YN",
                List.of(useCode, unusedCode),
                "jwt-user"
        );
        inOrder.verify(commonCodeRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE",
                "REPLACE",
                "common_code_groups",
                "USE_YN",
                "공통코드 그룹 및 코드 일괄 교체",
                "jwt-user"
        );
        inOrder.verify(commonCodeRepository).updateReferenceDataVersion(
                "COMMON_CODE",
                "공통코드 그룹 및 코드 일괄 교체",
                "jwt-user"
        );

        CommonCodeGroupReqDto updatedGroup = groupRequestCaptor.getValue().getData();
        assertThat(updatedGroup.getGroupCd()).isEqualTo("USE_YN");
        assertThat(updatedGroup.getGroupNm()).isEqualTo("사용 여부");
    }

    @Test
    void rejectsBlankGroupCodeBeforeRepositoryAccess() {
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(commonCode("Y", "사용")));

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes(" ", request, "jwt-user"));

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void rejectsMissingCommonCodeGroupBeforeDelete() {
        when(commonCodeRepository.findCommonCodeGroupDetail("UNKNOWN")).thenReturn(null);
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(commonCode("Y", "사용")));

        assertThatThrownBy(() -> commonCodeService.replaceCommonCodes("UNKNOWN", request, "jwt-user"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND);

        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("UNKNOWN");
        verify(commonCodeRepository, never()).insertCommonCodes("UNKNOWN", request.getData().getItems(), "jwt-user");
    }

    @Test
    void rejectsMissingGroupBeforeUpdate() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(commonCode("Y", "사용")));
        request.getData().setGroup(null);

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

        verify(commonCodeRepository, never()).updateCommonCodeGroup(any(), any());
        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("USE_YN");
    }

    @Test
    void rejectsBlankGroupNameBeforeUpdate() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(commonCode("Y", "사용")));
        request.getData().getGroup().setGroupNm(" ");

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

        verify(commonCodeRepository, never()).updateCommonCodeGroup(any(), any());
        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("USE_YN");
    }

    @Test
    void rejectsNullCodeListBeforeDelete() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(null);

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("USE_YN");
    }

    @Test
    void rejectsEmptyCodeListBeforeDelete() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of());

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("USE_YN");
    }

    @Test
    void rejectsBlankCodeBeforeDelete() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(commonCode(" ", "사용")));

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("USE_YN");
    }

    @Test
    void rejectsBlankCodeNameBeforeDelete() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(commonCode("Y", " ")));

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("USE_YN");
    }

    @Test
    void rejectsDuplicateCodesBeforeDelete() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(
                commonCode("Y", "사용"),
                commonCode(" Y ", "사용 중복")
        ));

        assertThatThrownBy(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_RULE_VIOLATION);

        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("USE_YN");
    }

    private void stubExistingGroup(String groupCd) {
        CommonCodeGroupDetailResDto group = new CommonCodeGroupDetailResDto();
        group.setGroupCd(groupCd);
        when(commonCodeRepository.findCommonCodeGroupDetail(groupCd)).thenReturn(group);
    }

    private ApiRequest<CommonCodeReplaceReqDto> replaceRequest(List<CommonCodeReqDto> items) {
        CommonCodeGroupReplaceReqDto group = new CommonCodeGroupReplaceReqDto();
        group.setGroupNm("사용 여부");
        CommonCodeReplaceReqDto data = new CommonCodeReplaceReqDto();
        data.setGroup(group);
        data.setItems(items);
        ApiRequest<CommonCodeReplaceReqDto> request = new ApiRequest<>();
        request.setData(data);
        return request;
    }

    private CommonCodeReqDto commonCode(String code, String codeNm) {
        CommonCodeReqDto data = new CommonCodeReqDto();
        data.setCode(code);
        data.setCodeNm(codeNm);
        return data;
    }

    private <T> ApiRequest<T> request(T data) {
        ApiRequest<T> request = new ApiRequest<>();
        request.setData(data);
        return request;
    }

    private void assertRequiredValueMissing(Runnable invocation) {
        assertThatThrownBy(invocation::run)
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);
    }
}
