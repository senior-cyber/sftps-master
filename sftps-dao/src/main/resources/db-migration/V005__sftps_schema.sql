CREATE TABLE tbl_key
(
    key_id      VARCHAR(36)  NOT NULL,
    user_id     VARCHAR(36)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    certificate TEXT         NOT NULL,
    private_key TEXT         NOT NULL,
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    last_seen   DATETIME NULL,
    INDEX(last_seen),
    INDEX(enabled),
    UNIQUE KEY (user_id, name),
    PRIMARY KEY (key_id)
);

CREATE TABLE tbl_log
(
    log_id            VARCHAR(36)  NOT NULL,
    user_display_name VARCHAR(200) NOT NULL,
    key_name          VARCHAR(200) NOT NULL,
    size              BIGINT NULL,
    src_path          VARCHAR(255) NULL,
    dst_path          VARCHAR(255) NULL,
    created_at        DATETIME NULL,
    INDEX(created_at),
    PRIMARY KEY (log_id)
);