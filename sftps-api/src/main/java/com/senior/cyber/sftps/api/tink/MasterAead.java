package com.senior.cyber.sftps.api.tink;

import com.google.crypto.tink.Aead;

import java.security.GeneralSecurityException;

public class MasterAead implements Aead {

    private final Aead delegate;

    public MasterAead(Aead delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte[] encrypt(byte[] plaintext, byte[] associatedData) throws GeneralSecurityException {
        return delegate.encrypt(plaintext, associatedData);
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] associatedData) throws GeneralSecurityException {
        return delegate.decrypt(ciphertext, associatedData);
    }

}
