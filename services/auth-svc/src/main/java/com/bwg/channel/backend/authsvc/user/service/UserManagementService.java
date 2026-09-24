package com.bwg.channel.backend.authsvc.user.service;

import com.bwg.channel.backend.authsvc.user.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.user.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.user.dto.UserUpdateReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;

import java.util.List;

/** 관리자 권한으로 사용자를 조회하고 변경하는 업무 계약. */
public interface UserManagementService {

    /**
     * 관리자 권한을 검증한 뒤 전체 사용자 목록을 조회한다.
     *
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 전체 사용자 목록과 페이지 정보가 포함된 응답
     * @throws com.bwg.channel.backend.securitycommon.exception.BwgAuthException 관리자 권한이 없는 경우
     */
    ApiResponse<List<UserListResDto>> getUsers(String roles);

    /**
     * 관리자 권한을 검증하고 사용자 ID에 해당하는 상세정보를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 사용자 상세정보가 포함된 응답
     * @throws com.bwg.channel.backend.securitycommon.exception.BwgAuthException 관리자 권한이 없는 경우
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         사용자 ID가 없거나 대상 사용자가 존재하지 않는 경우
     */
    ApiResponse<UserDetailResDto> getUser(String userId, String roles);

    /**
     * 관리자 권한과 Gateway가 검증한 작업자를 확인하고 사용자를 기본 역할과 함께 등록한다.
     *
     * <p>물리 삭제된 사용자 ID는 세션 생성 차단 tombstone이 유지되므로 재사용하지 않는다.</p>
     *
     * @param request 등록할 사용자 정보
     * @param actor Gateway가 검증한 작업자 ID
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 등록 성공 응답
     * @throws com.bwg.channel.backend.securitycommon.exception.BwgAuthException
     *         관리자 권한이 없거나 사용자 또는 세션 저장소 처리에 실패한 경우
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         필수값이 없거나 사용자 ID가 중복되거나 재사용할 수 없는 경우
     */
    ApiResponse<Void> createUser(
            ApiRequest<UserCreateReqDto> request,
                                    String actor,
                                    String roles
    );

    /**
     * 관리자 권한과 Gateway가 검증한 작업자를 확인하고 사용자 기본정보를 수정한다.
     *
     * <p>비밀번호와 사용자 역할은 이 메서드의 수정 대상에 포함하지 않는다.</p>
     *
     * @param userId 수정할 사용자 ID
     * @param request 수정할 사용자 기본정보
     * @param actor Gateway가 검증한 작업자 ID
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 수정 성공 응답
     * @throws com.bwg.channel.backend.securitycommon.exception.BwgAuthException
     *         관리자 권한이 없거나 사용자 저장에 실패한 경우
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         필수값이 없거나 대상 사용자가 존재하지 않는 경우
     */
    ApiResponse<Void> updateUser(
            String userId,
            ApiRequest<UserUpdateReqDto> request,
            String actor,
            String roles
    );

    /**
     * 신규 세션 생성을 차단하고 기존 세션과 역할 연결을 제거한 뒤 사용자를 물리 삭제한다.
     *
     * <p>삭제 성공 후 세션 생성 차단 tombstone을 유지하여 삭제된 사용자 ID의 재사용과
     * 진행 중이던 인증 요청의 세션 생성을 방지한다.</p>
     *
     * @param userId 삭제할 사용자 ID
     * @param actor Gateway가 검증한 작업자 ID
     * @param roles Gateway가 전달한 쉼표 구분 역할 목록
     * @return 삭제 성공 응답
     * @throws com.bwg.channel.backend.securitycommon.exception.BwgAuthException
     *         관리자 권한이 없거나 세션 또는 사용자 저장소 처리에 실패한 경우
     * @throws com.bwg.channel.backend.businesscommon.exception.BwgBusinessException
     *         사용자 ID가 없거나 대상 사용자가 존재하지 않거나 다른 삭제가 진행 중인 경우
     */
    ApiResponse<Void> deleteUser(String userId, String actor, String roles);
}
