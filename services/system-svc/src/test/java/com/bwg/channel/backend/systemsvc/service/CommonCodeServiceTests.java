package com.bwg.channel.backend.systemsvc.service;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeCreateReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.repository.CommonCodeRepository;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeService;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
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
    void mutationMethodsUseMainMybatisTransactionManager() throws NoSuchMethodException {
        List<Method> mutationMethods = List.of(
                CommonCodeServiceImpl.class.getMethod(
                        "createCommonCodes",
                        ApiRequest.class,
                        String.class
                ),
                CommonCodeServiceImpl.class.getMethod(
                        "replaceCommonCodes",
                        String.class,
                        ApiRequest.class,
                        String.class
                ),
                CommonCodeServiceImpl.class.getMethod(
                        "deleteCommonCodes",
                        String.class,
                        String.class
                )
        );

        // 그룹과 상세코드를 함께 변경하는 API는 모두 같은 MyBatis 트랜잭션 경계를 사용한다.
        assertThat(mutationMethods).allSatisfy(method -> {
            Transactional transactional = method.getAnnotation(Transactional.class);
            assertThat(transactional).isNotNull();
            assertThat(transactional.transactionManager())
                    .isEqualTo("mybatisMainTransactionManager");
        });
    }

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
    void returnsAllCommonCodeGroupDetails() {
        CommonCodeGroupDetailResDto group = new CommonCodeGroupDetailResDto();
        group.setGroupCd("USE_YN");
        CommonCodeResDto code = new CommonCodeResDto();
        code.setGroupCd("USE_YN");
        code.setCode("Y");
        code.setCodeNm("사용");

        when(commonCodeRepository.findCommonCodeGroupDetails()).thenReturn(List.of(group));
        when(commonCodeRepository.findCommonCodeDetails(null)).thenReturn(List.of(code));

        ApiResponse<List<CommonCodeGroupDetailResDto>> response =
                commonCodeService.getCommonCodeGroupDetails();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).singleElement()
                .satisfies(result -> {
                    assertThat(result.getGroupCd()).isEqualTo("USE_YN");
                    assertThat(result.getCodes()).extracting(CommonCodeResDto::getCode)
                            .containsExactly("Y");
                });
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(1);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(1L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
    }

    @Test
    void returnsCommonCodeGroupDetail() {
        CommonCodeGroupDetailResDto group = new CommonCodeGroupDetailResDto();
        group.setGroupCd("USE_YN");
        CommonCodeResDto code = new CommonCodeResDto();
        code.setGroupCd("USE_YN");
        code.setCode("Y");

        when(commonCodeRepository.findCommonCodeGroupDetail("USE_YN")).thenReturn(group);
        when(commonCodeRepository.findCommonCodeDetails("USE_YN")).thenReturn(List.of(code));

        ApiResponse<List<CommonCodeGroupDetailResDto>> response =
                commonCodeService.getCommonCodeGroupDetail(" USE_YN ");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPayload()).singleElement()
                .satisfies(result -> {
                    assertThat(result.getGroupCd()).isEqualTo("USE_YN");
                    assertThat(result.getCodes()).extracting(CommonCodeResDto::getCode)
                            .containsExactly("Y");
                });
    }

    @Test
    void rejectsBlankCommonCodeGroupDetailCode() {
        assertRequiredValueMissing(() -> commonCodeService.getCommonCodeGroupDetail(" "));

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void rejectsMissingCommonCodeGroupDetail() {
        when(commonCodeRepository.findCommonCodeGroupDetail("UNKNOWN")).thenReturn(null);

        assertThatThrownBy(() -> commonCodeService.getCommonCodeGroupDetail("UNKNOWN"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND);

        verify(commonCodeRepository, never()).findCommonCodeDetails("UNKNOWN");
    }

    @Test
    void createsCommonCodeGroupAndCodesWithAuthenticatedUser() {
        CommonCodeReqDto code = commonCode(" Y ", " 사용 ");
        ApiRequest<CommonCodeCreateReqDto> request = createRequest(List.of(code));
        when(commonCodeRepository.insertCommonCodeGroup(request, "jwt-user")).thenReturn(1);
        when(commonCodeRepository.insertCommonCodes("USE_YN", List.of(code), "jwt-user")).thenReturn(1);
        stubSuccessfulVersionChange();

        ApiResponse<Void> response = commonCodeService.createCommonCodes(request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        assertThat(request.getData().getGroupCd()).isEqualTo("USE_YN");
        assertThat(request.getData().getGroupNm()).isEqualTo("사용 여부");
        assertThat(code.getCode()).isEqualTo("Y");
        assertThat(code.getCodeNm()).isEqualTo("사용");
        verify(commonCodeRepository).insertCommonCodeGroup(request, "jwt-user");
        verify(commonCodeRepository).insertCommonCodes("USE_YN", List.of(code), "jwt-user");
        verify(commonCodeRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE",
                "CREATE",
                "common_code_groups",
                "USE_YN",
                "공통코드 그룹 및 코드 통합 등록",
                "jwt-user"
        );
    }

    @Test
    void createsOnlyCommonCodeGroupWhenCodesAreEmpty() {
        ApiRequest<CommonCodeCreateReqDto> request = createRequest(List.of());
        when(commonCodeRepository.insertCommonCodeGroup(request, "jwt-user")).thenReturn(1);
        stubSuccessfulVersionChange();

        ApiResponse<Void> response = commonCodeService.createCommonCodes(request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        verify(commonCodeRepository).insertCommonCodeGroup(request, "jwt-user");
        verify(commonCodeRepository, never()).insertCommonCodes(eq("USE_YN"), any(), eq("jwt-user"));
    }

    @Test
    void rejectsCreateWhenGroupInsertAffectsNoRows() {
        ApiRequest<CommonCodeCreateReqDto> request = createRequest(List.of());

        assertServerError(() -> commonCodeService.createCommonCodes(request, "jwt-user"));

        verify(commonCodeRepository, never()).insertReferenceDataVersionHistory(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void rejectsCreateWhenVersionHistoryAffectsNoRows() {
        ApiRequest<CommonCodeCreateReqDto> request = createRequest(List.of());
        when(commonCodeRepository.insertCommonCodeGroup(request, "jwt-user")).thenReturn(1);

        assertServerError(() -> commonCodeService.createCommonCodes(request, "jwt-user"));

        verify(commonCodeRepository, never()).updateReferenceDataVersion(
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void rejectsCreateWhenLatestVersionUpdateAffectsNoRows() {
        ApiRequest<CommonCodeCreateReqDto> request = createRequest(List.of());
        when(commonCodeRepository.insertCommonCodeGroup(request, "jwt-user")).thenReturn(1);
        when(commonCodeRepository.insertReferenceDataVersionHistory(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(1);

        assertServerError(() -> commonCodeService.createCommonCodes(request, "jwt-user"));
    }

    @Test
    void rejectsNullCreateCodesBeforeWrite() {
        ApiRequest<CommonCodeCreateReqDto> request = createRequest(null);

        assertRequiredValueMissing(() -> commonCodeService.createCommonCodes(request, "jwt-user"));

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void rejectsDuplicateCreateCodesBeforeWrite() {
        ApiRequest<CommonCodeCreateReqDto> request = createRequest(List.of(
                commonCode("Y", "사용"),
                commonCode(" Y ", "사용 중복")
        ));

        assertThatThrownBy(() -> commonCodeService.createCommonCodes(request, "jwt-user"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_RULE_VIOLATION);

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void rejectsBlankAuthenticatedUserBeforeWrite() {
        assertRequiredValueMissing(() -> commonCodeService.createCommonCodes(createRequest(List.of()), " "));

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void replacesCommonCodeGroupAndCodesInUpdateDeleteInsertVersionOrder() {
        CommonCodeGroupDetailResDto group = new CommonCodeGroupDetailResDto();
        group.setGroupCd("USE_YN");
        when(commonCodeRepository.findCommonCodeGroupDetail("USE_YN")).thenReturn(group);

        CommonCodeReqDto useCode = commonCode(" Y ", " 사용 ");
        CommonCodeReqDto unusedCode = commonCode("N", "미사용");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(useCode, unusedCode));
        when(commonCodeRepository.updateCommonCodeGroup("USE_YN", request, "jwt-user")).thenReturn(1);
        when(commonCodeRepository.insertCommonCodes(
                "USE_YN",
                List.of(useCode, unusedCode),
                "jwt-user"
        )).thenReturn(2);
        stubSuccessfulVersionChange();

        ApiResponse<Void> response = commonCodeService.replaceCommonCodes(" USE_YN ", request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        assertThat(useCode.getCode()).isEqualTo("Y");
        assertThat(useCode.getCodeNm()).isEqualTo("사용");

        InOrder inOrder = inOrder(commonCodeRepository);
        inOrder.verify(commonCodeRepository).findCommonCodeGroupDetail("USE_YN");
        inOrder.verify(commonCodeRepository).updateCommonCodeGroup("USE_YN", request, "jwt-user");
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

        assertThat(request.getData().getGroupNm()).isEqualTo("사용 여부");
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
        verify(commonCodeRepository, never()).insertCommonCodes("UNKNOWN", request.getData().getCodes(), "jwt-user");
    }

    @Test
    void rejectsBlankGroupNameBeforeUpdate() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of(commonCode("Y", "사용")));
        request.getData().setGroupNm(" ");

        assertRequiredValueMissing(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

        verify(commonCodeRepository, never()).updateCommonCodeGroup(any(), any(), any());
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
    void emptyCodeListDeletesExistingCodesWithoutInsert() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of());
        when(commonCodeRepository.updateCommonCodeGroup("USE_YN", request, "jwt-user")).thenReturn(1);
        stubSuccessfulVersionChange();

        ApiResponse<Void> response = commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        verify(commonCodeRepository).updateCommonCodeGroup("USE_YN", request, "jwt-user");
        verify(commonCodeRepository).deleteCommonCodesByGroupCd("USE_YN");
        verify(commonCodeRepository, never()).insertCommonCodes(eq("USE_YN"), any(), eq("jwt-user"));
    }

    @Test
    void rejectsReplaceWhenGroupUpdateAffectsNoRowsBeforeCodeDelete() {
        stubExistingGroup("USE_YN");
        ApiRequest<CommonCodeReplaceReqDto> request = replaceRequest(List.of());

        assertServerError(() -> commonCodeService.replaceCommonCodes("USE_YN", request, "jwt-user"));

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

    @Test
    void deletesCommonCodeGroupAndCodesInChildParentVersionOrder() {
        stubExistingGroup("USE_YN");
        when(commonCodeRepository.deleteCommonCodeGroup("USE_YN")).thenReturn(1);
        stubSuccessfulVersionChange();

        ApiResponse<Void> response = commonCodeService.deleteCommonCodes(" USE_YN ", "jwt-user");

        assertThat(response.isSuccess()).isTrue();
        InOrder inOrder = inOrder(commonCodeRepository);
        inOrder.verify(commonCodeRepository).findCommonCodeGroupDetail("USE_YN");
        inOrder.verify(commonCodeRepository).deleteCommonCodesByGroupCd("USE_YN");
        inOrder.verify(commonCodeRepository).deleteCommonCodeGroup("USE_YN");
        inOrder.verify(commonCodeRepository).insertReferenceDataVersionHistory(
                "COMMON_CODE",
                "DELETE",
                "common_code_groups",
                "USE_YN",
                "공통코드 그룹 및 코드 통합 삭제",
                "jwt-user"
        );
        inOrder.verify(commonCodeRepository).updateReferenceDataVersion(
                "COMMON_CODE",
                "공통코드 그룹 및 코드 통합 삭제",
                "jwt-user"
        );
    }

    @Test
    void rejectsDeleteWhenGroupDeleteAffectsNoRows() {
        stubExistingGroup("USE_YN");

        assertServerError(() -> commonCodeService.deleteCommonCodes("USE_YN", "jwt-user"));

        verify(commonCodeRepository, never()).insertReferenceDataVersionHistory(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void rejectsBlankDeleteGroupCodeBeforeRepositoryAccess() {
        assertRequiredValueMissing(() -> commonCodeService.deleteCommonCodes(" ", "jwt-user"));

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void rejectsBlankDeleteUserBeforeRepositoryAccess() {
        assertRequiredValueMissing(() -> commonCodeService.deleteCommonCodes("USE_YN", " "));

        verifyNoInteractions(commonCodeRepository);
    }

    @Test
    void rejectsMissingCommonCodeGroupBeforeGroupDelete() {
        when(commonCodeRepository.findCommonCodeGroupDetail("UNKNOWN")).thenReturn(null);

        assertThatThrownBy(() -> commonCodeService.deleteCommonCodes("UNKNOWN", "jwt-user"))
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND);

        verify(commonCodeRepository, never()).deleteCommonCodesByGroupCd("UNKNOWN");
        verify(commonCodeRepository, never()).deleteCommonCodeGroup("UNKNOWN");
    }

    private void stubExistingGroup(String groupCd) {
        CommonCodeGroupDetailResDto group = new CommonCodeGroupDetailResDto();
        group.setGroupCd(groupCd);
        when(commonCodeRepository.findCommonCodeGroupDetail(groupCd)).thenReturn(group);
    }

    private void stubSuccessfulVersionChange() {
        // 성공 흐름에서는 버전 이력 등록과 최신 버전 갱신이 각각 한 건씩 반영된다.
        when(commonCodeRepository.insertReferenceDataVersionHistory(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(1);
        when(commonCodeRepository.updateReferenceDataVersion(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(1);
    }

    private ApiRequest<CommonCodeReplaceReqDto> replaceRequest(List<CommonCodeReqDto> codes) {
        CommonCodeReplaceReqDto data = new CommonCodeReplaceReqDto();
        data.setGroupNm("사용 여부");
        data.setCodes(codes);
        ApiRequest<CommonCodeReplaceReqDto> request = new ApiRequest<>();
        request.setData(data);
        return request;
    }

    private ApiRequest<CommonCodeCreateReqDto> createRequest(List<CommonCodeReqDto> codes) {
        CommonCodeCreateReqDto data = new CommonCodeCreateReqDto();
        data.setGroupCd(" USE_YN ");
        data.setGroupNm(" 사용 여부 ");
        data.setCodes(codes);
        return request(data);
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

    private void assertServerError(Runnable invocation) {
        assertThatThrownBy(invocation::run)
                .isInstanceOf(BwgBusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessErrorCode.SERVER_ERROR);
    }
}
