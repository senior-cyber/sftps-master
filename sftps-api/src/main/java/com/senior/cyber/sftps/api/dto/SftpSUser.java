package com.senior.cyber.sftps.api.dto;

import com.senior.cyber.sftps.api.SecretUtils;
import org.apache.ftpserver.usermanager.impl.BaseUser;

public class SftpSUser extends BaseUser {

    public static final String USER_SESSION = "USER_SESSION";

    protected final String userDisplayName;

    protected final String userId;

    protected final String keyId;

    protected final String keyName;

    protected final boolean encryptAtRest;

    protected final byte[] originDictionary;

    protected final byte[] fakeDictionary;

    public SftpSUser(String userId, String keyId, String keyName, String userDisplayName, String secret, boolean encryptAtRest) {
        this.encryptAtRest = encryptAtRest;
        this.userDisplayName = userDisplayName;
        this.userId = userId;
        this.keyId = keyId;
        this.keyName = keyName;
        if (secret != null && !secret.isEmpty()) {
            this.originDictionary = SecretUtils.buildOriginToFake(secret);
            this.fakeDictionary = SecretUtils.buildFakeToOrigin(secret);
        } else {
            this.originDictionary = null;
            this.fakeDictionary = null;
        }
    }

    public String getKeyName() {
        return keyName;
    }

    public String getUserId() {
        return userId;
    }

    public String getKeyId() {
        return keyId;
    }

    public byte[] getOriginDictionary() {
        return originDictionary;
    }

    public byte[] getFakeDictionary() {
        return fakeDictionary;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public boolean isEncryptAtRest() {
        return encryptAtRest;
    }

}
