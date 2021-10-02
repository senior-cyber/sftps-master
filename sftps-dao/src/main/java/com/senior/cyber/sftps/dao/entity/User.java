package com.senior.cyber.sftps.dao.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "tbl_user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "login")
    private String login;

    @Column(name = "pwd")
    private String password;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "home_directory")
    private String homeDirectory;

    @Column(name = "admin")
    private boolean admin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_seen")
    private Date lastSeen;

    @Column(name = "dek")
    private String dek;

    @Column(name = "secret")
    private String secret;

    @Column(name = "webhook_enabled")
    private boolean webhookEnabled;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "encrypt_at_rest")
    private boolean encryptAtRest;

    public boolean isEncryptAtRest() {
        return encryptAtRest;
    }

    public void setEncryptAtRest(boolean encryptAtRest) {
        this.encryptAtRest = encryptAtRest;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_user_group",
            joinColumns = @JoinColumn(name = "r_user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "r_group_id", referencedColumnName = "group_id")

    )
    @MapKeyColumn(name = "user_group_id")
    private Map<String, Group> groups;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_user_role",
            joinColumns = @JoinColumn(name = "r_user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "r_role_id", referencedColumnName = "role_id")

    )
    @MapKeyColumn(name = "user_role_id")
    private Map<String, Role> roles;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tbl_deny_role",
            joinColumns = @JoinColumn(name = "r_user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "r_role_id", referencedColumnName = "role_id")
    )
    @MapKeyColumn(name = "deny_role_id")
    private Map<String, Role> denyRoles;

    public Long getId() {
        return id;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    public Map<String, Role> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Role> roles) {
        this.roles = roles;
    }

    public Map<String, Role> getDenyRoles() {
        return denyRoles;
    }

    public void setDenyRoles(Map<String, Role> denyRoles) {
        this.denyRoles = denyRoles;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getDek() {
        return dek;
    }

    public void setDek(String dek) {
        this.dek = dek;
    }

    public boolean isWebhookEnabled() {
        return webhookEnabled;
    }

    public void setWebhookEnabled(boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
}
