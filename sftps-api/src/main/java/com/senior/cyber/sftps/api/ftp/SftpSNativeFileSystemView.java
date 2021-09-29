package com.senior.cyber.sftps.api.ftp;

import com.senior.cyber.sftps.api.dto.SftpSUser;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SftpSNativeFileSystemView extends org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpSNativeFileSystemView.class);

    public SftpSNativeFileSystemView(User user, boolean caseInsensitive) throws FtpException {
        super(user, caseInsensitive);
    }

    @Override
    public FtpFile getFile(String file) {
        try {
            String rootDir = (String) FieldUtils.readField(this, "rootDir", true);
            String currDir = (String) FieldUtils.readField(this, "currDir", true);
            boolean caseInsensitive = (boolean) FieldUtils.readField(this, "caseInsensitive", true);
            SftpSUser user = (SftpSUser) FieldUtils.readField(this, "user", true);
            String physicalName = getPhysicalName(rootDir, currDir, file, caseInsensitive);
            File fileObj = new File(physicalName);

            // strip the root directory and return
            String userFileName = physicalName.substring(rootDir.length() - 1);
            if (user.getOriginDictionary() == null || user.getFakeDictionary() == null) {
                return new SftpSNativeFtpFile(userFileName, fileObj, user);
            } else {
                return new DrmSftpSNativeFtpFile(userFileName, fileObj, user, user.getOriginDictionary(), user.getFakeDictionary());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
