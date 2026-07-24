package com.bwg.channel.backend.authsvc.authentication.repository.jpa;

import com.bwg.channel.backend.authsvc.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaLoginRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsrId(String usrId);
    Optional<User> findByUsrIdAndUsrPwd(String usrId, String usrPwd);
    Optional<User> findByRefreshToken(String refreshToken);
}

