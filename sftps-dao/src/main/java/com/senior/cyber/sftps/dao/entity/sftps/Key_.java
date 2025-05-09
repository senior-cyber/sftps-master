package com.senior.cyber.sftps.dao.entity.sftps;

import com.senior.cyber.sftps.dao.entity.rbac.User;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.util.Date;

@StaticMetamodel(Key.class)
public abstract class Key_ {

    public static volatile SingularAttribute<Key, String> id;
    public static volatile SingularAttribute<Key, String> name;
    public static volatile SingularAttribute<Key, Boolean> enabled;
    public static volatile SingularAttribute<Key, Date> lastSeen;
    public static volatile SingularAttribute<Key, String> certificate;
    public static volatile SingularAttribute<Key, String> privateKey;
    public static volatile SingularAttribute<Key, User> user;

    public Key_() {
    }

}