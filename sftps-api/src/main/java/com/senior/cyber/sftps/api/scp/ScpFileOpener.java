package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.dto.SftpSUser;
import com.senior.cyber.sftps.api.ftp.DrmInputStream;
import com.senior.cyber.sftps.api.ftp.DrmOutputStream;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.common.helpers.DefaultScpFileOpener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class ScpFileOpener extends DefaultScpFileOpener {

    @Override
    public OutputStream openWrite(Session session, Path file, long size, Set<PosixFilePermission> permissions, OpenOption... options) throws IOException {
        org.apache.sshd.server.session.ServerSession serverSession = (org.apache.sshd.server.session.ServerSession) session;
        SftpSUser user = (SftpSUser) serverSession.getProperties().get(SftpSUser.USER_SESSION);
        if (user.getFakeDictionary() != null && user.getOriginDictionary() != null) {
            return new DrmOutputStream(super.openWrite(session, file, size, permissions, options), user.getOriginDictionary());
        } else {
            return super.openWrite(session, file, size, permissions, options);
        }
    }

    @Override
    public InputStream openRead(Session session, Path file, long size, Set<PosixFilePermission> permissions, OpenOption... options) throws IOException {
        org.apache.sshd.server.session.ServerSession serverSession = (org.apache.sshd.server.session.ServerSession) session;
        SftpSUser user = (SftpSUser) serverSession.getProperties().get(SftpSUser.USER_SESSION);
        if (user.getFakeDictionary() != null && user.getOriginDictionary() != null) {
            return new DrmInputStream(super.openRead(session, file, size, permissions, options), user.getFakeDictionary());
        } else {
            return super.openRead(session, file, size, permissions, options);
        }
    }

}
