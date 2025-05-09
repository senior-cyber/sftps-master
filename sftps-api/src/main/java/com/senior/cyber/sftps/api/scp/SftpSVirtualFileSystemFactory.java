package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.configuration.AppConfig;
import com.senior.cyber.sftps.dao.entity.rbac.User;
import com.senior.cyber.sftps.dao.repository.rbac.UserRepository;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class SftpSVirtualFileSystemFactory extends org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory {

    private final UserRepository userRepository;

    private final AppConfig configuration;

    public SftpSVirtualFileSystemFactory(AppConfig configuration, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.configuration = configuration;
    }

    @Override
    public Path getUserHomeDir(String userName) {
        User user = this.userRepository.findByLogin(userName);
        if (user == null) {
            throw new IllegalArgumentException(userName);
        }
        File homeDirectory = new File(this.configuration.getWorkspace(), user.getHomeDirectory());
        homeDirectory.mkdirs();
        return FileSystems.getDefault().getPath(homeDirectory.getAbsolutePath());
    }

}
