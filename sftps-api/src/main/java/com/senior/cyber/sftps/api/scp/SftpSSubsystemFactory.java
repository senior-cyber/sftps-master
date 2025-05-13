package com.senior.cyber.sftps.api.scp;

import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.threads.CloseableExecutorService;
import org.apache.sshd.server.channel.ChannelDataReceiver;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.apache.sshd.sftp.server.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class SftpSSubsystemFactory extends org.apache.sshd.sftp.server.SftpSubsystemFactory {

    private final org.apache.sshd.sftp.server.SftpSubsystemFactory delegate;

    public SftpSSubsystemFactory(SftpSubsystemFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Supplier<? extends CloseableExecutorService> getExecutorServiceProvider() {
        return delegate.getExecutorServiceProvider();
    }

    @Override
    public void setExecutorServiceProvider(Supplier<? extends CloseableExecutorService> provider) {
        delegate.setExecutorServiceProvider(provider);
    }

    @Override
    public UnsupportedAttributePolicy getUnsupportedAttributePolicy() {
        return delegate.getUnsupportedAttributePolicy();
    }

    @Override
    public void setUnsupportedAttributePolicy(UnsupportedAttributePolicy p) {
        delegate.setUnsupportedAttributePolicy(p);
    }

    @Override
    public SftpFileSystemAccessor getFileSystemAccessor() {
        SftpFileSystemAccessor accessor = delegate.getFileSystemAccessor();
        return new SftpSFileSystemAccessor(accessor);
    }

    @Override
    public void setFileSystemAccessor(SftpFileSystemAccessor accessor) {
        delegate.setFileSystemAccessor(accessor);
    }

    @Override
    public SftpErrorStatusDataHandler getErrorStatusDataHandler() {
        return delegate.getErrorStatusDataHandler();
    }

    @Override
    public void setErrorStatusDataHandler(SftpErrorStatusDataHandler handler) {
        delegate.setErrorStatusDataHandler(handler);
    }

    @Override
    public CloseableExecutorService getExecutorService() {
        return delegate.getExecutorService();
    }

    @Override
    public ChannelDataReceiver getErrorChannelDataReceiver() {
        return delegate.getErrorChannelDataReceiver();
    }

    @Override
    public void setErrorChannelDataReceiver(ChannelDataReceiver errorChannelDataReceiver) {
        delegate.setErrorChannelDataReceiver(errorChannelDataReceiver);
    }

    @Override
    public Command createSubsystem(ChannelSession channel) throws IOException {
        SftpSSubsystem subsystem = new SftpSSubsystem(channel, this);
        GenericUtils.forEach(getRegisteredListeners(), subsystem::addSftpEventListener);
        return subsystem;
    }

    @Override
    public Collection<SftpEventListener> getRegisteredListeners() {
        return delegate.getRegisteredListeners();
    }

    @Override
    public SftpEventListener getSftpEventListenerProxy() {
        return delegate.getSftpEventListenerProxy();
    }

    @Override
    public boolean addSftpEventListener(SftpEventListener listener) {
        return delegate.addSftpEventListener(listener);
    }

    @Override
    public boolean removeSftpEventListener(SftpEventListener listener) {
        return delegate.removeSftpEventListener(listener);
    }

    @Override
    public CloseableExecutorService resolveExecutorService() {
        return delegate.resolveExecutorService();
    }

    public static Command createSubsystem(ChannelSession channel, Collection<? extends SubsystemFactory> factories, String name) throws IOException {
        return SubsystemFactory.createSubsystem(channel, factories, name);
    }

    public static List<String> getNameList(Collection<? extends NamedResource> resources) {
        return NamedResource.getNameList(resources);
    }

    public static String getNames(Collection<? extends NamedResource> resources) {
        return NamedResource.getNames(resources);
    }

    public static <R extends NamedResource> R removeByName(String name, Comparator<? super String> c, Collection<? extends R> resources) {
        return NamedResource.removeByName(name, c, resources);
    }

    public static <R extends NamedResource> R findByName(String name, Comparator<? super String> c, Collection<? extends R> resources) {
        return NamedResource.findByName(name, c, resources);
    }

    public static <R extends NamedResource> R findFirstMatchByName(Collection<String> names, Comparator<? super String> c, Collection<? extends R> resources) {
        return NamedResource.findFirstMatchByName(names, c, resources);
    }

    public static NamedResource ofName(String name) {
        return NamedResource.ofName(name);
    }

    public static int safeCompareByName(NamedResource r1, NamedResource r2, boolean caseSensitive) {
        return NamedResource.safeCompareByName(r1, r2, caseSensitive);
    }

}
