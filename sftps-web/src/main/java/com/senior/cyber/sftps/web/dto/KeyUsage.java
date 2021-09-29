package com.senior.cyber.sftps.web.dto;

public enum KeyUsage {

    digitalSignature,
    nonRepudiation,
    keyEncipherment,
    dataEncipherment,
    keyAgreement,
    keyCertSign,
    cRLSign,
    encipherOnly,
    decipherOnly;

}
