package com.senior.cyber.sftps.web.tink;

import com.senior.cyber.frmk.common.pki.PublicKeyUtils;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KmsClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

public class PkiKeyClient implements KmsClient, Closeable {

    public static final String URI = "pki-kms://";

    private final String base;

    private final String clientSecret;

    private final CloseableHttpClient client = HttpClientBuilder.create().build();

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
        HttpUriRequest request = RequestBuilder.get(this.base + "/info").build();
        try (CloseableHttpResponse response = this.client.execute(request)) {
            serverPublicKey = PublicKeyUtils.read(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
        } catch (IOException e) {
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
