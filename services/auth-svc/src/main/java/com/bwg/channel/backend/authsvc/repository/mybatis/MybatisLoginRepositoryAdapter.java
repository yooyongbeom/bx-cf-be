package com.bwg.channel.backend.authsvc.repository.mybatis;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.repository.mybatis.mapper.UserMapper;
import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("mybatisLogin")
@RequiredArgsConstructor
public class MybatisLoginRepositoryAdapter implements LoginRepository {
    /** MyBatis XML/Mapper SQL로 사용자와 refresh token을 조회/갱신하는 매퍼. */
    private final UserMapper userMapper;

    @Override
    public LoginResDto findByUsrIdAndUsrPwd(LoginReqDto paramDto) {
        // 로그인 요청의 사용자 ID/PW 기준으로 사용자 정보 조회
        return userMapper.findByUsrIdAndUsrPwd(paramDto.getUsrId(), paramDto.getUsrPwd())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .build());
    }

    @Override
    public LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        // 검증된 refresh token 값으로 DB에 저장된 사용자 정보 조회
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
        // 사용자 ID 기준 refresh token과 만료 일시 갱신
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
