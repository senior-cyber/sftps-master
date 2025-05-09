package com.senior.cyber.sftps.dao.entity.rbac;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;

@Entity
@Table(name = "tbl_rbac_deny_role")
@Getter
@Setter
public class DenyRole implements Serializable {

    @Id
    @Column(name = "deny_role_id")
    @UuidGenerator
    @Setter(AccessLevel.NONE)
    private String id;

    @Column(name = "role_id")
    private String roleId;

    @Column(name = "user_id")
    private String userId;

}