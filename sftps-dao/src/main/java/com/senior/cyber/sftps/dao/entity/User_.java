package com.senior.cyber.sftps.dao.entity;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.Date;

@StaticMetamodel(User.class)
public abstract class User_ {

    public static volatile SingularAttribute<User, Long> id;
    public static volatile SingularAttribute<User, String> displayName;
    public static volatile SingularAttribute<User, Boolean> enabled;
    public static volatile SingularAttribute<User, Boolean> encryptAtRest;
    public static volatile SingularAttribute<User, String> login;
    public static volatile SingularAttribute<User, String> password;
    public static volatile SingularAttribute<User, String> emailAddress;
    public static volatile SingularAttribute<User, String> homeDirectory;
    public static volatile SingularAttribute<User, String> dek;
    public static volatile SingularAttribute<User, String> secret;
    public static volatile SingularAttribute<User, Boolean> admin;
    public static volatile SingularAttribute<User, Boolean> webhookEnabled;
    public static volatile SingularAttribute<User, String> webhookUrl;
    public static volatile SingularAttribute<User, String> webhookSecret;
    public static volatile SingularAttribute<User, Date> lastSeen;
    public static volatile MapAttribute<User, String, Group> groups;
    public static volatile MapAttribute<User, String, Role> roles;
    public static volatile MapAttribute<User, String, Role> denyRoles;

    public User_() {
    }

}