package com.senior.cyber.sftps.dao.entity.sftps;

import com.senior.cyber.sftps.dao.entity.rbac.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "tbl_key")
@Getter
@Setter
public class Key implements Serializable {

    @Id
    @UuidGenerator
    @Setter(AccessLevel.NONE)
    @Column(name = "key_id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "certificate")
    private String certificate;

    @Column(name = "private_key")
    private String privateKey;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_seen")
    private Date lastSeen;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

}
