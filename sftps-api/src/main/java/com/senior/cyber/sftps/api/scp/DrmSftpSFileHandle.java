package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.SecretUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DrmSftpSFileHandle extends org.apache.sshd.sftp.server.FileHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrmSftpSFileHandle.class);

    private final byte[] originDictionary;
    private final byte[] fakeDictionary;

    public DrmSftpSFileHandle(SftpSSubsystem subsystem, Path file, String handle, int flags, int access, Map<String, Object> attrs, byte[] originDictionary, byte[] fakeDictionary) throws IOException {
        super(subsystem, file, handle, flags, access, attrs);
        this.originDictionary = originDictionary;
        this.fakeDictionary = fakeDictionary;
    }

    @Override
    public void write(byte[] data, int doff, int length, long offset) throws IOException {
        LOGGER.info("DrmSftpSFileHandle write");
        for (int i = doff; i < doff + length; i++) {
            data[i] = SecretUtils.translate(this.originDictionary, data[i]);
        }
        super.write(data, doff, length, offset);
    }

    @Override
    public int read(byte[] data, int doff, int length, long offset, AtomicReference<Boolean> eof) throws IOException {
        LOGGER.info("DrmSftpSFileHandle read");
        int read = super.read(data, doff, length, offset, eof);
        for (int i = doff; i < doff + length; i++) {
            data[i] = SecretUtils.translate(this.fakeDictionary, data[i]);
        }
        return read;
    }
    
}
