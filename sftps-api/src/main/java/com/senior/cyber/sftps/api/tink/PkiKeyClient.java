package com.senior.cyber.sftps.api.tink;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KmsClient;
import com.senior.cyber.sftps.x509.PublicKeyUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

public class PkiKeyClient implements KmsClient, Closeable {

    public static final String URI = "pki-kms://";

    private final String base;

    private final String clientSecret;

    private final HttpClient client = HttpClient.newHttpClient();

    private final Crypto crypto;

    public PkiKeyClient(Crypto crypto, String base, String clientSecret) {
        this.crypto = crypto;
        this.base = base;
        this.clientSecret = clientSecret;
    }

    @Override
    public boolean doesSupport(String keyUri) {
        return keyUri.startsWith(URI);
    }

    @Override
    public KmsClient withCredentials(String credentialPath) throws GeneralSecurityException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public KmsClient withDefaultCredentials() throws GeneralSecurityException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Aead getAead(String keyUri) throws GeneralSecurityException {

        PublicKey serverPublicKey = null;
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(java.net.URI.create(this.base + "/info"))
                .build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            serverPublicKey = PublicKeyUtils.read(response.body());
        } catch (IOException | InterruptedException e) {
            throw new GeneralSecurityException("server public key is required");
        }

        String clientId = keyUri.substring(URI.length());
        String serviceUrl = this.base + "/" + clientId;
        return new RemoteAead(this.crypto, this.client, serverPublicKey, serviceUrl, this.clientSecret);
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }

}
