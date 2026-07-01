package com.bwg.channel.backend.authsvc.repository.mybatis.mapper;

import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<LoginResDto> findByUsrIdAndUsrPwd(@Param("usrId") String usrId, @Param("usrPwd") String usrPwd);

    Optional<LoginResDto> findByRefreshToken(@Param("refreshToken") String refreshToken);

    int updateRefreshToken(LoginResDto loginResDto);
}
