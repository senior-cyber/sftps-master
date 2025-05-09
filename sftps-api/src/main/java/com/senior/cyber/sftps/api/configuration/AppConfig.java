package com.senior.cyber.sftps.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class AppConfig {

    private String clientAuth;
    private File trustStore;
    private String trustStoreType;
    private String trustStorePassword;

    private File keyStore;
    private String keyStoreType;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;

    private String passiveAddress;
    private String passiveExternalAddress;

    private int ftpPort;
    private String ftpDataPort;

    private int ftpsPort;
    private String ftpsDataPort;

    private int sftpPort;

    private File workspace;

}
