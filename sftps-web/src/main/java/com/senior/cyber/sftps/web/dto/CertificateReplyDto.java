package com.senior.cyber.sftps.web.dto;

import com.senior.cyber.sftps.web.gson.X509CertificateTypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.security.cert.X509Certificate;

public class CertificateReplyDto {

    @Expose
    @SerializedName("certificate")
    @JsonAdapter(X509CertificateTypeAdapter.class)
    private X509Certificate certificate;

    public CertificateReplyDto() {
    }

    public CertificateReplyDto(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

}
