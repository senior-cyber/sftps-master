package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.configuration.AppConfig;
import com.senior.cyber.sftps.dao.entity.rbac.User;
import com.senior.cyber.sftps.dao.repository.rbac.UserRepository;
import org.apache.sshd.common.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class SftpSVirtualFileSystemFactory extends org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpSVirtualFileSystemFactory.class);

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
        boolean made = homeDirectory.mkdirs();
        if (!made) {
            LOGGER.info("Created home directory: " + homeDirectory.exists());
        }
        FileSystem fileSystem = FileSystems.getDefault();
        String absolutePath = homeDirectory.getAbsolutePath();
        Path path = fileSystem.getPath(absolutePath);
        return path;
    }

    @Override
    public FileSystem createFileSystem(SessionContext session) throws IOException {
        FileSystem fileSystem = super.createFileSystem(session);
        return new SftpSFileSystem(fileSystem);
    }

}
