package com.senior.cyber.sftps.api.factory;

import com.senior.cyber.sftps.api.BootApplication;
import com.senior.cyber.sftps.api.UserManager;
import com.senior.cyber.sftps.api.configuration.ApplicationConfiguration;
import com.senior.cyber.sftps.api.ftp.SftpSFtplet;
import com.senior.cyber.sftps.api.ftp.SftpSNativeFileSystemFactory;
import com.senior.cyber.sftps.api.repository.KeyRepository;
import com.senior.cyber.sftps.api.repository.LogRepository;
import com.senior.cyber.sftps.api.repository.UserRepository;
import com.senior.cyber.sftps.api.scp.ScpFileOpener;
import com.senior.cyber.sftps.api.scp.SftpSEventListenerAdapter;
import com.senior.cyber.sftps.api.scp.SftpSSubsystemFactory;
import com.senior.cyber.sftps.api.scp.SftpSVirtualFileSystemFactory;
import com.senior.cyber.sftps.api.tink.MasterAead;
import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;
import java.nio.file.FileSystems;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SftpSFactory extends AbstractFactoryBean<SftpS> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpSFactory.class);

    private FtpServer ftpserver = null;

    private FtpServer ftpsserver = null;

    private SshServer sshd = null;

    @Autowired
    protected ApplicationConfiguration configuration;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected KeyRepository keyRepository;

    @Autowired
    protected PasswordEncryptor passwordEncryptor;

    @Autowired
    protected LogRepository logRepository;

    @Autowired
    protected MasterAead masterAead;

    @Autowired
    protected CloseableHttpClient client;

    @Override
    public Class<?> getObjectType() {
        return SftpS.class;
    }

    @Override
    protected SftpS createInstance() throws Exception {

        int ftpsPort = this.configuration.getFtpsPort();

        SslConfiguration sslConfiguration = null;
        if (ftpsPort != -1 && ftpsPort != 0) {
            SslConfigurationFactory factory = new SslConfigurationFactory();
            File trustStore = this.configuration.getTrustStore();
            factory.setTruststoreType(this.configuration.getTrustStoreType());
            if (trustStore != null) {
                factory.setTruststoreFile(trustStore);
            }
            String truststorePassword = this.configuration.getTrustStorePassword();
            if (truststorePassword != null) {
                factory.setTruststorePassword(truststorePassword);
            } else {
                factory.setTruststorePassword("");
            }

            String clientAuthentication = this.configuration.getClientAuth();
            if ("optional".equalsIgnoreCase(clientAuthentication)) {
                factory.setClientAuthentication(ClientAuth.WANT.name());
            } else if ("yes".equalsIgnoreCase(clientAuthentication) || "true".equalsIgnoreCase(clientAuthentication)) {
                factory.setClientAuthentication(ClientAuth.NEED.name());
            } else if ("no".equalsIgnoreCase(clientAuthentication) || "false".equalsIgnoreCase(clientAuthentication)) {
                factory.setClientAuthentication(ClientAuth.NONE.name());
            }

            String keyAlias = this.configuration.getKeyAlias();
            if (keyAlias != null && !"".equals(keyAlias)) {
                factory.setKeyAlias(keyAlias);
            }

            String keyPassword = this.configuration.getKeyPassword();
            if (keyPassword != null) {
                factory.setKeyPassword(keyPassword);
            } else {
                factory.setKeyPassword("");
            }

            File keyStore = this.configuration.getKeyStore();
            factory.setKeystoreType(this.configuration.getKeyStoreType());
            if (keyStore != null) {
                factory.setKeystoreFile(keyStore);
            }

            String keyStorePassword = this.configuration.getKeyStorePassword();
            if (keyStorePassword != null) {
                factory.setKeystorePassword(keyStorePassword);
            } else {
                factory.setKeystorePassword("");
            }

            try {
                sslConfiguration = factory.createSslConfiguration();
            } catch (FtpServerConfigurationException e) {
                e.printStackTrace();
            }
        }

        SftpSFtplet sftpSFtplet = new SftpSFtplet(this.client, this.userRepository, this.keyRepository, this.logRepository, masterAead);
        UserManager userManager = new UserManager(this.passwordEncryptor, this.userRepository, this.keyRepository, this.configuration, this.masterAead);

        List<String> logs = new ArrayList<>();
        boolean hasFtpPort = false;
        boolean hasFtpsport = false;

        if (ftpsPort != -1 && ftpsPort != 0) {
            boolean ssl = false;
            ListenerFactory listenerFactory = new ListenerFactory();
            listenerFactory.setPort(ftpsPort);
            if (sslConfiguration != null) {
                listenerFactory.setSslConfiguration(sslConfiguration);
                listenerFactory.setImplicitSsl(true);
                ssl = true;
            }
            if (ssl) {
                String ftpsDataPort = this.configuration.getFtpsDataPort();
                DataConnectionConfigurationFactory factory = new DataConnectionConfigurationFactory();
                factory.setPassivePorts(ftpsDataPort);
                factory.setSslConfiguration(sslConfiguration);
                factory.setImplicitSsl(true);
                listenerFactory.setDataConnectionConfiguration(factory.createDataConnectionConfiguration());
                FtpServerFactory serverFactory = new FtpServerFactory();
                serverFactory.addListener("default", listenerFactory.createListener());
                serverFactory.getFtplets().put("default", sftpSFtplet);
                serverFactory.setUserManager(userManager);
                serverFactory.setFileSystem(new SftpSNativeFileSystemFactory());
                this.ftpsserver = serverFactory.createServer();
                this.ftpsserver.start();
                logs.add("          ftps port [" + ftpsPort + "] [Implicit SSL]");
                logs.add("     data ftps port [" + ftpsDataPort + "]");
                hasFtpsport = true;
            }
        }

        int ftpPort = this.configuration.getFtpPort();
        if (ftpPort != -1 && ftpPort != 0) {
            String ftpDataPort = this.configuration.getFtpDataPort();
            DataConnectionConfigurationFactory factory = new DataConnectionConfigurationFactory();
            factory.setPassivePorts(ftpDataPort);
            ListenerFactory listenerFactory = new ListenerFactory();
            listenerFactory.setPort(ftpPort);
            listenerFactory.setDataConnectionConfiguration(factory.createDataConnectionConfiguration());

            FtpServerFactory serverFactory = new FtpServerFactory();

            serverFactory.addListener("default", listenerFactory.createListener());
            serverFactory.getFtplets().put("default", sftpSFtplet);
            serverFactory.setUserManager(userManager);
            serverFactory.setFileSystem(new SftpSNativeFileSystemFactory());
            this.ftpserver = serverFactory.createServer();
            this.ftpserver.start();
            logs.add("           ftp port [" + ftpPort + "]");
            logs.add("      data ftp port [" + ftpDataPort + "]");
            hasFtpPort = true;
        }

        int sftpPort = this.configuration.getSftpPort();
        if (sftpPort != -1 && sftpPort != 0) {
            sshd = SshServer.setUpDefaultServer();
            sshd.setFileSystemFactory(new SftpSVirtualFileSystemFactory(this.configuration, this.userRepository));

            File key = new File(FileUtils.getTempDirectory(), BootApplication.class.getSimpleName() + ".key");
            SimpleGeneratorHostKeyProvider provider = new SimpleGeneratorHostKeyProvider(FileSystems.getDefault().getPath(key.getAbsolutePath()));
            provider.setKeySize(2048);
            provider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
            sshd.setKeyPairProvider(provider);

            sshd.setPasswordAuthenticator(userManager);
            sshd.setPublickeyAuthenticator(userManager);

            SftpSubsystemFactory sftp = new SftpSubsystemFactory.Builder()
                    .build();
            sshd.setSubsystemFactories(Collections.singletonList(sftp));
            sftp.addSftpEventListener(new SftpSEventListenerAdapter(this.logRepository, this.client, this.userRepository, masterAead));

            /**
             * only for SCP, but it was not working
             */
            ScpCommandFactory commandFactory = new ScpCommandFactory.Builder()
                    .withDelegateShellFactory(new InteractiveProcessShellFactory())
                    // .withFileOpener(new ScpFileOpener())
                    .build();
            sshd.setCommandFactory(commandFactory);

            sshd.setPort(sftpPort);
            sshd.start();
            logs.add("          sftp port [" + sftpPort + "]");
        }
        for (String log : logs) {
            LOGGER.info(log);
        }

        return new SftpS();
    }

    @Override
    protected void destroyInstance(SftpS sftps) throws Exception {
        if (this.ftpserver != null) {
            this.ftpserver.stop();
        }
        if (this.ftpsserver != null) {
            this.ftpsserver.stop();
        }
        if (this.sshd != null) {
            this.sshd.stop(true);
        }
    }

}
