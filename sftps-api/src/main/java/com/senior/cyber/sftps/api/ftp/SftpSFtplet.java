package com.senior.cyber.sftps.api.ftp;

import com.senior.cyber.sftps.api.Audit;
import com.senior.cyber.sftps.api.dto.SftpSUser;
import com.senior.cyber.sftps.api.repository.KeyRepository;
import com.senior.cyber.sftps.api.repository.LogRepository;
import com.senior.cyber.sftps.api.repository.UserRepository;
import com.senior.cyber.sftps.api.tink.MasterAead;
import com.senior.cyber.sftps.api.tink.WebHook;
import com.senior.cyber.sftps.dao.entity.Key;
import com.senior.cyber.sftps.dao.entity.Log;
import com.senior.cyber.sftps.dao.entity.User;
import org.apache.ftpserver.ftplet.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SftpSFtplet extends DefaultFtplet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpSFtplet.class);

    private final CloseableHttpClient client;

    private final UserRepository userRepository;

    private final KeyRepository keyRepository;

    private final LogRepository logRepository;

    private final MasterAead masterAead;

    private ConcurrentHashMap<String, File> renameSession;

    private Map<String, Long> sizes = new ConcurrentHashMap<>();

    public SftpSFtplet(CloseableHttpClient client, UserRepository userRepository, KeyRepository keyRepository, LogRepository logRepository, MasterAead masterAead) {
        this.renameSession = new ConcurrentHashMap<>();
        this.client = client;
        this.userRepository = userRepository;
        this.keyRepository = keyRepository;
        this.logRepository = logRepository;
        this.masterAead = masterAead;
    }

    @Override
    public FtpletResult onLogin(FtpSession session, FtpRequest request) throws FtpException, IOException {
        Audit.log(session.getUser().getName() + " connected " + session.getClientAddress().getHostName());
        File home = new File(session.getUser().getHomeDirectory());
        home.mkdirs();
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onDownloadStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        return super.onDownloadStart(session, request);
    }

    @Override
    public FtpletResult onDownloadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        SftpSUser user = (SftpSUser) session.getUser();
        String userId = user.getUserId();
        String keyId = user.getKeyId();

        Optional<com.senior.cyber.sftps.dao.entity.User> optionalUser = this.userRepository.findById(Long.parseLong(userId));
        User userObject = optionalUser.orElseThrow(() -> new FtpException(""));

        Key keyObject = null;
        if (keyId != null) {
            Optional<Key> optionalKey = this.keyRepository.findById(Long.parseLong(keyId));
            keyObject = optionalKey.orElseThrow(() -> new FtpException(""));
        }

        FtpFile ftpFile = session.getFileSystemView().getFile(request.getArgument());
        File file = new File(session.getUser().getHomeDirectory(), ftpFile.getAbsolutePath());

        String path = file.getAbsolutePath().substring(userObject.getHomeDirectory().length());

        String key = null;
        if (keyObject != null) {
            key = keyObject.getName();
        }

        Log log = new Log();
        log.setCreatedAt(new Date());
        log.setEventType(Log.EVENT_TYPE_DOWNLOADED);
        log.setUserDisplayName(userObject.getDisplayName());
        log.setKeyName(key);
        log.setSize(file.length());
        log.setSrcPath(path);
        logRepository.save(log);
        WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onDeleteStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        FtpFile ftpFile = session.getFileSystemView().getFile(request.getArgument());
        File file = new File(session.getUser().getHomeDirectory(), ftpFile.getAbsolutePath());
        sizes.put(file.getAbsolutePath(), file.length());
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onDeleteEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        SftpSUser user = (SftpSUser) session.getUser();

        FtpFile ftpFile = session.getFileSystemView().getFile(request.getArgument());
        File file = new File(session.getUser().getHomeDirectory(), ftpFile.getAbsolutePath());

        String path = file.getAbsolutePath().substring(user.getHomeDirectory().length());

        long size = this.sizes.remove(file.getAbsolutePath());

        Log log = new Log();
        log.setCreatedAt(new Date());
        log.setEventType(Log.EVENT_TYPE_DELETED);
        log.setUserDisplayName(user.getUserDisplayName());
        log.setKeyName(user.getKeyName());
        log.setSrcPath(path);
        log.setSize(size);
        logRepository.save(log);
        WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        SftpSUser user = (SftpSUser) session.getUser();

        FtpFile ftpFile = session.getFileSystemView().getFile(request.getArgument());
        File file = new File(session.getUser().getHomeDirectory(), ftpFile.getAbsolutePath());

        String path = file.getAbsolutePath().substring(user.getHomeDirectory().length());

        Log log = new Log();
        log.setCreatedAt(new Date());
        log.setEventType(Log.EVENT_TYPE_UPLOADED);
        log.setUserDisplayName(user.getUserDisplayName());
        log.setKeyName(user.getKeyName());
        log.setSize(file.length());
        log.setSrcPath(path);
        logRepository.save(log);
        WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onRenameStart(FtpSession session, FtpRequest request) throws FtpException, IOException {
        FtpFile srcFtpFile = session.getRenameFrom();
        File srcFile = new File(session.getUser().getHomeDirectory(), srcFtpFile.getAbsolutePath());
        this.renameSession.put(session.getSessionId().toString(), srcFile);
        return FtpletResult.DEFAULT;
    }

    @Override
    public FtpletResult onRenameEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        SftpSUser user = (SftpSUser) session.getUser();

        FtpFile dstFtpFile = session.getFileSystemView().getFile(request.getArgument());

        File dstFile = new File(session.getUser().getHomeDirectory(), dstFtpFile.getAbsolutePath());
        File srcFile = this.renameSession.remove(session.getSessionId().toString());

        String dstPath = dstFile.getAbsolutePath().substring(user.getHomeDirectory().length());
        String srcPath = srcFile.getAbsolutePath().substring(user.getHomeDirectory().length());

        Log log = new Log();
        log.setCreatedAt(new Date());
        log.setEventType(Log.EVENT_TYPE_MOVED);
        log.setUserDisplayName(user.getUserDisplayName());
        log.setKeyName(user.getKeyName());
        log.setSize(dstFile.length());
        log.setSrcPath(srcPath);
        log.setDstPath(dstPath);
        logRepository.save(log);
        WebHook.report(this.client, this.userRepository, this.masterAead, log, user);
        return FtpletResult.DEFAULT;
    }
}
