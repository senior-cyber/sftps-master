package com.senior.cyber.sftps.api;

import com.google.crypto.tink.*;
import com.senior.cyber.sftps.api.configuration.AppConfig;
import com.senior.cyber.sftps.api.dto.SftpSUser;
import com.senior.cyber.sftps.dao.entity.rbac.User;
import com.senior.cyber.sftps.dao.entity.sftps.Key;
import com.senior.cyber.sftps.dao.repository.rbac.UserRepository;
import com.senior.cyber.sftps.dao.repository.sftps.KeyRepository;
import com.senior.cyber.sftps.x509.CertificateUtils;
import com.senior.cyber.sftps.x509.PublicKeyUtils;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.jasypt.util.password.PasswordEncryptor;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserManager implements org.apache.ftpserver.ftplet.UserManager, PasswordAuthenticator, PublickeyAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

    private final UserRepository userRepository;

    private final KeyRepository keyRepository;

    private final PasswordEncryptor passwordEncryptor;

    private final AppConfig configuration;

    public UserManager(PasswordEncryptor passwordEncryptor, UserRepository userRepository, KeyRepository keyRepository, AppConfig configuration) {
        this.userRepository = userRepository;
        this.keyRepository = keyRepository;
        this.passwordEncryptor = passwordEncryptor;
        this.configuration = configuration;
    }

    @Override
    public org.apache.ftpserver.ftplet.User getUserByName(String username) throws FtpException {
        try {
            User userObject = this.userRepository.findByLogin(username);
            if (userObject == null) {
                throw new FtpException(username + " is not found");
            }

            String dek = userObject.getDek();
            Aead dekAead = null;
            if (dek != null && !dek.isBlank()) {
                KeysetHandle handle = TinkProtoKeysetFormat.parseKeyset(Base64.getDecoder().decode(dek), InsecureSecretKeyAccess.get());
                dekAead = handle.getPrimitive(RegistryConfiguration.get(), Aead.class);
            }

            File homeDirectory = new File(configuration.getWorkspace(), userObject.getHomeDirectory());
            boolean made = homeDirectory.mkdirs();
            if (!made) {
                LOGGER.info("make directory [{}]", homeDirectory.exists());
            }

            return authenticated(String.valueOf(userObject.getId()), username, null, null, userObject.getDisplayName(), userObject.getSecret(), dekAead, homeDirectory, userObject.isEncryptAtRest());
        } catch (GeneralSecurityException | IOException e) {
            throw new FtpException(e);
        }
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        return null;
    }

    @Override
    public void delete(String username) throws FtpException {
    }

    @Override
    public void save(org.apache.ftpserver.ftplet.User user) throws FtpException {
    }

    @Override
    public boolean doesExist(String username) throws FtpException {
        return this.userRepository.existsByLogin(username);
    }

    @Override
    public org.apache.ftpserver.ftplet.User authenticate(Authentication authentication) throws AuthenticationFailedException {
        if (authentication instanceof UsernamePasswordAuthentication upauth) {

            String login = upauth.getUsername();
            String password = upauth.getPassword();

            if (login == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }

            if (upauth.getUserMetadata() != null && upauth.getUserMetadata().getCertificateChain() != null && upauth.getUserMetadata().getCertificateChain().length >= 1) {
                if ("USINGCERT".equals(password)) {
                    password = null;
                }
            }

            if ((password == null || password.isBlank()) && (upauth.getUserMetadata().getCertificateChain() == null || upauth.getUserMetadata().getCertificateChain().length == 0)) {
                throw new AuthenticationFailedException("Authentication failed");
            } else if ((password != null && !password.isBlank()) && (upauth.getUserMetadata().getCertificateChain() != null && upauth.getUserMetadata().getCertificateChain().length >= 1)) {
                throw new AuthenticationFailedException("Authentication failed");
            }

            User userObject = this.userRepository.findByLogin(login);
            if (userObject == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }

            if (!userObject.isEnabled()) {
                throw new AuthenticationFailedException("Authentication failed");
            }

            String userId = String.valueOf(userObject.getId());

            if (password != null && !password.isBlank()) {
                if (this.passwordEncryptor.checkPassword(password, userObject.getPassword())) {
                    userObject.setLastSeen(LocalDate.now().toDate());
                    userRepository.save(userObject);

                    try {
                        String dek = userObject.getDek();
                        Aead dekAead = null;
                        if (dek != null && !dek.isBlank()) {
                            KeysetHandle handle = TinkProtoKeysetFormat.parseKeyset(Base64.getDecoder().decode(dek), InsecureSecretKeyAccess.get());
                            dekAead = handle.getPrimitive(RegistryConfiguration.get(), Aead.class);
                        }

                        return authenticated(userId, login, null, null, userObject.getDisplayName(), userObject.getSecret(), dekAead, new File(configuration.getWorkspace(), userObject.getHomeDirectory()), userObject.isEncryptAtRest());
                    } catch (GeneralSecurityException | IOException e) {
                        throw new AuthenticationFailedException(e);
                    }
                }
            }

            if (upauth.getUserMetadata().getCertificateChain() != null && upauth.getUserMetadata().getCertificateChain().length >= 1) {
                try {
                    PublicKey a = PublicKeyUtils.read(PublicKeyUtils.write(upauth.getUserMetadata().getCertificateChain()[0].getPublicKey()));
                    List<Key> keys = this.keyRepository.findByUser(userObject);
                    if (!keys.isEmpty()) {
                        for (Key key : keys) {
                            X509Certificate certificate = CertificateUtils.read(key.getCertificate());
                            PublicKey b = PublicKeyUtils.read(PublicKeyUtils.write(certificate.getPublicKey()));
                            if (key.isEnabled() && a != null && a.equals(b)) {
                                String keyId = String.valueOf(key.getId());

                                userObject.setLastSeen(LocalDate.now().toDate());
                                userRepository.save(userObject);

                                key.setLastSeen(LocalDate.now().toDate());
                                keyRepository.save(key);

                                String dek = userObject.getDek();
                                Aead dekAead = null;
                                if (dek != null && !dek.isBlank()) {
                                    KeysetHandle handle = TinkProtoKeysetFormat.parseKeyset(Base64.getDecoder().decode(dek), InsecureSecretKeyAccess.get());
                                    dekAead = handle.getPrimitive(RegistryConfiguration.get(), Aead.class);
                                }

                                return authenticated(userId, login, keyId, key.getName(), userObject.getDisplayName(), userObject.getSecret(), dekAead, new File(configuration.getWorkspace(), userObject.getHomeDirectory()), userObject.isEncryptAtRest());
                            }
                        }
                    }
                } catch (IOException | GeneralSecurityException e) {
                    throw new AuthenticationFailedException(e);
                }
            }
            throw new AuthenticationFailedException("Authentication failed");
        } else if (authentication instanceof AnonymousAuthentication) {
            throw new AuthenticationFailedException("anonymous not supported");
        } else {
            throw new AuthenticationFailedException("Authentication not supported by this user manager");
        }
    }

    private SftpSUser authenticated(String userId, String userName, String keyId, String keyName, String userDisplayName, String black_secret, Aead dek, File homeDirectory, boolean encryptAtRest) throws GeneralSecurityException, IOException {
        LOGGER.info("userName [{}] homeDirectory [{}]", userName, homeDirectory.getAbsolutePath());
        String white_secret = null;
        if (black_secret != null && !black_secret.isBlank()) {
            if (dek != null) {
                white_secret = new String(dek.decrypt(Base64.getDecoder().decode(black_secret), "".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            } else {
                white_secret = black_secret;
            }
        }
        SftpSUser user = new SftpSUser(userId, keyId, keyName, userDisplayName, white_secret, encryptAtRest);
        user.setName(userName);
        boolean made = homeDirectory.mkdirs();
        if (!made) {
            LOGGER.info("make directory [{}]", homeDirectory.exists());
        }
        user.setHomeDirectory(homeDirectory.getAbsolutePath());
        user.setEnabled(true);
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(20, 20));
        authorities.add(new TransferRatePermission(Integer.MAX_VALUE, Integer.MAX_VALUE));
        user.setAuthorities(authorities);
        user.setMaxIdleTime(60);
        return user;
    }

    @Override
    public String getAdminName() throws FtpException {
        return null;
    }

    @Override
    public boolean isAdmin(String username) throws FtpException {
        return false;
    }

    @Override
    public boolean authenticate(String username, PublicKey password, ServerSession session) throws AsyncAuthException {
        try {
            User userObject = this.userRepository.findByLogin(username);
            if (userObject == null || !userObject.isEnabled()) {
                return false;
            }

            PublicKey a = PublicKeyUtils.read(PublicKeyUtils.write(password));

            List<Key> keys = this.keyRepository.findByUser(userObject);

            for (Key key : keys) {
                X509Certificate certificate = CertificateUtils.read(key.getCertificate());
                PublicKey b = PublicKeyUtils.read(PublicKeyUtils.write(certificate.getPublicKey()));
                if (key.isEnabled() && a != null && a.equals(b)) {
                    String keyId = String.valueOf(key.getId());

                    userObject.setLastSeen(LocalDate.now().toDate());
                    userRepository.save(userObject);

                    key.setLastSeen(LocalDate.now().toDate());
                    keyRepository.save(key);

                    Audit.log(username + " connected " + ((InetSocketAddress) session.getClientAddress()).getHostName());

                    File homeDirectory = new File(configuration.getWorkspace(), userObject.getHomeDirectory());
                    boolean made = homeDirectory.mkdirs();
                    if (!made) {
                        LOGGER.info("make directory [{}]", homeDirectory.exists());
                    }

                    String dek = userObject.getDek();
                    Aead dekAead = null;
                    if (dek != null && !dek.isBlank()) {
                        KeysetHandle handle = TinkProtoKeysetFormat.parseKeyset(Base64.getDecoder().decode(dek), InsecureSecretKeyAccess.get());
                        dekAead = handle.getPrimitive(RegistryConfiguration.get(), Aead.class);
                    }

                    SftpSUser user = authenticated(String.valueOf(userObject.getId()), username, keyId, key.getName(), userObject.getDisplayName(), userObject.getSecret(), dekAead, homeDirectory, userObject.isEncryptAtRest());
                    session.getProperties().put(SftpSUser.USER_SESSION, user);

                    return true;
                }
            }
            return false;
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
        try {
            User userObject = this.userRepository.findByLogin(username);
            if (userObject == null || !userObject.isEnabled()) {
                return false;
            }

            if (this.passwordEncryptor.checkPassword(password, userObject.getPassword())) {

                userObject.setLastSeen(LocalDate.now().toDate());
                userRepository.save(userObject);

                Audit.log(username + " connected " + ((InetSocketAddress) session.getClientAddress()).getHostName());

                File homeDirectory = new File(configuration.getWorkspace(), userObject.getHomeDirectory());
                boolean made = homeDirectory.mkdirs();
                if (!made) {
                    LOGGER.info("make directory [{}]", homeDirectory.exists());
                }

                String dek = userObject.getDek();
                Aead dekAead = null;
                if (dek != null && !dek.isBlank()) {
                    KeysetHandle handle = TinkProtoKeysetFormat.parseKeyset(Base64.getDecoder().decode(dek), InsecureSecretKeyAccess.get());
                    dekAead = handle.getPrimitive(RegistryConfiguration.get(), Aead.class);
                }

                SftpSUser user = authenticated(String.valueOf(userObject.getId()), username, null, null, userObject.getDisplayName(), userObject.getSecret(), dekAead, homeDirectory, userObject.isEncryptAtRest());
                session.getProperties().put(SftpSUser.USER_SESSION, user);

                return true;
            }
            return false;
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
