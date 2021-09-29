package com.senior.cyber.sftps.web.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
public class ApplicationConfiguration {

    private File homeDirectory;

    public File getHomeDirectory() {
        return homeDirectory;
    }

    public void setHomeDirectory(File homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

}
