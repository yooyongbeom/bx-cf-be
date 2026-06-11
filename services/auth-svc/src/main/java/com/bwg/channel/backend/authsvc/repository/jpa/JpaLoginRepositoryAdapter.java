package com.bwg.channel.backend.authsvc.repository.jpa;

import com.bwg.channel.backend.authsvc.domain.dto.LoginDto;
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
    public LoginDto findByUsrIdAndUsrPwd(LoginDto paramDto) {
        User user = jpaLoginRepository.findByUsrIdAndUsrPwd(paramDto.getUsrId(), paramDto.getUsrPwd())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .details(null)
                                .build());

        return makeLoginDto(user);
    }

    @Override
    public LoginDto findByRefreshToken(RefreshTknReqDto refreshTknReqDto) {
        User user = jpaLoginRepository.findByRefreshToken(refreshTknReqDto.getRefreshToken())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_NO_DATA_ERROR)
                                .message(AuthErrorCode.DB_NO_DATA_ERROR.getMsg())
                                .details(null)
                                .build());

        return makeLoginDto(user);
    }

    private LoginDto makeLoginDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleNm)
                .collect(Collectors.toList());

        LoginDto loginDto = new LoginDto();
        loginDto.setUsrId(user.getUsrId());
        loginDto.setUsrNm(user.getUsrNm());
        loginDto.setPositDivName(user.getPositDivName());
        loginDto.setDeptName(user.getDeptName());
        loginDto.setRoles(roles);

        return loginDto;
    }

    @Override
    @Transactional
    public int updateRefreshToken(LoginDto loginDto) {
        User user = jpaLoginRepository.findByUsrId(loginDto.getUsrId())
                .orElseThrow(() ->
                        new BwgAuthException.Builder()
                                .code(AuthErrorCode.DB_SAVE_DATA_ERROR)
                                .message("User not found for refresh token update")
                                .details(null)
                                .build());

        user.setRefreshToken(loginDto.getRefreshToken());
        user.setRefreshTokenExpiresAt(loginDto.getRefreshTokenExpiresAt());
        jpaLoginRepository.save(user);
        return 1; // Assuming success
    }
}