package com.senior.cyber.sftps.web;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SecretUtils {

    public static byte[] buildOriginToFake(String secret) {
        byte[] fake = Base64.getDecoder().decode(secret);
        byte[] origin = new byte[256];
        int index = 0;
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            origin[index] = (byte) i;
            index++;
        }
        byte[] originToFake = new byte[origin.length];
        for (int i = 0; i < origin.length; i++) {
            byte o = origin[i];
            byte f = fake[i];
            originToFake[index(o)] = f;
        }
        return originToFake;
    }

    public static byte[] buildFakeToOrigin(String secret) {
        byte[] fake = Base64.getDecoder().decode(secret);
        byte[] origin = new byte[256];
        int index = 0;
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            origin[index] = (byte) i;
            index++;
        }
        byte[] fakeToOrigin = new byte[origin.length];
        for (int i = 0; i < origin.length; i++) {
            byte o = origin[i];
            byte f = fake[i];
            fakeToOrigin[index(f)] = o;
        }
        return fakeToOrigin;
    }

    public static byte translate(byte[] dictionary, byte value) {
        return dictionary[index(value)];
    }

    public static byte[] translate(byte[] dictionary, byte[] values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = dictionary[index(values[i])];
        }
        return result;
    }

    private static int index(int value) {
        return java.lang.Byte.MAX_VALUE + 1 + value;
    }

    public static String generateSecret() {
        List<Byte> tempSecret = new ArrayList<>(256);
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            tempSecret.add((byte) i);
        }
        byte[] secret = new byte[256];
        for (int i = 0; i < secret.length; i++) {
            secret[i] = tempSecret.remove(RandomUtils.nextInt(0, tempSecret.size()));
        }
        return Base64.getEncoder().withoutPadding().encodeToString(secret);
    }

}
