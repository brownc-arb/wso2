package com.wso2.finance.open.banking.eidas.validator.dto;

/**
 * Certificate validator error response DTO.
 */
public class CertificateValidatorErrorResponse {

    private int errorCode;
    private String errorMessage;

    public int getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(int errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {

        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {

        return "{" +
                "\"errorCode\": \"" + errorCode + "\"" +
                ", \"errorMessage\": \"" + errorMessage + "\"" +
                "}";
    }
}
