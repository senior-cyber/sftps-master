package com.senior.cyber.sftps.api.tink;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.senior.cyber.sftps.api.dto.SftpSUser;
import com.senior.cyber.sftps.dao.entity.rbac.User;
import com.senior.cyber.sftps.dao.entity.sftps.Key;
import com.senior.cyber.sftps.dao.entity.sftps.Log;
import com.senior.cyber.sftps.dao.repository.rbac.UserRepository;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public static void report(HttpClient client, UserRepository userRepository, MasterAead masterAead, Log log, User user, String keyId, String keyName) {
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
                    throw new RuntimeException(e);
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

            var requestBuilder = HttpRequest.newBuilder();
            if (signature == null || "".equals(signature)) {
                LOGGER.info("X-SftpS-Event [{}]", log.getEventType());
                requestBuilder.header("X-SftpS-Event", log.getEventType().name());
            } else {
                LOGGER.info("X-SftpS-Event [{}] X-SftpS-Signature [{}]", log.getEventType(), signature);
                requestBuilder.header("X-SftpS-Event", log.getEventType().name());
                requestBuilder.header("X-SftpS-Signature", signature);
            }
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            requestBuilder.uri(URI.create(user.getWebhookUrl()));
            var request = requestBuilder.build();
            try {
                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    public static void report(HttpClient client, UserRepository userRepository, MasterAead masterAead, Log log, User user, Key key) {
        if (key == null) {
            report(client, userRepository, masterAead, log, user, null, null);
        } else {
            report(client, userRepository, masterAead, log, user, key.getId(), key.getName());
        }
    }

    public static void report(HttpClient client, UserRepository userRepository, MasterAead masterAead, Log log, SftpSUser sftpsUser) {
        Optional<User> optionalUser = userRepository.findById(sftpsUser.getUserId());
        User user = optionalUser.orElseThrow();
        report(client, userRepository, masterAead, log, user, sftpsUser.getKeyId() == null || "".equals(sftpsUser.getKeyId()) ? null : sftpsUser.getKeyId(), sftpsUser.getKeyName());
    }

}
