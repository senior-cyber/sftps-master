package com.senior.cyber.sftps.web.tink;

import com.senior.cyber.frmk.common.pki.PublicKeyUtils;
import com.google.crypto.tink.Aead;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

public class RemoteAead implements Aead {

    private final String serviceUrl;

    private final String clientSecret;

    private final CloseableHttpClient client;

    private final PublicKey serverPublicKey;

    private final KeyPair clientKey;

    private final Crypto crypto;

    public RemoteAead(Crypto crypto, CloseableHttpClient client, PublicKey serverPublicKey, String serviceUrl, String clientSecret) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.client = client;
        this.serverPublicKey = serverPublicKey;
        this.serviceUrl = serviceUrl;
        this.clientSecret = clientSecret;
        this.crypto = crypto;
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        generator.initialize(256);
        this.clientKey = generator.generateKeyPair();
    }

    @Override
    public byte[] encrypt(byte[] plaintext, byte[] associatedData) throws GeneralSecurityException {
        String clientPublicKey = null;
        try {
            clientPublicKey = this.crypto.encrypt(this.serverPublicKey, PublicKeyUtils.write(this.clientKey.getPublic()));
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
        SecretKey secret = this.crypto.lookupKeyAgreement((ECPrivateKey) this.clientKey.getPrivate(), (ECPublicKey) this.serverPublicKey);
        HttpUriRequest request = RequestBuilder.post().setUri(this.serviceUrl + "/encrypt")
                .setHeader("Client-Secret", this.clientSecret)
                .setHeader("Public-Key", clientPublicKey)
                .setEntity(new ByteArrayEntity(this.crypto.encrypt(secret, Base64.getEncoder().encodeToString(plaintext)).getBytes(StandardCharsets.UTF_8)))
                .build();
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                return Base64.getDecoder().decode(this.crypto.decrypt(secret, EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)));
            } else {
                throw new GeneralSecurityException("encryption error");
            }
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] associatedData) throws GeneralSecurityException {
        String clientPublicKey = null;
        try {
            clientPublicKey = this.crypto.encrypt(this.serverPublicKey, PublicKeyUtils.write(this.clientKey.getPublic()));
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
        SecretKey secret = this.crypto.lookupKeyAgreement((ECPrivateKey) this.clientKey.getPrivate(), (ECPublicKey) this.serverPublicKey);

        HttpUriRequest request = RequestBuilder.post(this.serviceUrl + "/decrypt")
                .setHeader("Client-Secret", this.clientSecret)
                .setHeader("Public-Key", clientPublicKey)
                .setEntity(new ByteArrayEntity(this.crypto.encrypt(secret, Base64.getEncoder().encodeToString(ciphertext)).getBytes(StandardCharsets.UTF_8)))
                .build();
        try (CloseableHttpResponse response = client.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                return Base64.getDecoder().decode(this.crypto.decrypt(secret, EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)));
            } else {
                throw new GeneralSecurityException("encryption error");
            }
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
    }

}
