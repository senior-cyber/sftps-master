package com.senior.cyber.sftps.web.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "pki", ignoreUnknownFields = true)
public class PkiApiConfiguration {

    private File certificate;

    private File privateKey;

    public File getCertificate() {
        return certificate;
    }

    public void setCertificate(File certificate) {
        this.certificate = certificate;
    }

    public File getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(File privateKey) {
        this.privateKey = privateKey;
    }

}
