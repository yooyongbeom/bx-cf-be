package com.bwg.channel.backend.authsvc.usermanagement.repository.mybatis;

import com.bwg.channel.backend.authsvc.usermanagement.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserUpdateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.repository.UserManagementRepository;
import com.bwg.channel.backend.authsvc.usermanagement.repository.mybatis.mapper.UserManagementMapper;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 사용자 관리 저장소 계약을 MyBatis Mapper 호출로 연결하는 어댑터. */
@Repository
@RequiredArgsConstructor
public class MybatisUserManagementRepositoryAdapter implements UserManagementRepository {

    private final UserManagementMapper userManagementMapper;

    @Override
    public List<UserListResDto> findUsers() {
        // 응답에 자격 증명을 포함하지 않는 사용자 목록 조회를 Mapper에 위임한다.
        return userManagementMapper.findUsers();
    }

    @Override
    public UserDetailResDto findUser(String userId) {
        // 사용자 ID 기준 단건 조회를 Mapper에 위임한다.
        return userManagementMapper.findUser(userId);
    }

    @Override
    public boolean existsUser(String userId) {
        // 등록 중복과 수정·삭제 대상 검증에 사용할 존재 여부를 조회한다.
        return userManagementMapper.existsUser(userId);
    }

    @Override
    public List<Long> findRoleIdsByName(String roleName) {
        // ROLE_USER 같은 역할 이름을 역할 ID로 변환해 기본 역할을 DB에서 결정한다.
        return userManagementMapper.findRoleIdsByName(roleName);
    }

    @Override
    public int insertUser(ApiRequest<UserCreateReqDto> request, String createdBy) {
        // Gateway가 검증해 전달한 생성자 ID로 사용자 본문 등록을 위임한다.
        return userManagementMapper.insertUser(request, createdBy);
    }

    @Override
    public int insertUserRole(String userId, Long roleId, String createdBy) {
        // 사용자 등록 후 기본 역할 연결을 별도 SQL로 등록한다.
        return userManagementMapper.insertUserRole(userId, roleId, createdBy);
    }

    @Override
    public int updateUser(String userId, ApiRequest<UserUpdateReqDto> request, String updatedBy) {
        // 자격 증명과 역할을 변경하지 않는 기본 정보 수정만 Mapper에 위임한다.
        return userManagementMapper.updateUser(userId, request, updatedBy);
    }

    @Override
    public int deleteUserRoles(String userId) {
        // 트랜잭션에서 USERS보다 먼저 USER_ROLES를 삭제해 FK 참조를 해소한다.
        return userManagementMapper.deleteUserRoles(userId);
    }

    @Override
    public int deleteUser(String userId) {
        // USER_ROLES 삭제가 완료된 뒤 사용자 본문을 물리 삭제한다.
        return userManagementMapper.deleteUser(userId);
    }
}
