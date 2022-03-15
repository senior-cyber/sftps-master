package com.senior.cyber.sftps.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
public class ApplicationConfiguration {

    private String clientAuth;
    private File trustStore;
    private String trustStoreType;
    private String trustStorePassword;

    private File keyStore;
    private String keyStoreType;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;

    private int ftpPort;
    private String ftpDataPort;

    private int ftpsPort;
    private String ftpsDataPort;

    private int sftpPort;

    private File workspace;

    public File getWorkspace() {
        return workspace;
    }

    public void setWorkspace(File workspace) {
        this.workspace = workspace;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public int getFtpsPort() {
        return ftpsPort;
    }

    public void setFtpsPort(int ftpsPort) {
        this.ftpsPort = ftpsPort;
    }

    public int getSftpPort() {
        return sftpPort;
    }

    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }

    public String getFtpDataPort() {
        return ftpDataPort;
    }

    public void setFtpDataPort(String ftpDataPort) {
        this.ftpDataPort = ftpDataPort;
    }

    public String getFtpsDataPort() {
        return ftpsDataPort;
    }

    public void setFtpsDataPort(String ftpsDataPort) {
        this.ftpsDataPort = ftpsDataPort;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(String clientAuth) {
        this.clientAuth = clientAuth;
    }

    public File getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(File trustStore) {
        this.trustStore = trustStore;
    }

    public File getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

}
