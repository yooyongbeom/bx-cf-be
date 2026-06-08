package com.bwg.channel.backend.authsvc.domain.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "USERS") // DB 테이블명을 'USERS'로 지정
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(unique = true, nullable = false, name = "USER_ID")
    private String usrId;
    @Column(nullable = false, name = "USER_NM")
    private String usrNm;
    @Column(name = "POSIT_DIV_NAME")
    private String positDivName;
    @Column(name = "DEPT_NAME")
    private String deptName;
    @Column(nullable = false, name = "USER_PWD")
    private String usrPwd;
    @Column(name = "REFRESH_TOKEN")
    private String refreshToken;
    @Column(name = "REFRESH_TOKEN_EXPIRES_AT", length = 50)
    private String refreshTokenExpiresAt;

    @ManyToMany(fetch = FetchType.EAGER) // EAGER 또는 LAZY 전략 선택
    @JoinTable(
        name = "USER_ROLES", // 조인 테이블 이름
        joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID"), // User 테이블의 조인 컬럼
        inverseJoinColumns = @JoinColumn(name = "ROLE_ID") // Role 테이블의 조인 컬럼
    )
    private Set<Role> roles;
}