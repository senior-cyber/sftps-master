package com.senior.cyber.sftps.api.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SftpSNativeFileSystemFactory extends org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpSNativeFileSystemFactory.class);

    public SftpSNativeFileSystemFactory() {
    }

    @Override
    public FileSystemView createFileSystemView(User user) throws FtpException {
        synchronized (user) {
            // create home if does not exist
            if (isCreateHome()) {
                String homeDirStr = user.getHomeDirectory();
                File homeDir = new File(homeDirStr);
                if (homeDir.isFile()) {
                    throw new FtpException("Not a directory :: " + homeDirStr);
                }
                if ((!homeDir.exists()) && (!homeDir.mkdirs())) {
                    throw new FtpException("Cannot create user home :: "
                            + homeDirStr);
                }
            }

            return new SftpSNativeFileSystemView(user, isCaseInsensitive());
        }
    }
}
