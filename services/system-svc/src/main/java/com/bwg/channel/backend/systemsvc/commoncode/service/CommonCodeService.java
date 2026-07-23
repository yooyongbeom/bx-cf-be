package com.bwg.channel.backend.systemsvc.commoncode.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeCreateReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;

import java.util.List;

/**
 * 공통코드 그룹과 공통코드 관리 기능의 서비스 계약.
 */
public interface CommonCodeService {

    /**
     * 공통코드 그룹 목록을 조회한다.
     *
     * @return 공통코드 그룹 목록과 페이지 정보가 포함된 응답
     */
    ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups();

    /**
     * 공통코드 그룹과 하위 상세코드를 하나의 트랜잭션으로 등록한다.
     *
     * <p>{@code codes}가 빈 배열이면 그룹만 등록한다.</p>
     *
     * @param paramDto 등록할 그룹 정보와 상세코드 목록
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 등록 성공 응답
     */
    ApiResponse<Void> createCommonCodes(ApiRequest<CommonCodeCreateReqDto> paramDto, String userId);

    /**
     * 전체 공통코드 그룹과 각 그룹의 하위 상세코드 목록을 조회한다.
     *
     * @return 전체 그룹 및 상세코드 목록과 페이지 정보가 포함된 응답
     */
    ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetails();

    /**
     * 그룹 코드에 해당하는 그룹 정보와 하위 상세코드를 조회한다.
     *
     * @param groupCd 조회할 공통코드 그룹 코드
     * @return 요청 그룹 한 건을 배열 형태로 포함한 응답
     */
    ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetail(String groupCd);

    /**
     * 공통코드 그룹 정보를 수정하고 기존 상세코드 전체를 요청 목록으로 교체한다.
     *
     * <p>{@code codes}가 빈 배열이면 기존 상세코드를 모두 삭제하고 그룹 정보만 수정한다.</p>
     *
     * @param groupCd 교체할 공통코드 그룹 코드
     * @param paramDto 수정할 그룹 정보와 새 상세코드 목록
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 교체 성공 응답
     */
    ApiResponse<Void> replaceCommonCodes(
            String groupCd,
            ApiRequest<CommonCodeReplaceReqDto> paramDto,
            String userId
    );

    /**
     * 공통코드 그룹과 해당 그룹의 하위 상세코드를 하나의 트랜잭션으로 삭제한다.
     *
     * @param groupCd 삭제할 공통코드 그룹 코드
     * @param userId Gateway가 전달한 인증 사용자 ID
     * @return 삭제 성공 응답
     */
    ApiResponse<Void> deleteCommonCodes(String groupCd, String userId);
}
