package com.senior.cyber.sftps.dao.entity.rbac;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(UserGroup.class)
public abstract class UserGroup_ {

    public static volatile SingularAttribute<UserGroup, String> id;
    public static volatile SingularAttribute<UserGroup, String> groupId;
    public static volatile SingularAttribute<UserGroup, String> userId;

}