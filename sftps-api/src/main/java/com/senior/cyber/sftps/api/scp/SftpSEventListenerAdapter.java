package com.senior.cyber.sftps.api.scp;

import com.senior.cyber.sftps.api.dto.SftpSUser;
import com.senior.cyber.sftps.api.tink.MasterAead;
import com.senior.cyber.sftps.api.tink.WebHook;
import com.senior.cyber.sftps.dao.enums.EventTypeEnum;
import com.senior.cyber.sftps.dao.entity.sftps.Log;
import com.senior.cyber.sftps.dao.repository.rbac.UserRepository;
import com.senior.cyber.sftps.dao.repository.sftps.LogRepository;
import org.apache.commons.io.FilenameUtils;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.AbstractSftpEventListenerAdapter;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SftpSEventListenerAdapter extends AbstractSftpEventListenerAdapter {

    private Map<String, String> uploadHandles = new ConcurrentHashMap<>();

    private Map<String, String> downloadHandles = new ConcurrentHashMap<>();

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    private List<String> temps = Collections.synchronizedList(new ArrayList<>());

    private Map<String, Long> sizes = new ConcurrentHashMap<>();

    private final HttpClient client;

    private final LogRepository logRepository;

    private final UserRepository userRepository;

    private final MasterAead masterAead;

    public SftpSEventListenerAdapter(LogRepository logRepository, HttpClient client, UserRepository userRepository, MasterAead masterAead) {
        this.logRepository = logRepository;
        this.client = client;
        this.userRepository = userRepository;
        this.masterAead = masterAead;
    }

    @Override
    public void written(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data, int dataOffset, int dataLen, Throwable thrown) throws IOException {
        this.uploadHandles.put(remoteHandle, localHandle.getFile().toFile().getAbsolutePath());
    }

    @Override
    public void read(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data, int dataOffset, int dataLen, int readLen, Throwable thrown) throws IOException {
        this.downloadHandles.put(remoteHandle, localHandle.getFile().toFile().getAbsolutePath());
    }

    @Override
    public void removing(ServerSession session, Path path, boolean isDirectory) throws IOException {
        File file = path.toFile();
        if (file.isFile()) {
            sizes.put(file.getAbsolutePath(), file.length());
        }
    }

    @Override
    public void removed(ServerSession session, Path file, boolean isDirectory, Throwable thrown) throws IOException {
        SftpSUser user = (SftpSUser) session.getProperties().get(SftpSUser.USER_SESSION);

        String path = file.toFile().getAbsolutePath().substring(user.getHomeDirectory().length());

        long size = sizes.remove(file.toFile().getAbsolutePath());

        Log log = new Log();
        log.setCreatedAt(new Date());
        log.setEventType(EventTypeEnum.Deleted);
        log.setUserDisplayName(user.getUserDisplayName());
        log.setKeyName(user.getKeyName());
        log.setSrcPath(path);
        log.setSize(size);
        logRepository.save(log);
        WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
    }

    @Override
    public void moved(ServerSession session, Path srcPath, Path dstPath, Collection<CopyOption> opts, Throwable thrown) throws IOException {
        SftpSUser user = (SftpSUser) session.getProperties().get(SftpSUser.USER_SESSION);

        File srcFile = srcPath.toFile();
        File dstFile = dstPath.toFile();

        String srcPathS = srcFile.getAbsolutePath().substring(user.getHomeDirectory().length());
        String dstPathS = dstFile.getAbsolutePath().substring(user.getHomeDirectory().length());

        if (temps.contains(srcFile.getAbsolutePath())) {
            temps.remove(srcFile.getAbsolutePath());
        } else {
            Log log = new Log();
            log.setCreatedAt(new Date());
            log.setEventType(EventTypeEnum.Moved);
            log.setUserDisplayName(user.getUserDisplayName());
            log.setKeyName(user.getKeyName());
            log.setSize(dstFile.length());
            log.setSrcPath(srcPathS);
            log.setDstPath(dstPathS);
            logRepository.save(log);
            WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
        }
    }

    @Override
    public void closed(ServerSession session, String remoteHandle, Handle localHandle, Throwable thrown) throws IOException {
        SftpSUser user = (SftpSUser) session.getProperties().get(SftpSUser.USER_SESSION);
        final File file = localHandle.getFile().toFile();
        if (this.uploadHandles.containsKey(remoteHandle)) {
            this.uploadHandles.remove(remoteHandle);
            if (FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("filepart")) {
                temps.add(file.getAbsolutePath());
                service.schedule(() -> {
                    if (file.exists()) {
                        String name = file.getAbsolutePath();
                        if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("filepart")) {
                            name = new File(file.getParent(), FilenameUtils.getBaseName(file.getName())).getAbsolutePath();
                        }
                        String path = name.substring(user.getHomeDirectory().length());
                        Log log = new Log();
                        log.setCreatedAt(new Date());
                        log.setEventType(EventTypeEnum.Uploaded);
                        log.setUserDisplayName(user.getUserDisplayName());
                        log.setKeyName(user.getKeyName());
                        log.setSize(file.length());
                        log.setSrcPath(path);
                        logRepository.save(log);
                        WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
                    } else {
                        File newFile = new File(file.getParent(), FilenameUtils.getBaseName(file.getName()));
                        String path = file.getAbsolutePath().substring(user.getHomeDirectory().length());
                        if (newFile.exists()) {
                            Log log = new Log();
                            log.setCreatedAt(new Date());
                            log.setEventType(EventTypeEnum.Uploaded);
                            log.setUserDisplayName(user.getUserDisplayName());
                            log.setKeyName(user.getKeyName());
                            log.setSize(file.length());
                            log.setSrcPath(path);
                            logRepository.save(log);
                            WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
                        }
                    }
                }, 100, TimeUnit.MICROSECONDS);
            } else {
                if (file.exists()) {
                    String path = file.getAbsolutePath().substring(user.getHomeDirectory().length());
                    Log log = new Log();
                    log.setCreatedAt(new Date());
                    log.setEventType(EventTypeEnum.Uploaded);
                    log.setUserDisplayName(user.getUserDisplayName());
                    log.setKeyName(user.getKeyName());
                    log.setSize(file.length());
                    log.setSrcPath(path);
                    logRepository.save(log);
                    WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
                }
            }
            return;
        }
        if (this.downloadHandles.containsKey(remoteHandle)) {
            this.downloadHandles.remove(remoteHandle);
            String path = file.getAbsolutePath().substring(user.getHomeDirectory().length());

            Log log = new Log();
            log.setCreatedAt(new Date());
            log.setEventType(EventTypeEnum.Downloaded);
            log.setUserDisplayName(user.getUserDisplayName());
            log.setKeyName(user.getKeyName());
            log.setSize(file.length());
            log.setSrcPath(path);
            logRepository.save(log);
            WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
        }
    }

}
