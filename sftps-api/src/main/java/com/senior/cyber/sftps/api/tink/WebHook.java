package com.senior.cyber.sftps.api.tink;

import com.senior.cyber.sftps.api.dto.SftpSUser;
import com.senior.cyber.sftps.api.repository.UserRepository;
import com.senior.cyber.sftps.dao.entity.Key;
import com.senior.cyber.sftps.dao.entity.Log;
import com.senior.cyber.sftps.dao.entity.User;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.wicket.WicketRuntimeException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebHook.class);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void report(CloseableHttpClient client, UserRepository userRepository, MasterAead masterAead, Log log, User user, Long keyId, String keyName) {
        if (user.isWebhookEnabled() && user.getWebhookUrl() != null && !"".equals(user.getWebhookUrl())) {
            Map<String, Object> gson = new HashMap<>();
            gson.put("when", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(log.getCreatedAt()));
            gson.put("userId", user.getId());
            gson.put("userDisplayName", user.getDisplayName());
            if (keyId != null) {
                gson.put("keyId", keyId);
                gson.put("keyName", keyName);
            }
            gson.put("srcPath", log.getSrcPath());
            if (log.getDstPath() != null && !"".equals(log.getDstPath())) {
                gson.put("dstPath", log.getDstPath());
            }
            if (log.getSize() != null) {
                gson.put("size", log.getSize());
            }

            String json = GSON.toJson(gson);

            Aead aeadDek = null;
            try {
                aeadDek = KeysetHandle.read(JsonKeysetReader.withString(user.getDek()), masterAead).getPrimitive(Aead.class);
            } catch (IOException | GeneralSecurityException e) {
            }

            String secret_value = null;
            if (aeadDek != null && user.getWebhookSecret() != null) {
                try {
                    byte[] secret = aeadDek.decrypt(Base64.getDecoder().decode(user.getWebhookSecret()), "".getBytes(StandardCharsets.UTF_8));
                    secret_value = new String(secret, StandardCharsets.UTF_8);
                } catch (GeneralSecurityException e) {
                    throw new WicketRuntimeException(e);
                }
            } else {
                secret_value = user.getWebhookSecret();
            }

            SecretKey secret = null;
            if (secret_value != null) {
                try {
                    SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secret_value), "AES");
                    SecretKeyFactory factory = SecretKeyFactory.getInstance("AES", BouncyCastleProvider.PROVIDER_NAME);
                    secret = factory.generateSecret(secretKeySpec);
                } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchProviderException e) {
                }
            }

            String signature = null;
            if (secret != null) {
                try {
                    Mac hmac = Mac.getInstance("HmacSHA256", BouncyCastleProvider.PROVIDER_NAME);
                    hmac.init(secret);
                    signature = Base64.getEncoder().encodeToString(hmac.doFinal(json.getBytes(StandardCharsets.UTF_8)));
                } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
                }
            }

            StringEntity entity = new StringEntity(json, ContentType.create("application/json", StandardCharsets.UTF_8));
            HttpUriRequest request = null;
            if (signature == null || "".equals(signature)) {
                LOGGER.info("X-SftpS-Event [{}]", log.getEventType());
                request = RequestBuilder.post(user.getWebhookUrl())
                        .setHeader("X-SftpS-Event", log.getEventType())
                        .setEntity(entity).build();
            } else {
                LOGGER.info("X-SftpS-Event [{}] X-SftpS-Signature [{}]", log.getEventType(), signature);
                request = RequestBuilder.post(user.getWebhookUrl())
                        .setHeader("X-SftpS-Event", log.getEventType())
                        .setHeader("X-SftpS-Signature", signature)
                        .setEntity(entity).build();
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                EntityUtils.consumeQuietly(response.getEntity());
            } catch (IOException e) {
            }
        }
    }

    public static void report(CloseableHttpClient client, UserRepository userRepository, MasterAead masterAead, Log log, User user, Key key) {
        if (key == null) {
            report(client, userRepository, masterAead, log, user, null, null);
        } else {
            report(client, userRepository, masterAead, log, user, key.getId(), key.getName());
        }
    }

    public static void report(CloseableHttpClient client, UserRepository userRepository, MasterAead masterAead, Log log, SftpSUser sftpsUser) {
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(sftpsUser.getUserId()));
        User user = optionalUser.orElseThrow();
        report(client, userRepository, masterAead, log, user, sftpsUser.getKeyId() == null || "".equals(sftpsUser.getKeyId()) ? null : Long.parseLong(sftpsUser.getKeyId()), sftpsUser.getKeyName());
    }

}
