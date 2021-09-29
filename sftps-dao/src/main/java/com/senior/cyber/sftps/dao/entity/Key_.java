package com.senior.cyber.sftps.dao.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.Date;

@StaticMetamodel(Key.class)
public abstract class Key_ {

    public static volatile SingularAttribute<Key, Long> id;
    public static volatile SingularAttribute<Key, String> name;
    public static volatile SingularAttribute<Key, Boolean> enabled;
    public static volatile SingularAttribute<Key, Date> lastSeen;
    public static volatile SingularAttribute<Key, String> certificate;
    public static volatile SingularAttribute<Key, String> privateKey;
    public static volatile SingularAttribute<Key, User> user;

    public Key_() {
    }

}