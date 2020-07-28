package com.alrayan.wso2.webapp.management.client.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlElement;

/**
 * Al Rayan User active status response.
 *
 * @since 1.0.0
 */
@JsonRootName(value = "status")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String errorCode;
    private String errorDescription;

    /**
     * Returns the salesforce ID of the user.
     *
     * @return salesforce
     */
    @XmlElement()
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the salesforce Id.
     *
     * @param errorCode errorCode
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Returns the salesforce ID of the user.
     *
     * @return salesforce
     */
    @XmlElement()
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Sets the salesforce Id.
     *
     * @param errorDescription errorCode
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }


    /**
     * User response object builder.
     *
     * @since 1.0.0
     */
    public static class ErrorResponseBuilder {

        private String errorCode;
        private String errorDescription;


        /**
         * Sets the salesforce Id for the user status builder.
         *
         * @param errorCode salesforce Id
         * @return this {@link ErrorResponseBuilder} instance
         */
        public ErrorResponseBuilder setErrorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponseBuilder setErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
            return this;
        }


        /**
         * Builds and returns an instance of {@link ErrorResponse}.
         *
         * @return built {@link ErrorResponse} instance
         */
        public ErrorResponse build() {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorCode(errorCode);
            errorResponse.setErrorDescription(errorDescription);
            return errorResponse;
        }
    }
}
