package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.dto.SftpSUser;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.apache.sshd.sftp.common.SftpConstants;
import org.apache.sshd.sftp.common.SftpException;
import org.apache.sshd.sftp.server.SftpSubsystemConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class SftpSSubsystem extends org.apache.sshd.sftp.server.SftpSubsystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpSSubsystem.class);

    public SftpSSubsystem(ChannelSession channel, SftpSubsystemConfigurator configurator) {
        super(channel, configurator);
    }

    @Override
    protected String doOpen(int id, String path, int pflags, int access, Map<String, Object> attrs) throws IOException {
        ServerSession session = getServerSession();
        SftpSUser user = (SftpSUser) session.getProperties().get(SftpSUser.USER_SESSION);

        LOGGER.info("encryptAtRest [{}] fakeDictionary [{}] originDictionary [{}]", user.isEncryptAtRest(), user.getFakeDictionary() != null, user.getOriginDictionary() != null);

        if (!user.isEncryptAtRest() || user.getFakeDictionary() == null || user.getOriginDictionary() == null) {
            LOGGER.info("doOpen FileHandle");
            return super.doOpen(id, path, pflags, access, attrs);
        }

        if (log.isDebugEnabled()) {
            log.debug("doOpen({})[id={}] SSH_FXP_OPEN (path={}, access=0x{}, pflags=0x{}, attrs={})",
                    session, id, path, Integer.toHexString(access), Integer.toHexString(pflags), attrs);
        }
        Path file = resolveFile(path);
        int curHandleCount = handles.size();
        int maxHandleCount = SftpModuleProperties.MAX_OPEN_HANDLES_PER_SESSION.getRequired(session);
        if (curHandleCount > maxHandleCount) {
            throw signalOpenFailure(id, path, file, false,
                    new SftpException(
                            SftpConstants.SSH_FX_NO_SPACE_ON_FILESYSTEM,
                            "Too many open handles: current=" + curHandleCount + ", max.=" + maxHandleCount));
        }

        String handle;
        try {
            synchronized (handles) {
                handle = generateFileHandle(file);
                LOGGER.info("doOpen DrmSftpSFileHandle");
                DrmSftpSFileHandle fileHandle = new DrmSftpSFileHandle(this, file, handle, pflags, access, attrs, user.getOriginDictionary(), user.getFakeDictionary());
                handles.put(handle, fileHandle);
            }
        } catch (IOException e) {
            throw signalOpenFailure(id, path, file, false, e);
        }

        return handle;
    }
}
