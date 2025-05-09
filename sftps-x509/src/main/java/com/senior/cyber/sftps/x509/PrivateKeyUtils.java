package com.senior.cyber.sftps.x509;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

public class PrivateKeyUtils {

    public static PrivateKey read(String pem) throws IOException {
        try (PEMParser parser = new PEMParser(new StringReader(pem))) {
            Object objectHolder = parser.readObject();
            if (objectHolder instanceof PEMKeyPair) {
                PEMKeyPair holder = (PEMKeyPair) objectHolder;
                return new JcaPEMKeyConverter().getPrivateKey(holder.getPrivateKeyInfo());
            } else if (objectHolder instanceof PrivateKeyInfo) {
                PrivateKeyInfo holder = (PrivateKeyInfo) objectHolder;
                return new JcaPEMKeyConverter().getPrivateKey(holder);
            } else {
                return null;
            }
        }
    }

    public static PrivateKey read(File pem) throws IOException {
        return read(FileUtils.readFileToString(pem, StandardCharsets.UTF_8));
    }

    public static String write(PrivateKey privateKey) throws IOException {
        StringWriter pem = new StringWriter();
        try (JcaPEMWriter writer = new JcaPEMWriter(pem)) {
            writer.writeObject(new JcaPKCS8Generator(privateKey, null));
        }
        return pem.toString();
    }

}