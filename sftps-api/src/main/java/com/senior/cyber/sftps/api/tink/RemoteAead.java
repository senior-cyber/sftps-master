package com.senior.cyber.sftps.api.tink;

import com.google.crypto.tink.Aead;
import com.senior.cyber.sftps.x509.PublicKeyUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

public class RemoteAead implements Aead {

    private final String serviceUrl;

    private final String clientSecret;

    private final HttpClient client;

    private final PublicKey serverPublicKey;

    private final KeyPair clientKey;

    private final Crypto crypto;

    public RemoteAead(Crypto crypto, HttpClient client, PublicKey serverPublicKey, String serviceUrl, String clientSecret) throws NoSuchAlgorithmException, NoSuchProviderException {
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
        var builder = HttpRequest.newBuilder(URI.create(this.serviceUrl + "/encrypt"));
        builder.setHeader("Client-Secret", this.clientSecret);
        builder.setHeader("Public-Key", clientPublicKey);
        builder.POST(HttpRequest.BodyPublishers.ofByteArray(this.crypto.encrypt(secret, Base64.getEncoder().encodeToString(plaintext)).getBytes(StandardCharsets.UTF_8)));
        var request = builder.build();

        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return Base64.getDecoder().decode(this.crypto.decrypt(secret, response.body()));
            } else {
                throw new GeneralSecurityException("encryption error");
            }

        } catch (IOException | InterruptedException e) {
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

        var builder = HttpRequest.newBuilder(URI.create(this.serviceUrl + "/decrypt"));
        builder.setHeader("Client-Secret", this.clientSecret);
        builder.setHeader("Public-Key", clientPublicKey);
        builder.POST(HttpRequest.BodyPublishers.ofByteArray(this.crypto.encrypt(secret, Base64.getEncoder().encodeToString(ciphertext)).getBytes(StandardCharsets.UTF_8)));
        var request = builder.build();

        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return Base64.getDecoder().decode(this.crypto.decrypt(secret, response.body()));
            } else {
                throw new GeneralSecurityException("encryption error");
            }
        } catch (IOException | InterruptedException e) {
            throw new GeneralSecurityException(e);
        }
    }

}
