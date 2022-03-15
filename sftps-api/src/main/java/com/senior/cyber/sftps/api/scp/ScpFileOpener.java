package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.dto.SftpSUser;
import com.senior.cyber.sftps.api.ftp.DrmInputStream;
import com.senior.cyber.sftps.api.ftp.DrmOutputStream;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.common.helpers.DefaultScpFileOpener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class ScpFileOpener extends DefaultScpFileOpener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScpFileOpener.class);

    @Override
    public OutputStream openWrite(Session session, Path file, long size, Set<PosixFilePermission> permissions, OpenOption... options) throws IOException {
        org.apache.sshd.server.session.ServerSession serverSession = (org.apache.sshd.server.session.ServerSession) session;
        SftpSUser user = (SftpSUser) serverSession.getProperties().get(SftpSUser.USER_SESSION);
        OutputStream origin = super.openWrite(session, file, size, permissions, options);
        if (user.getFakeDictionary() != null && user.getOriginDictionary() != null) {
            LOGGER.info("openWrite with DRM");
            return new DrmOutputStream(origin, user.getFakeDictionary());
        } else {
            LOGGER.info("openWrite");
            return origin;
        }
    }

    @Override
    public InputStream openRead(Session session, Path file, long size, Set<PosixFilePermission> permissions, OpenOption... options) throws IOException {
        org.apache.sshd.server.session.ServerSession serverSession = (org.apache.sshd.server.session.ServerSession) session;
        SftpSUser user = (SftpSUser) serverSession.getProperties().get(SftpSUser.USER_SESSION);
        InputStream origin = super.openRead(session, file, size, permissions, options);
        if (user.getFakeDictionary() != null && user.getOriginDictionary() != null) {
            LOGGER.info("openRead with DRM");
            return new DrmInputStream(origin, user.getOriginDictionary());
        } else {
            LOGGER.info("openRead");
            return origin;
        }
    }

}
