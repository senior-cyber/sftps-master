package com.senior.cyber.sftps.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "master.aead", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class MasterAeadConfig {

    private String address;

    private String uri;

    private String clientSecret;

    private String clientId;

}
