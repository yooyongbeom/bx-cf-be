package com.bwg.channel.backend.authsvc.user.entity;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

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