package com.senior.cyber.sftps.api.ftp;

import com.senior.cyber.sftps.api.SecretUtils;

import java.io.IOException;

public class DrmOutputStream extends java.io.OutputStream {

    private final java.io.OutputStream stream;

    private final byte[] dictionary;

    public DrmOutputStream(java.io.OutputStream stream, byte[] dictionary) {
        this.stream = stream;
        this.dictionary = dictionary;
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (int i = 0; i < b.length; i++) {
            b[i] = SecretUtils.translate(this.dictionary, b[i]);
        }
        stream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            b[i] = SecretUtils.translate(this.dictionary, b[i]);
        }
        stream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

}
