package com.senior.cyber.sftps.api;

import com.google.crypto.tink.config.TinkConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.senior.cyber.sftps.api.factory.SftpSFactory;
import com.senior.cyber.sftps.api.tink.Crypto;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.http.HttpClient;
import java.security.Security;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class})
@EntityScan("com.senior.cyber.sftps.dao.entity")
@EnableJpaRepositories(
        basePackages = "com.senior.cyber.sftps.dao.repository"
)
public class BootApplication implements CommandLineRunner, ApplicationContextAware {

    private ApplicationContext applicationContext;

    static {
        if (Security.getProperty(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static void main(String[] args) throws Exception {
        TinkConfig.register();
        SpringApplication.run(BootApplication.class, args);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
//        PasswordEncryptor passwordEncryptor = this.applicationContext.getBean(PasswordEncryptor.class);
//        UserRepository userRepository = this.applicationContext.getBean(UserRepository.class);
//        User user = new User();
//        user.setDisplayName("Socheat KHAUV");
//        user.setHomeDirectory("C:/Users/User/Downloads");
//        user.setEnabled(true);
//        user.setAdmin(false);
//        user.setEncryptAtRest(false);
//        user.setWebhookEnabled(false);
//        user.setLastSeen(new Date());
//        user.setEmailAddress("k.socheat@dgc.gov.kh");
//        user.setLogin("k.socheat");
//        user.setPassword(passwordEncryptor.encryptPassword("123123a"));
//        userRepository.save(user);
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

    @Bean
    public Crypto createCrypto() {
        return new Crypto();
    }

    @Bean(destroyMethod = "close")
    public HttpClient createHttpClient() {
        return HttpClient.newBuilder().build();
    }

}
