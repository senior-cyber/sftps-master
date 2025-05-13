package com.senior.cyber.sftps.api.scp;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

public class SftpSFileSystem extends FileSystem {

    private final FileSystem delegate;

    public SftpSFileSystem(FileSystem delegate) {
        this.delegate = delegate;
    }

    @Override
    public FileSystemProvider provider() {
        return delegate.provider();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return delegate.getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return delegate.getRootDirectories();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return delegate.getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        try {
            return delegate.supportedFileAttributeViews();
        } catch (Throwable e) {
            return Collections.emptySet();
        }
    }

    @Override
    public Path getPath(String first, String... more) {
        return delegate.getPath(first, more);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return delegate.getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return delegate.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return delegate.newWatchService();
    }

}
