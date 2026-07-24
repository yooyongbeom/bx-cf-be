package com.bwg.channel.backend.systemsvc.commoncode.service;

import com.bwg.channel.backend.businesscommon.constants.BusinessErrorCode;
import com.bwg.channel.backend.businesscommon.exception.BwgBusinessException;
import com.bwg.channel.backend.businesscommon.validation.BusinessValidator;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.util.PageUtil;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeCreateReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.repository.CommonCodeRepository;
import com.bwg.channel.backend.systemsvc.referencedata.service.ReferenceDataVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 공통코드 그룹과 공통코드의 입력값을 검증하고 저장소 처리를 위임하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class CommonCodeServiceImpl implements CommonCodeService {

    private static final String REF_TYPE_COMMON_CODE = "COMMON_CODE";

    private final CommonCodeRepository commonCodeRepository;
    private final ReferenceDataVersionService referenceDataVersionService;

    /**
     * 공통코드 그룹의 기본 정보 목록을 조회한다.
     *
     * @return 그룹 목록과 전체 목록 기준 페이지 정보가 포함된 응답
     */
    @Override
    public ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups() {
        // 저장소에서 공통코드 그룹 목록 조회
        List<CommonCodeGroupResDto> result = commonCodeRepository.findCommonCodeGroups();
        // 조회 결과와 목록 메타데이터를 포함하여 응답 생성
        return ApiResponse.success(result, PageUtil.singlePage(result));
    }

    /**
     * 그룹을 먼저 등록한 뒤 요청된 상세코드를 동일 트랜잭션에서 일괄 등록한다.
     *
     * <p>{@code codes}는 필수 필드지만 빈 배열을 허용하며, 빈 배열이면 그룹만 등록한다.
     * 등록 완료 후 공통코드 기준정보 버전을 한 번 갱신한다.</p>
     *
     * @param paramDto 등록할 그룹 정보와 상세코드 목록
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 등록 성공 응답
     * @throws BwgBusinessException 필수값이 없거나 DB 반영 건수가 예상과 다른 경우
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> createCommonCodes(
            ApiRequest<CommonCodeCreateReqDto> paramDto,
            String userId
    ) {
        // 그룹과 상세코드 시스템 필드에는 Gateway가 검증한 사용자만 사용한다.
        String changedBy = BusinessValidator.requireNonBlank(userId, "userId");
        CommonCodeCreateReqDto data = BusinessValidator.requireData(paramDto);
        // 데이터 변경 전에 그룹과 전체 상세코드 목록을 검증한다.
        data.setGroupCd(BusinessValidator.requireNonBlank(data.getGroupCd(), "groupCd"));
        data.setGroupNm(BusinessValidator.requireNonBlank(data.getGroupNm(), "groupNm"));
        List<CommonCodeReqDto> codes = validateCommonCodes(data.getCodes());

        // 그룹을 먼저 등록하고 목록이 있을 때만 동일 트랜잭션에서 상세코드를 등록한다.
        BusinessValidator.requireAffectedRows(
                commonCodeRepository.insertCommonCodeGroup(paramDto, changedBy),
                1,
                "insertCommonCodeGroup"
        );
        if (!codes.isEmpty()) {
            BusinessValidator.requireAffectedRows(
                    commonCodeRepository.insertCommonCodes(data.getGroupCd(), codes, changedBy),
                    codes.size(),
                    "insertCommonCodes"
            );
        }
        referenceDataVersionService.versionChange(
                REF_TYPE_COMMON_CODE,
                "CREATE",
                "common_code_groups",
                data.getGroupCd(),
                "공통코드 그룹 및 코드 통합 등록",
                changedBy
        );
        return ApiResponse.success(null);
    }

    /**
     * 전체 공통코드 그룹과 각 그룹에 속한 상세코드를 함께 조회한다.
     *
     * @return 전체 그룹 및 상세코드 목록과 페이지 정보가 포함된 응답
     */
    @Override
    public ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetails() {
        List<CommonCodeGroupDetailResDto> groups = commonCodeRepository.findCommonCodeGroupDetails();
        // 전체 상세코드를 그룹별로 결합한 뒤 목록 응답에만 페이지 정보를 포함한다.
        attachCommonCodeDetails(groups, null);
        return ApiResponse.success(groups, PageUtil.singlePage(groups));
    }

    /**
     * 지정한 그룹과 해당 그룹에 속한 상세코드를 함께 조회한다.
     *
     * @param groupCd 조회할 공통코드 그룹 코드
     * @return 요청 그룹 한 건을 object 형태로 포함한 응답
     * @throws BwgBusinessException 그룹 코드가 비어 있거나 대상 그룹이 존재하지 않는 경우
     */
    @Override
    public ApiResponse<CommonCodeGroupDetailResDto> getCommonCodeGroupDetail(String groupCd) {
        String requiredGroupCd = BusinessValidator.requireNonBlank(groupCd, "groupCd");
        CommonCodeGroupDetailResDto group = BusinessValidator.requireFound(
                commonCodeRepository.findCommonCodeGroupDetail(requiredGroupCd),
                "commonCodeGroup"
        );
        // 단건 상세의 내부 codes만 배열로 조립하고 payload 자체는 그룹 object로 유지한다.
        attachCommonCodeDetails(List.of(group), requiredGroupCd);
        return ApiResponse.success(group);
    }

    /**
     * 조회된 그룹에 그룹별 상세코드 목록을 결합한다.
     *
     * @param groups 상세코드를 결합할 그룹 목록
     * @param groupCd 조회할 그룹 코드, 전체 상세코드 조회 시 {@code null}
     */
    private void attachCommonCodeDetails(
            List<CommonCodeGroupDetailResDto> groups,
            String groupCd
    ) {
        // 전체 또는 지정 그룹의 상세코드를 그룹 코드 기준으로 묶어 응답 DTO에 연결한다.
        Map<String, List<CommonCodeResDto>> codesByGroupCd = commonCodeRepository
                .findCommonCodeDetails(groupCd)
                .stream()
                .collect(Collectors.groupingBy(
                        CommonCodeResDto::getGroupCd,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        groups.forEach(group -> group.setCodes(codesByGroupCd.getOrDefault(group.getGroupCd(), List.of())));
    }

    /**
     * 그룹 정보를 수정하고 기존 상세코드를 삭제한 뒤 요청 목록으로 다시 등록한다.
     *
     * <p>{@code codes}가 빈 배열이면 상세코드 재등록을 생략하므로 기존 상세코드 전체 삭제로
     * 처리된다. 그룹 수정, 상세코드 교체, 버전 갱신은 동일 트랜잭션에서 수행한다.</p>
     *
     * @param groupCd 교체할 공통코드 그룹 코드
     * @param paramDto 수정할 그룹 정보와 새 상세코드 목록
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 교체 성공 응답
     * @throws BwgBusinessException 필수값 누락, 대상 그룹 미존재 또는 DB 반영 건수 불일치 시
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> replaceCommonCodes(
            String groupCd,
            ApiRequest<CommonCodeReplaceReqDto> paramDto,
            String userId
    ) {
        // 그룹 수정, 코드 교체, 변경 이력에 동일한 검증 사용자를 적용한다.
        String changedBy = BusinessValidator.requireNonBlank(userId, "userId");
        // 경로의 그룹 코드를 먼저 정규화하고 실제 존재하는 공통코드 그룹인지 확인한다.
        String requiredGroupCd = BusinessValidator.requireNonBlank(groupCd, "groupCd");
        BusinessValidator.requireFound(
                commonCodeRepository.findCommonCodeGroupDetail(requiredGroupCd),
                "commonCodeGroup"
        );

        CommonCodeReplaceReqDto data = BusinessValidator.requireData(paramDto);
        // 평탄화된 그룹 정보와 전체 코드 목록을 DELETE 전에 모두 검증한다.
        data.setGroupNm(BusinessValidator.requireNonBlank(data.getGroupNm(), "groupNm"));
        List<CommonCodeReqDto> codes = validateCommonCodes(data.getCodes());

        // 검증 완료 후 그룹을 수정하고 기존 코드 목록을 새 항목으로 원자적으로 교체한다.
        BusinessValidator.requireAffectedRows(
                commonCodeRepository.updateCommonCodeGroup(requiredGroupCd, paramDto, changedBy),
                1,
                "updateCommonCodeGroup"
        );
        commonCodeRepository.deleteCommonCodesByGroupCd(requiredGroupCd);
        if (!codes.isEmpty()) {
            BusinessValidator.requireAffectedRows(
                    commonCodeRepository.insertCommonCodes(requiredGroupCd, codes, changedBy),
                    codes.size(),
                    "insertCommonCodes"
            );
        }
        referenceDataVersionService.versionChange(
                REF_TYPE_COMMON_CODE,
                "REPLACE",
                "common_code_groups",
                requiredGroupCd,
                "공통코드 그룹 및 코드 일괄 교체",
                changedBy
        );
        return ApiResponse.success(null);
    }

    /**
     * 외래키 순서에 맞춰 하위 상세코드를 먼저 삭제하고 공통코드 그룹을 삭제한다.
     *
     * <p>상세코드와 그룹 삭제 및 기준정보 버전 갱신을 동일 트랜잭션으로 처리한다.</p>
     *
     * @param groupCd 삭제할 공통코드 그룹 코드
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 삭제 성공 응답
     * @throws BwgBusinessException 필수값 누락, 대상 그룹 미존재 또는 DB 반영 건수 불일치 시
     */
    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public ApiResponse<Void> deleteCommonCodes(String groupCd, String userId) {
        // 그룹과 상세코드 삭제 및 변경 이력에는 검증된 사용자만 사용한다.
        String changedBy = BusinessValidator.requireNonBlank(userId, "userId");
        String requiredGroupCd = BusinessValidator.requireNonBlank(groupCd, "groupCd");
        BusinessValidator.requireFound(
                commonCodeRepository.findCommonCodeGroupDetail(requiredGroupCd),
                "commonCodeGroup"
        );

        // 외래키 관계를 고려해 상세코드를 먼저 삭제한 뒤 그룹과 버전을 변경한다.
        commonCodeRepository.deleteCommonCodesByGroupCd(requiredGroupCd);
        BusinessValidator.requireAffectedRows(
                commonCodeRepository.deleteCommonCodeGroup(requiredGroupCd),
                1,
                "deleteCommonCodeGroup"
        );
        referenceDataVersionService.versionChange(
                REF_TYPE_COMMON_CODE,
                "DELETE",
                "common_code_groups",
                requiredGroupCd,
                "공통코드 그룹 및 코드 통합 삭제",
                changedBy
        );
        return ApiResponse.success(null);
    }

    /**
     * 한 요청 안에서 동일한 상세코드가 중복된 경우 반환할 업무 예외를 생성한다.
     *
     * @param code 중복된 상세코드 값
     * @return 중복 필드와 값을 상세정보로 포함한 업무 예외
     */
    private BwgBusinessException duplicateCode(String code) {
        // 동일 요청에 중복 코드가 있으면 DB 제약조건에 의존하지 않고 업무 오류로 반환한다.
        return new BwgBusinessException.Builder()
                .code(BusinessErrorCode.BUSINESS_RULE_VIOLATION)
                .message(BusinessErrorCode.BUSINESS_RULE_VIOLATION.getMsg())
                .details(Map.of("field", "codes.code", "value", code))
                .build();
    }

    /**
     * 상세코드 목록과 각 항목의 필수값을 검증하고 코드값을 정규화한다.
     *
     * <p>목록 자체는 필수지만 빈 배열은 허용한다. 각 항목의 코드와 코드명은 공백을 제거한
     * 값으로 치환하며, 정규화된 코드 기준으로 요청 내부 중복 여부를 확인한다.</p>
     *
     * @param requestedCodes 검증할 상세코드 목록
     * @return 필수값 검증과 정규화가 완료된 상세코드 목록
     * @throws BwgBusinessException 목록 또는 필수값이 없거나 중복 코드가 존재하는 경우
     */
    private List<CommonCodeReqDto> validateCommonCodes(List<CommonCodeReqDto> requestedCodes) {
        // 빈 배열은 허용하되 codes 필드 자체와 각 상세코드의 필수값은 검증한다.
        List<CommonCodeReqDto> codes = BusinessValidator.requireNonNull(requestedCodes, "codes");
        Set<String> uniqueCodes = new HashSet<>();
        for (CommonCodeReqDto code : codes) {
            CommonCodeReqDto requiredCode = BusinessValidator.requireNonNull(code, "codes.item");
            requiredCode.setCode(BusinessValidator.requireNonBlank(requiredCode.getCode(), "code"));
            requiredCode.setCodeNm(BusinessValidator.requireNonBlank(requiredCode.getCodeNm(), "codeNm"));
            if (!uniqueCodes.add(requiredCode.getCode())) {
                throw duplicateCode(requiredCode.getCode());
            }
        }
        return codes;
    }

}
