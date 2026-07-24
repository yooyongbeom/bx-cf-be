package com.bwg.channel.backend.authsvc.authentication.repository.mybatis;

import com.bwg.channel.backend.authsvc.authentication.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.authentication.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.authentication.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.authentication.repository.LoginRepository;
import com.bwg.channel.backend.authsvc.authentication.repository.mybatis.mapper.UserMapper;
import com.bwg.channel.backend.common.constants.error.CommonErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.securitycommon.exception.BwgAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("mybatisLogin")
@RequiredArgsConstructor
public class MybatisLoginRepositoryAdapter implements LoginRepository {
    /** MyBatis XML/Mapper SQL로 사용자와 refresh token을 조회/갱신하는 매퍼. */
    private final UserMapper userMapper;

    @Override
    public LoginResDto findByUsrIdAndUsrPwd(ApiRequest<LoginReqDto> paramDto) {
        // 로그인 요청의 사용자 ID/PW 기준으로 사용자 정보 조회
        LoginReqDto data = paramDto.getData();
        return userMapper.findByUsrIdAndUsrPwd(data.getUsrId(), data.getUsrPwd())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(CommonErrorCode.DB_NO_DATA_ERROR)
                                .message(CommonErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .build());
    }

    @Override
    public LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        // 검증된 refresh token 값으로 DB에 저장된 사용자 정보 조회
        return userMapper.findByRefreshToken(refreshTknReqDto.getRefreshToken())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(CommonErrorCode.DB_NO_DATA_ERROR)
                                .message(CommonErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .build());
    }

    @Override
    @Transactional(transactionManager = "mybatisMainTransactionManager")
    public int updateRefreshToken(LoginResDto loginResDto) {
        // 인증 서비스의 MyBatis 주 트랜잭션 경계 안에서 refresh token과 만료 일시를 갱신한다.
        int affectedRows = userMapper.updateRefreshToken(loginResDto);
        if (affectedRows == 0) {
            throw new BwgAuthException.Builder()
                    .code(CommonErrorCode.DB_SAVE_DATA_ERROR)
                    .message("Refresh token update failed for user: " + loginResDto.getUsrId())
                    .build();
        }
        return affectedRows;
    }
}
