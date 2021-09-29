package com.senior.cyber.sftps.dao.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "tbl_role")
public class Role implements Serializable {

    public static final String NAME_ROOT = "root";
    public static final String NAME_Page_MyProfile = "MyProfilePage";
    public static final String NAME_Page_MyKey = "MyKeyPage";
    public static final String NAME_Page_MyClientBrowse = "MyClientBrowsePage";
    public static final String NAME_Page_MyClientCreate = "MyClientCreatePage";
    public static final String NAME_Page_MyClientModify = "MyClientModifyPage";
    public static final String NAME_Page_RoleBrowse = "RoleBrowsePage";
    public static final String NAME_Page_Log = "LogPage";
    public static final String NAME_Page_GroupBrowse = "GroupBrowsePage";
    public static final String NAME_Page_SessionBrowse = "SessionBrowsePage";
    public static final String NAME_Page_GroupModify = "GroupModifyPage";
    public static final String NAME_Page_UserBrowse = "UserBrowsePage";
    public static final String NAME_Page_UserModify = "UserModifyPage";
    public static final String NAME_Page_UserSwitch = "UserSwitchPage";
    public static final String NAME_Page_UserExit = "UserExitPage";

    public static final String DESCRIPTION_ROOT = "could access everything";
    public static final String DESCRIPTION_Page_MyKey = "could access my key browse page";
    public static final String DESCRIPTION_Page_Log = "could access log page";
    public static final String DESCRIPTION_Page_MyProfile = "could access my profile page";
    public static final String DESCRIPTION_Page_MyClientBrowse = "could access my client browse page";
    public static final String DESCRIPTION_Page_MyClientCreate = "could access my client create page";
    public static final String DESCRIPTION_Page_MyClientModify = "could access my client modify page";
    public static final String DESCRIPTION_Page_RoleBrowse = "could access role browse page";
    public static final String DESCRIPTION_Page_GroupBrowse = "could access group browse page";
    public static final String DESCRIPTION_Page_SessionBrowse = "could access session browse page";
    public static final String DESCRIPTION_Page_GroupModify = "could access group update page";
    public static final String DESCRIPTION_Page_UserBrowse = "could access user browse page";
    public static final String DESCRIPTION_Page_UserModify = "could access user update page";
    public static final String DESCRIPTION_Page_UserSwitch = "could access user switch page";
    public static final String DESCRIPTION_Page_UserExit = "could access user exit page";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "enabled")
    private boolean enabled;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_group_role",
            joinColumns = @JoinColumn(name = "r_role_id", referencedColumnName = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "r_group_id", referencedColumnName = "group_id")

    )
    @MapKeyColumn(name = "group_role_id")
    private Map<String, Group> groups;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_user_role",
            joinColumns = @JoinColumn(name = "r_role_id", referencedColumnName = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "r_user_id", referencedColumnName = "user_id")

    )
    @MapKeyColumn(name = "user_role_id")
    private Map<String, User> users;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_deny_role",
            joinColumns = @JoinColumn(name = "r_role_id", referencedColumnName = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "r_user_id", referencedColumnName = "user_id")
    )
    @MapKeyColumn(name = "deny_role_id")
    private Map<String, User> denyUsers;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<String, User> getDenyUsers() {
        return denyUsers;
    }

    public void setDenyUsers(Map<String, User> denyUsers) {
        this.denyUsers = denyUsers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
