package com.senior.cyber.sftps.api.ftp;

import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;

import java.io.File;

public class SftpSNativeFtpFile extends NativeFtpFile {

    public SftpSNativeFtpFile(String fileName, File file, org.apache.ftpserver.ftplet.User user) {
        super(fileName, file, user);
    }

}
