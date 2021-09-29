package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.repository.UserRepository;
import com.senior.cyber.sftps.dao.entity.User;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

public class SftpSVirtualFileSystemFactory extends org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory {

    private final UserRepository userRepository;

    public SftpSVirtualFileSystemFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Path getUserHomeDir(String userName) {
        Optional<User> optionalUser = this.userRepository.findByLogin(userName);
        User user = optionalUser.orElseThrow(() -> new IllegalArgumentException(""));
        File homeDirectory = new File(user.getHomeDirectory());
        homeDirectory.mkdirs();
        return FileSystems.getDefault().getPath(homeDirectory.getAbsolutePath());
    }

}
