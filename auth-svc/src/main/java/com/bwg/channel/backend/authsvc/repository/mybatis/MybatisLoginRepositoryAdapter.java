package com.bwg.channel.backend.authsvc.repository.mybatis;

import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.domain.entity.User;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.mybatis.mapper.UserMapper;
import com.bwg.channel.backend.common.constants.enums.AuthErrorCode;
import com.bwg.channel.backend.common.exception.BwgAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("mybatisLogin")
@RequiredArgsConstructor
public class MybatisLoginRepositoryAdapter implements LoginRepository {
    private final UserMapper userMapper;

    @Override
    public LoginDto findByUsrIdAndUsrPwd(LoginDto paramDto) {
        return userMapper.findByUsrIdAndUsrPwd(paramDto.getUsrId(), paramDto.getUsrPwd())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .build());
    }

    @Override
    public LoginDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        return userMapper.findByRefreshToken(refreshTknReqDto.getRefreshToken())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .build());
    }

    @Override
    @Transactional
    public int updateRefreshToken(LoginDto loginDto) {
        int affectedRows = userMapper.updateRefreshToken(loginDto);
        if (affectedRows == 0) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.DB_SAVE_DATA_ERROR)
                    .message("Refresh token update failed for user: " + loginDto.getUsrId())
                    .build();
        }
        return affectedRows;
    }
}