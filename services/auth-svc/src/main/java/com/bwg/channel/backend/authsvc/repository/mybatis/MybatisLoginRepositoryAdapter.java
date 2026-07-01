package com.bwg.channel.backend.authsvc.repository.mybatis;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.mybatis.mapper.UserMapper;
import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.authcore.exception.BwgAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("mybatisLogin")
@RequiredArgsConstructor
public class MybatisLoginRepositoryAdapter implements LoginRepository {
    private final UserMapper userMapper;

    @Override
    public LoginResDto findByUsrIdAndUsrPwd(LoginReqDto paramDto) {
        return userMapper.findByUsrIdAndUsrPwd(paramDto.getUsrId(), paramDto.getUsrPwd())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .build());
    }

    @Override
    public LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        return userMapper.findByRefreshToken(refreshTknReqDto.getRefreshToken())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .build());
    }

    @Override
    @Transactional
    public int updateRefreshToken(LoginResDto loginResDto) {
        int affectedRows = userMapper.updateRefreshToken(loginResDto);
        if (affectedRows == 0) {
            throw new BwgAuthException.Builder()
                    .code(AuthErrorCode.DB_SAVE_DATA_ERROR)
                    .message("Refresh token update failed for user: " + loginResDto.getUsrId())
                    .build();
        }
        return affectedRows;
    }
}
