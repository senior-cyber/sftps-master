package com.senior.cyber.sftps.api.ftp;

import com.senior.cyber.sftps.api.SecretUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DrmInputStream extends java.io.InputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrmInputStream.class);

    private final java.io.InputStream stream;

    private final byte[] dictionary;

    public DrmInputStream(java.io.InputStream stream, byte[] dictionary) {
        this.stream = stream;
        this.dictionary = dictionary;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = stream.read(b);
        for (int i = 0; i < b.length; i++) {
            b[i] = SecretUtils.translate(this.dictionary, b[i]);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = stream.read(b, off, len);
        for (int i = off; i < off + len; i++) {
            b[i] = SecretUtils.translate(this.dictionary, b[i]);
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
}
