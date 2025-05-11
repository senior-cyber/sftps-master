package com.senior.cyber.sftps.api.factory;

import com.senior.cyber.sftps.api.BootApplication;
import com.senior.cyber.sftps.api.UserManager;
import com.senior.cyber.sftps.api.configuration.AppConfig;
import com.senior.cyber.sftps.api.ftp.SftpSFtplet;
import com.senior.cyber.sftps.api.ftp.SftpSNativeFileSystemFactory;
import com.senior.cyber.sftps.api.scp.ScpFileOpener;
import com.senior.cyber.sftps.api.scp.SftpSEventListenerAdapter;
import com.senior.cyber.sftps.api.scp.SftpSSubsystemFactory;
import com.senior.cyber.sftps.api.scp.SftpSVirtualFileSystemFactory;
import com.senior.cyber.sftps.dao.repository.rbac.UserRepository;
import com.senior.cyber.sftps.dao.repository.sftps.KeyRepository;
import com.senior.cyber.sftps.dao.repository.sftps.LogRepository;
import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.io.File;
import java.net.http.HttpClient;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SftpSFactory extends AbstractFactoryBean<SftpS> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpSFactory.class);

    private FtpServer ftpserver = null;

    private FtpServer ftpsserver = null;

    private SshServer sshd = null;

    @Autowired
    protected AppConfig appConfig;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected KeyRepository keyRepository;

    @Autowired
    protected PasswordEncryptor passwordEncryptor;

    @Autowired
    protected LogRepository logRepository;

    @Autowired
    protected HttpClient client;

    @Override
    public Class<?> getObjectType() {
        return SftpS.class;
    }

    @Override
    protected SftpS createInstance() throws Exception {

        int ftpsPort = this.appConfig.getFtpsPort();

        SslConfiguration sslConfiguration = null;
        if (ftpsPort != -1 && ftpsPort != 0) {
            SslConfigurationFactory factory = new SslConfigurationFactory();
            File trustStore = this.appConfig.getTrustStore();
            factory.setTruststoreType(this.appConfig.getTrustStoreType());
            if (trustStore != null) {
                factory.setTruststoreFile(trustStore);
            }
            String truststorePassword = this.appConfig.getTrustStorePassword();
            if (truststorePassword != null) {
                factory.setTruststorePassword(truststorePassword);
            } else {
                factory.setTruststorePassword("");
            }

            String clientAuthentication = this.appConfig.getClientAuth();
            if ("optional".equalsIgnoreCase(clientAuthentication)) {
                factory.setClientAuthentication(ClientAuth.WANT.name());
            } else if ("yes".equalsIgnoreCase(clientAuthentication) || "true".equalsIgnoreCase(clientAuthentication)) {
                factory.setClientAuthentication(ClientAuth.NEED.name());
            } else if ("no".equalsIgnoreCase(clientAuthentication) || "false".equalsIgnoreCase(clientAuthentication)) {
                factory.setClientAuthentication(ClientAuth.NONE.name());
            }

            String keyAlias = this.appConfig.getKeyAlias();
            if (keyAlias != null && !"".equals(keyAlias)) {
                factory.setKeyAlias(keyAlias);
            }

            String keyPassword = this.appConfig.getKeyPassword();
            if (keyPassword != null) {
                factory.setKeyPassword(keyPassword);
            } else {
                factory.setKeyPassword("");
            }

            File keyStore = this.appConfig.getKeyStore();
            factory.setKeystoreType(this.appConfig.getKeyStoreType());
            if (keyStore != null) {
                factory.setKeystoreFile(keyStore);
            }

            String keyStorePassword = this.appConfig.getKeyStorePassword();
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

        SftpSFtplet sftpSFtplet = new SftpSFtplet(this.client, this.userRepository, this.keyRepository, this.logRepository);
        UserManager userManager = new UserManager(this.passwordEncryptor, this.userRepository, this.keyRepository, this.appConfig);

        List<String> logs = new ArrayList<>();

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
                String ftpsDataPort = this.appConfig.getFtpsDataPort();
                DataConnectionConfigurationFactory factory = new DataConnectionConfigurationFactory();
                factory.setPassivePorts(ftpsDataPort);
                if (this.appConfig.getPassiveAddress() != null && !"".equals(this.appConfig.getPassiveAddress())) {
                    factory.setPassiveAddress(this.appConfig.getPassiveAddress());
                }
                if (this.appConfig.getPassiveExternalAddress() != null && !"".equals(this.appConfig.getPassiveExternalAddress())) {
                    factory.setPassiveExternalAddress(this.appConfig.getPassiveExternalAddress());
                }
                factory.setPassiveIpCheck(true);
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
            }
        }

        int ftpPort = this.appConfig.getFtpPort();
        if (ftpPort != -1 && ftpPort != 0) {
            String ftpDataPort = this.appConfig.getFtpDataPort();
            DataConnectionConfigurationFactory factory = new DataConnectionConfigurationFactory();
            if (this.appConfig.getPassiveAddress() != null && !this.appConfig.getPassiveAddress().isEmpty()) {
                factory.setPassiveAddress(this.appConfig.getPassiveAddress());
            }
            if (this.appConfig.getPassiveExternalAddress() != null && !this.appConfig.getPassiveExternalAddress().isEmpty()) {
                factory.setPassiveExternalAddress(this.appConfig.getPassiveExternalAddress());
            }
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
        }

        int sftpPort = this.appConfig.getSftpPort();
        if (sftpPort != -1 && sftpPort != 0) {
            sshd = SshServer.setUpDefaultServer();
            sshd.setFileSystemFactory(new SftpSVirtualFileSystemFactory(this.appConfig, this.userRepository));

            File key = new File(FileUtils.getTempDirectory(), BootApplication.class.getSimpleName() + ".key");
            SimpleGeneratorHostKeyProvider provider = new SimpleGeneratorHostKeyProvider(FileSystems.getDefault().getPath(key.getAbsolutePath()));
            provider.setKeySize(2048);
            provider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
            sshd.setKeyPairProvider(provider);

            sshd.setPasswordAuthenticator(userManager);
            sshd.setPublickeyAuthenticator(userManager);

            SftpSSubsystemFactory sftp = new SftpSSubsystemFactory();
            sshd.setSubsystemFactories(Collections.singletonList(sftp));
            sftp.addSftpEventListener(new SftpSEventListenerAdapter(this.logRepository, this.client, this.userRepository));

            /**
             * only for SCP, but it was not working
             */
            ScpCommandFactory commandFactory = new ScpCommandFactory();
            commandFactory.setScpFileOpener(new ScpFileOpener());
            sshd.setCommandFactory(commandFactory);
            sshd.setShellFactory(new InteractiveProcessShellFactory());

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
