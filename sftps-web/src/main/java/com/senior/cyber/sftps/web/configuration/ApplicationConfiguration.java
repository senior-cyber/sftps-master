package com.senior.cyber.sftps.web.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
public class ApplicationConfiguration {

    private boolean dataEncryption;

    private boolean secretEncryption;

    public boolean isDataEncryption() {
        return dataEncryption;
    }

    public void setDataEncryption(boolean dataEncryption) {
        this.dataEncryption = dataEncryption;
    }

    public boolean isSecretEncryption() {
        return secretEncryption;
    }

    public void setSecretEncryption(boolean secretEncryption) {
        this.secretEncryption = secretEncryption;
    }

}
