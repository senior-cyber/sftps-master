package com.senior.cyber.sftps.dao.entity.rbac;

import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Role.class)
public abstract class Role_ {

    public static volatile SingularAttribute<Role, String> id;
    public static volatile SingularAttribute<Role, String> name;
    public static volatile SingularAttribute<Role, String> description;
    public static volatile SingularAttribute<Role, Boolean> enabled;
    public static volatile MapAttribute<Role, String, Group> groups;
    public static volatile MapAttribute<Role, String, User> users;
    public static volatile MapAttribute<Role, String, User> denyUsers;

}