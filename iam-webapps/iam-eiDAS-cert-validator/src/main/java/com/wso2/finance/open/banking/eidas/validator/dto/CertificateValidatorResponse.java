package com.wso2.finance.open.banking.eidas.validator.dto;

/**
 * Certificate validator response DTO.
 */
public class CertificateValidatorResponse {

    private String certificateContent;
    private boolean isCertificateRevoked = true;
    private boolean isIssuerTrusted = false;

    public String getCertificateContent() {

        return certificateContent;
    }

    public void setCertificateContent(String certificateContent) {

        this.certificateContent = certificateContent;
    }

    public boolean getIsCertificateRevoked() {

        return isCertificateRevoked;
    }

    public void setIsCertificateRevoked(boolean isCertificateRevoked) {

        this.isCertificateRevoked = isCertificateRevoked;
    }

    public boolean getIsIssuerTrusted() {

        return isIssuerTrusted;
    }

    public void setIsIssuerTrusted(boolean isIssuerTrusted) {

        this.isIssuerTrusted = isIssuerTrusted;
    }

    @Override
    public String toString() {

        return "{" +
                "\"isCertificateRevoked\": \"" + isCertificateRevoked + "\"" +
                ", \"isIssuerTrusted:\": \"" + isIssuerTrusted + "\"" +
                ", \"certificateContent\": " + certificateContent +
                "}";
    }
}
