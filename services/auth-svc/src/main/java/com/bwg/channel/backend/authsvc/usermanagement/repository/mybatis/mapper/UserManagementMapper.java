package com.bwg.channel.backend.authsvc.usermanagement.repository.mybatis.mapper;

import com.bwg.channel.backend.authsvc.usermanagement.dto.UserCreateReqDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserDetailResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserListResDto;
import com.bwg.channel.backend.authsvc.usermanagement.dto.UserUpdateReqDto;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 사용자 관리 SQL에 대응하는 MyBatis Mapper. */
@Mapper
public interface UserManagementMapper {

    List<UserListResDto> findUsers();

    UserDetailResDto findUser(@Param("userId") String userId);

    boolean existsUser(@Param("userId") String userId);

    List<Long> findRoleIdsByName(@Param("roleName") String roleName);

    int insertUser(
            @Param("request") ApiRequest<UserCreateReqDto> request,
            @Param("createdBy") String createdBy
    );

    int insertUserRole(
            @Param("userId") String userId,
            @Param("roleId") Long roleId,
            @Param("createdBy") String createdBy
    );

    int updateUser(
            @Param("userId") String userId,
            @Param("request") ApiRequest<UserUpdateReqDto> request,
            @Param("updatedBy") String updatedBy
    );

    int deleteUserRoles(@Param("userId") String userId);

    int deleteUser(@Param("userId") String userId);
}
