package com.senior.cyber.sftps.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DefaultBufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;

public class AESGCMBlockCipher extends DefaultBufferedBlockCipher {

    private GCMBlockCipher internalCipher;

    public AESGCMBlockCipher() {
        this.internalCipher = new GCMBlockCipher(new AESEngine());
    }


    @Override
    public void init(boolean forEncryption, CipherParameters params) {
        internalCipher.init(forEncryption, params);
    }


    @Override
    public int getOutputSize(int len) {
        return internalCipher.getOutputSize(len);
    }


    @Override
    public int doFinal(byte[] out, int outOff) throws InvalidCipherTextException {
        return internalCipher.doFinal(out, outOff);
    }

    @Override
    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) {
        return internalCipher.processBytes(in, inOff, len, out, outOff);
    }

}