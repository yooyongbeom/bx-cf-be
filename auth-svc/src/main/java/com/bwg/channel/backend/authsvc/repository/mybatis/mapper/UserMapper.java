package com.bwg.channel.backend.authsvc.repository.mybatis.mapper;

import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<LoginDto> findByUsrIdAndUsrPwd(@Param("usrId") String usrId, @Param("usrPwd") String usrPwd);

    Optional<LoginDto> findByRefreshToken(@Param("refreshToken") String refreshToken);
    int updateRefreshToken(LoginDto loginDto);
}
