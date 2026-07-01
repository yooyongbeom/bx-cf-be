package com.bwg.channel.backend.authsvc.repository.jpa;

import com.bwg.channel.backend.authsvc.domain.dto.LoginReqDto;
import com.bwg.channel.backend.authsvc.domain.dto.LoginResDto;
import com.bwg.channel.backend.authsvc.domain.dto.RefreshTknReqDto;
import com.bwg.channel.backend.authsvc.domain.entity.Role;
import com.bwg.channel.backend.authsvc.domain.entity.User;
import com.bwg.channel.backend.authsvc.repository.LoginRepository;
import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.authcore.exception.BwgAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component("jpaLogin")
@RequiredArgsConstructor
public class JpaLoginRepositoryAdapter implements LoginRepository {
    private final JpaLoginRepository jpaLoginRepository;

    @Override
    public LoginResDto findByUsrIdAndUsrPwd(LoginReqDto paramDto) {
        User user = jpaLoginRepository.findByUsrIdAndUsrPwd(paramDto.getUsrId(), paramDto.getUsrPwd())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .details(null)
                                .build());

        return makeLoginResDto(user);
    }

    @Override
    public LoginResDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        User user = jpaLoginRepository.findByRefreshToken(refreshTknReqDto.getRefreshToken())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .details(null)
                                .build());

        return makeLoginResDto(user);
    }

    private LoginResDto makeLoginResDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleNm)
                .collect(Collectors.toList());

        LoginResDto loginResDto = new LoginResDto();
        loginResDto.setUsrId(user.getUsrId());
        loginResDto.setUsrNm(user.getUsrNm());
        loginResDto.setPositDivName(user.getPositDivName());
        loginResDto.setDeptName(user.getDeptName());
        loginResDto.setRoles(roles);

        return loginResDto;
    }

    @Override
    @Transactional
    public int updateRefreshToken(LoginResDto loginResDto) {
        User user = jpaLoginRepository.findByUsrId(loginResDto.getUsrId())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_SAVE_DATA_ERROR)
                                .message("User not found for refresh token update")
                                .details(null)
                                .build());

        user.setRefreshToken(loginResDto.getRefreshToken());
        user.setRefreshTokenExpiresAt(loginResDto.getRefreshTokenExpiresAt());
        jpaLoginRepository.save(user);
        return 1; // Assuming success
    }
}
