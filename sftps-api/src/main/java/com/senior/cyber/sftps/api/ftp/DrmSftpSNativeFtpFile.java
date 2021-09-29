package com.senior.cyber.sftps.api.ftp;

import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DrmSftpSNativeFtpFile extends NativeFtpFile {

    private final byte[] originDictionary;
    private final byte[] fakeDictionary;

    public DrmSftpSNativeFtpFile(String fileName, File file, org.apache.ftpserver.ftplet.User user, byte[] originDictionary, byte[] fakeDictionary) {
        super(fileName, file, user);
        this.originDictionary = originDictionary;
        this.fakeDictionary = fakeDictionary;
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        InputStream stream = super.createInputStream(offset);
        return new DrmInputStream(stream, this.fakeDictionary);
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        OutputStream stream = super.createOutputStream(offset);
        return new DrmOutputStream(stream, this.originDictionary);
    }

}
