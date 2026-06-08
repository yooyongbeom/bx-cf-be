package com.bwg.channel.backend.authsvc.domain.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;

import java.io.Serializable;

@Data
@Entity
@Table(name = "USER_ROLES")
public class UserRole implements Serializable {
    @Id
    @Column(nullable = false, name = "USER_ID")
    private String usrId;
    @Id
    @Column(nullable = false, name = "ROLE_ID")
    private Long roleId;
}