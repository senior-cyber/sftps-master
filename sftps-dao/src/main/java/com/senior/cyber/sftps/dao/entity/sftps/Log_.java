package com.senior.cyber.sftps.dao.entity.sftps;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.util.Date;

@StaticMetamodel(Log.class)
public abstract class Log_ {

    public static volatile SingularAttribute<Log, String> id;
    public static volatile SingularAttribute<Log, String> eventType;
    public static volatile SingularAttribute<Log, String> userDisplayName;
    public static volatile SingularAttribute<Log, String> keyName;
    public static volatile SingularAttribute<Log, Long> size;
    public static volatile SingularAttribute<Log, String> srcPath;
    public static volatile SingularAttribute<Log, String> dstPath;
    public static volatile SingularAttribute<Log, Date> createdAt;

    public Log_() {
    }

}