package com.senior.cyber.sftps.api.scp;

import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.sftp.server.DirectoryHandle;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.SftpFileSystemAccessor;
import org.apache.sshd.sftp.server.SftpSubsystemProxy;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.Principal;
import java.util.*;

public class SftpSFileSystemAccessor implements SftpFileSystemAccessor {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private final SftpFileSystemAccessor delegate;

    public SftpSFileSystemAccessor(SftpFileSystemAccessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public Path resolveLocalFilePath(SftpSubsystemProxy subsystem, Path rootDir, String remotePath) throws IOException, InvalidPathException {
        return delegate.resolveLocalFilePath(subsystem, rootDir, remotePath);
    }

    @Override
    public LinkOption[] resolveFileAccessLinkOptions(SftpSubsystemProxy subsystem, Path file, int cmd, String extension, boolean followLinks) throws IOException {
        return delegate.resolveFileAccessLinkOptions(subsystem, file, cmd, extension, followLinks);
    }

    @Override
    public NavigableMap<String, Object> resolveReportedFileAttributes(SftpSubsystemProxy subsystem, Path file, int flags, NavigableMap<String, Object> attrs, LinkOption... options) throws IOException {
        return delegate.resolveReportedFileAttributes(subsystem, file, flags, attrs, options);
    }

    @Override
    public void applyExtensionFileAttributes(SftpSubsystemProxy subsystem, Path file, Map<String, byte[]> extensions, LinkOption... options) throws IOException {
        delegate.applyExtensionFileAttributes(subsystem, file, extensions, options);
    }

    @Override
    public void putRemoteFileName(SftpSubsystemProxy subsystem, Path path, Buffer buf, String name, boolean shortName) throws IOException {
        delegate.putRemoteFileName(subsystem, path, buf, name, shortName);
    }

    @Override
    public SeekableByteChannel openFile(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        if (!OS.contains("win")) {
            return delegate.openFile(subsystem, fileHandle, file, handle, options, attrs);
        } else {
            return FileChannel.open(file, options, attrs);
        }
    }

    @Override
    public FileLock tryLock(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel, long position, long size, boolean shared) throws IOException {
        return delegate.tryLock(subsystem, fileHandle, file, handle, channel, position, size, shared);
    }

    @Override
    public void syncFileData(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel) throws IOException {
        delegate.syncFileData(subsystem, fileHandle, file, handle, channel);
    }

    @Override
    public void closeFile(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle, Channel channel, Set<? extends OpenOption> options) throws IOException {
        delegate.closeFile(subsystem, fileHandle, file, handle, channel, options);
    }

    @Override
    public DirectoryStream<Path> openDirectory(SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir, String handle, LinkOption... linkOptions) throws IOException {
        return delegate.openDirectory(subsystem, dirHandle, dir, handle, linkOptions);
    }

    @Override
    public void closeDirectory(SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir, String handle, DirectoryStream<Path> ds) throws IOException {
        delegate.closeDirectory(subsystem, dirHandle, dir, handle, ds);
    }

    @Override
    public Map<String, ?> readFileAttributes(SftpSubsystemProxy subsystem, Path file, String view, LinkOption... options) throws IOException {
        return delegate.readFileAttributes(subsystem, file, view, options);
    }

    @Override
    public void setFileAttribute(SftpSubsystemProxy subsystem, Path file, String view, String attribute, Object value, LinkOption... options) throws IOException {
        delegate.setFileAttribute(subsystem, file, view, attribute, value, options);
    }

    @Override
    public UserPrincipal resolveFileOwner(SftpSubsystemProxy subsystem, Path file, UserPrincipal name) throws IOException {
        return delegate.resolveFileOwner(subsystem, file, name);
    }

    @Override
    public void setFileOwner(SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options) throws IOException {
        delegate.setFileOwner(subsystem, file, value, options);
    }

    @Override
    public GroupPrincipal resolveGroupOwner(SftpSubsystemProxy subsystem, Path file, GroupPrincipal name) throws IOException {
        return delegate.resolveGroupOwner(subsystem, file, name);
    }

    @Override
    public void setGroupOwner(SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options) throws IOException {
        delegate.setGroupOwner(subsystem, file, value, options);
    }

    @Override
    public void setFilePermissions(SftpSubsystemProxy subsystem, Path file, Set<PosixFilePermission> perms, LinkOption... options) throws IOException {
        delegate.setFilePermissions(subsystem, file, perms, options);
    }

    @Override
    public void setFileAccessControl(SftpSubsystemProxy subsystem, Path file, List<AclEntry> acl, LinkOption... options) throws IOException {
        delegate.setFileAccessControl(subsystem, file, acl, options);
    }

    @Override
    public void createDirectory(SftpSubsystemProxy subsystem, Path path) throws IOException {
        delegate.createDirectory(subsystem, path);
    }

    @Override
    public void createLink(SftpSubsystemProxy subsystem, Path link, Path existing, boolean symLink) throws IOException {
        delegate.createLink(subsystem, link, existing, symLink);
    }

    @Override
    public String resolveLinkTarget(SftpSubsystemProxy subsystem, Path link) throws IOException {
        return delegate.resolveLinkTarget(subsystem, link);
    }

    @Override
    public void renameFile(SftpSubsystemProxy subsystem, Path oldPath, Path newPath, Collection<CopyOption> opts) throws IOException {
        delegate.renameFile(subsystem, oldPath, newPath, opts);
    }

    @Override
    public void copyFile(SftpSubsystemProxy subsystem, Path src, Path dst, Collection<CopyOption> opts) throws IOException {
        delegate.copyFile(subsystem, src, dst, opts);
    }

    @Override
    public void removeFile(SftpSubsystemProxy subsystem, Path path, boolean isDirectory) throws IOException {
        delegate.removeFile(subsystem, path, isDirectory);
    }

    @Override
    public boolean noFollow(Collection<?> opts) {
        return delegate.noFollow(opts);
    }

}
