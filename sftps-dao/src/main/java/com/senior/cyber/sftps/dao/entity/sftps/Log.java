package com.senior.cyber.sftps.dao.entity.sftps;

import com.senior.cyber.sftps.dao.enums.EventTypeEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "tbl_log")
@Getter
@Setter
public class Log implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "log_id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventTypeEnum eventType;

    @Column(name = "user_display_name")
    private String userDisplayName;

    @Column(name = "key_name")
    private String keyName;

    @Column(name = "size")
    private Long size;

    @Column(name = "src_path")
    private String srcPath;

    @Column(name = "dst_path")
    private String dstPath;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

}
