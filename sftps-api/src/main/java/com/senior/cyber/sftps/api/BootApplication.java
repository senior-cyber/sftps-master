package com.senior.cyber.sftps.api;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.KmsClients;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.KmsAeadKeyManager;
import com.google.crypto.tink.streamingaead.StreamingAeadConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.senior.cyber.sftps.api.configuration.MasterAeadConfiguration;
import com.senior.cyber.sftps.api.factory.SftpSFactory;
import com.senior.cyber.sftps.api.tink.Crypto;
import com.senior.cyber.sftps.api.tink.MasterAead;
import com.senior.cyber.sftps.api.tink.PkiKeyClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.security.GeneralSecurityException;
import java.security.Security;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class})
@EntityScan("com.senior.cyber.sftps.dao.entity")
public class BootApplication {

    static {
        if (Security.getProperty(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static void main(String[] args) throws Exception {
        AeadConfig.register();
        StreamingAeadConfig.register();
        SpringApplication.run(BootApplication.class, args);
    }

    @Bean
    public Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
        return builder.create();
    }

    @Bean
    public PasswordEncryptor createPasswordEncryptor() {
        return new StrongPasswordEncryptor();
    }

    @Bean
    public SftpSFactory createSftpS() {
        return new SftpSFactory();
    }

    @Bean(destroyMethod = "close")
    public PkiKeyClient createPkiKeyClient(Crypto crypto, MasterAeadConfiguration configuration) {
        PkiKeyClient client = new PkiKeyClient(crypto, configuration.getAddress(), configuration.getClientSecret());
        KmsClients.add(client);
        return client;
    }

    @Bean
    public MasterAead createMasterAead(PkiKeyClient client, MasterAeadConfiguration configuration) throws GeneralSecurityException {
        KeyTemplate keyTemplate = KmsAeadKeyManager.createKeyTemplate(configuration.getUri());
        KeysetHandle handle = KeysetHandle.generateNew(keyTemplate);
        return new MasterAead(handle.getPrimitive(Aead.class));
    }

    @Bean
    public Crypto createCrypto() {
        return new Crypto();
    }

    @Bean(destroyMethod = "close")
    public CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

}
