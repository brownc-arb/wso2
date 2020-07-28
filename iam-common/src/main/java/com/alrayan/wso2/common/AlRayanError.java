package com.alrayan.wso2.common;

/**
 * Enum defining the error codes for Al Rayan Bank.
 *
 * @since 1.0.0
 */
public enum AlRayanError {

    /**
     * Error while activating Al Rayan User store manager.
     */
    ERROR_WHILE_ACTIVATING_PSU_USER_STORE("ALRB-WSO2-4001", "Error while activating Al Rayan user store manager."),

    /**
     * Error on executing the HTTP request on VASCO server.
     */
    ERROR_HTTP_REQUEST_TO_VASCO("ALRB-WSO2+VASCO-4003", "Error on executing the HTTP request on VASCO server."),

    /**
     * Error on constructing SOAP body content.
     */
    ERROR_CONSTRUCTING_SOAP_BODY_CONTENT("ALRB-WSO2+VASCO-4004", "Error on constructing the SOAP body content."),

    /**
     * Error on constructing SOAP body content.
     */
    ERROR_CONSTRUCTING_SOAP_MESSAGE("ALRB-WSO2+VASCO-4005", "Error on constructing the SOAP message string."),

    /**
     * Error on converting response to SOAP message.
     */
    ERROR_CONVERTING_RESPONSE_TO_SOAP("ALRB-WSO2+VASCO-4007", "Error on converting the response to a SOAP message."),

    /**
     * User does not exists.
     */
    USER_DOES_NOT_EXISTS("ALRB-WSO2-4008", "User does not exists in the system."),

    /**
     * Invalid PIN code position.
     */
    INVALID_PIN_CODE_POSITION("ALRB-WSO2-4009", "Invalid PIN code position."),

    /**
     * Invalid PIN validation request.
     */
    INVALID_PIN_VALIDATION_REQUEST("ALRB-WSO2-4010", "Invalid PIN validation request."),

    /**
     * Acting username not specified.
     */
    ACTING_USERNAME_NOT_SPECIFIED("ALRB-WSO2-4011", "Acting username not specified."),

    /**
     * PIN code not specified.
     */
    PIN_CODE_NOT_SPECIFIED("ALRB-WSO2-4012", "PIN code not specified."),

    /**
     * User with the same acting username is found.
     */
    USER_WITH_SAME_ACTING_USERNAME_FOUND("ALRB-WSO2-4013", "The digital bank username is already taken."),

    /**
     * Acting username claim not specified.
     */
    ACTING_USERNAME_CLAIM_NOT_SPECIFIED("ALRB-WSO2-4014", "The digital bank username is not specified."),

    /**
     * Internal server error on PIN validation.
     */
    PIN_VALIDATION_INTERNAL_SERVER_ERROR("ALRB-WSO2-4015", "Internal server error on PIN validation."),

    /**
     * PIN validation failed on attempting to change the PIN.
     */
    CHANGE_PIN_FAILED_INCORRECT_CREDENTIALS("ALRB-WSO2-4016",
            "Change PIN failed since the digital banking username or the current PIN provided is incorrect or the " +
            "authenticated user is not of the given username."),

    /**
     * Internal server error on PIN change.
     */
    PIN_CHANGE_INTERNAL_SERVER_ERROR("ALRB-WSO2-4017", "Internal server error on PIN change."),

    /**
     * Authorization header is missing.
     */
    AUTHORIZATION_HEADER_MISSING("ALRB-WSO2-4018", "Authorization header is missing."),

    /**
     * Error on encrypting PIN.
     */
    PIN_ENCRYPTION_ERROR("ALRB-WSO2-4019", "PIN code encryption error"),

    /**
     * Error on decrypting PIN.
     */
    PIN_DECRYPTION_ERROR("ALRB-WSO2-4020", "PIN code decryption error"),

    /**
     * Internal server error on modifying user account lock status.
     */
    INTERNAL_SERVER_ERROR_ACCOUNT_LOCK_MODIFY("ALRB-WSO2-4021",
            "Internal server error on modifying user account lock status."),

    /**
     * Internal server error on modifying user account disable status.
     */
    INTERNAL_SERVER_ERROR_ACCOUNT_DISABLE_MODIFY("ALRB-WSO2-4022",
            "Internal server error on modifying user account disable status."),

    /**
     * Account lock status missing in request.
     */
    BAD_REQUEST_ACCOUNT_LOCK_STATUS_NOT_GIVEN("ALRB-WSO2-4023", "Account lock status missing in request."),

    /**
     * Account disable status missing in request.
     */
    BAD_REQUEST_ACCOUNT_DISABLE_STATUS_NOT_GIVEN("ALRB-WSO2-4024", "Account disable status missing in request."),

    /**
     * Account lock status is invalid in request.
     */
    BAD_REQUEST_ACCOUNT_LOCK_STATUS_INVALID("ALRB-WSO2-4025", "Account lock status is invalid in request."),

    /**
     * Account disable status is invalid in request.
     */
    BAD_REQUEST_ACCOUNT_DISABLE_STATUS_INVALID("ALRB-WSO2-4026", "Account disable status is invalid in request."),

    /**
     * Internal server error on PIN validation.
     */
    INTERNAL_SERVER_ERROR_PIN_VALIDATION("ALRB-WSO2-4027", "Internal server error on PIN validation."),

    /**
     * Access token validation failed.
     */
    ACCESS_TOKEN_VALIDATION_FAILED("ALRB-WSO2-4028", "Access token validation failed."),

    /**
     * Error validating access tokens.
     */
    ACCESS_TOKEN_VALIDATION_INTERNAL_SERVER_ERROR("ALRB-WSO2-4029",
            "Internal server error on access token validation."),

    /**
     * Internal server error on invoking functions in User Store.
     */
    INTERNAL_SERVER_ERROR_WITH_USERSTORE("ALRB-WSO2-4030",
            "Internal server error on invoking functions in User Store."),

    /**
     * Salesforce ID not specified.
     */
    SALESFORCE_ID_NOT_SPECIFIED("ALRB-WSO2-4031", "Salesforce ID not specified/received in the response."),

    /**
     * Error password based recovery not enabled.
     */
    ERROR_CODE_NOTIFICATION_BASED_PASSWORD_RECOVERY_NOT_ENABLE("ALRB-WSO2-4031",
            "Notification based password recovery is not enabled"),

    /**
     * User authentication failed because the account is locked.
     */
    USER_ACCOUNT_LOCKED("ALRB-WSO2-4032", "User authentication failed because the account is locked."),

    /**
     * User authentication failed because the account is disabled.
     */
    USER_ACCOUNT_DISABLED("ALRB-WSO2-4033", "User authentication failed because the account is disabled."),

    /**
     * Salesforce ID decryption failure.
     */
    SALESFORCE_ID_DECRYPTION_FAILURE("ALRB-WSO2-4034", "Salesforce ID decryption failure."),

    /**
     * Unknown error while user provisioning.
     */
    UNKNOWN_ERROR_WHILE_USER_REGISTRATION("ALRB-WSO2-4036", "Unknown error while user provisioning."),

    /**
     * Platform validation failure.
     */
    PLATFORM_VALIDATION_FAILED("ALRB-WSO2-4037", "Platform validation failure."),

    /**
     * VASCO authentication URL not defined.
     */
    VASCO_AUTHENTICATION_URL_NOT_DEFINED("ALRB-WSO2+VASCO-4038", "VASCO authentication URL not defined."),

    /**
     * VASCO secure challenge key not found in the response.
     */
    VASCO_SECURE_CHALLENGE_RESPONSE_NOT_FOUND("ALRB-WSO2+VASCO-4039",
            "VASCO secure challenge key not found in the response."),

    /**
     * VASCO auth user challenge key not found in the response.
     */
    VASCO_AUTH_USER_CHALLENGE_RESPONSE_NOT_FOUND("ALRB-WSO2+VASCO-4040",
            "VASCO auth user challenge key not found in the response."),

    /**
     * Consent not approved.
     */
    CONSENT_NOT_APPROVED("ALRB-WSO2-4041", "User consent not approved."),

    /**
     * Error on generating VASCO image.
     */
    ERROR_ON_GENERATING_VASCO_IMAGE("ALRB-WSO2+VASCO-4042", "Error on generating VASCO image."),

    /**
     * VASCO CRONTO validation failed.
     */
    VASCO_CRONTO_CODE_VALIDATION_FAILED("ALRB-WSO2+VASCO-4043", "VASCO cronto code validation failed."),

    /**
     * Error on VASCO auth user request.
     */
    ERROR_ON_VASCO_AUTH_USER_REQUEST("ALRB-WSO2+VASCO-4044", "Error on VASCO auth user request."),

    /**
     * Error on obtaining bank charges data from the bank charges endpoint.
     */
    ERROR_INVOKING_BANK_CHARGES_ENDPOINT("ALRB-WSO2-4045",
            "Error on obtaining bank charges data from the bank charges endpoint."),

    /**
     * Error on mapping bank charges request info.
     */
    ERROR_MAPPING_BANK_CHARGES_REQUEST_INFO("ALRB-WSO2-4046", "Error on mapping bank charges request info."),

    /**
     * Error on mapping bank charges response info.
     */
    ERROR_MAPPING_BANK_CHARGES_RESPONSE_INFO("ALRB-WSO2-4047", "Error on mapping bank charges response info."),

    /**
     *   Passed debtor account does not belong to the user.
     */
    PASSED_DEBTOR_ACCOUNT_DOES_NOT_BELONG_TO_USER("ALRB-WSO2-4048",
            "Passed debtor account does not belong to the user."),

    /**
     * Consent information sent to the client is not the one actually generated in system.
     */
    ERROR_CONSENT_INFORMATIONS_TAMPERED("ALRB-WSO2-4052",
            "Consent details sent for the client is not matching with the consent received"),

    /**
     * Client details to retrieve the consent information is not found.
     */
    ERROR_CLIENT_DETAILS_NOT_FOUND("ALRB-WSO2-4053", "Client Details are not found."),

    /**
     * Consent information is not been added for the application.
     */
    ERROR_CONSENT_INFORMATION_NOT_FOUND("ALRB-WSO2-4054", "Consent information is not found for the application."),

    /**
     * Exception occurred when parsing the consent message. JSON messages has not in the correct format.
     */
    ERROR_CONSENT_PARSING("ALRB-WSO2-4055", "Exception occurred when parsing the consent message."),

    /**
     * Exception occurred when parsing the consent message. JSON messages has not in the correct format.
     */
    ERROR_RETRIEVING_CONSENT_DETAILS("ALRB-WSO2-4056", "Error while retrieving account consent data."),

    /**
     * General message for the consent validation failed.
     */
    ERROR_CONSENT_VALIDATION_FAILED("ALRB-WSO2-4057", "Consent validation failed."),

    /**
     * Error on updating the application with the SCA configurations.
     */
    ERROR_UPDATE_APPLICATION("ALRB-APIM-WSO2-4058", "Error occurred while updating application."),

    /**
     * Error while making remote call when creating the application.
     */
    ERROR_MAKING_REMOTE_CALL_ON_APP_CREATION("ALRB-APIM-WSO2-4059", "Error occurred while making remote call."),

    /**
     * Error on reading adaptive authentication script.
     */
    ERROR_READING_ADAPTIVE_AUTH_SCRIPT("ALRB-APIM-WSO2-4060", "Error on reading adaptive authentication script."),

    /**
     * Axis fault while making remote call.
     */
    AXIS_FAULT_WHEN_INVOKING_REMOTE_CALL("ALRB-APIM-WSO2-4061", "Axis fault while making remote call."),

    /**
     * Error on authenticating the user to create application.
     */
    ERROR_AUTHENTICATING_USER("ALRB-APIM-WSO2-4062", "Error occurred while authenticating user."),

    /**
     * Error occurred while adding/removing application role.
     */
    ERROR_ON_ADD_REMOVE_APPLICATION_ROLE("ALRB-APIM-WSO2-4063",
            "Error occurred while adding/removing application role."),

    /**
     * Application certificate content is empty.
     */
    APPLICATION_CERT_CONTENT_IS_EMPTY("ALRB-APIM-WSO2-4064",
            "Invalid application certificate, certificate content cannot be empty."),

    /**
     * Invalid application certification information.
     */
    INVALID_APPLICATION_CERT_DATA("ALRB-APIM-WSO2-4065", "Invalid certificate data provided."),

    /**
     * Error on setting cert to application.
     */
    ERROR_ON_ADDING_CERT_TO_APP("ALRB-APIM-WSO2-4066",
            "Error occurred while setting application with the certificate information."),

    /**
     * General message for the signature validation failed for consent.
     */
    SIGNATURE_VALIDATION_FAILED_FOR_CONSENT("ALRB-WSO2-4067",
            "Signature validation failed for the consent details received."),

    /**
     * Error on constructing registration successful email content.
     */
    ERROR_CONSTRUCTING_EMAIL("ALRB-WSO2-4068", "Error on constructing email content."),

    /**
     * PSD2 TPP on boarding email address not defined.
     */
    TPP_ON_BOARDING_EMAIL_ADDRESS_NOT_DEFINED("ALRB-WSO2-4069", "PSD2 TPP on boarding email address not defined."),

    /**
     * PSD2 TPP on boarding email password not defined.
     */
    TPP_ON_BOARDING_EMAIL_PASSWORD_NOT_DEFINED("ALRB-WSO2-4070", "PSD2 TPP on boarding email password not defined."),

    /**
     * Accounts department email address not defined.
     */
    ACCOUNTS_DEPARTMENT_EMAIL_ADDRESS_NOT_DEFINED("ALRB-WSO2-4071", "Accounts department email address not defined."),

    /**
     * Error on obtaining user information for the workflow reference.
     */
    ERROR_ON_OBTAINING_USER_FOR_WORKFLOW_REFERENCE("ALRB-WSO2-4072",
            "Error on obtaining user information for the workflow reference."),

    /**
     * Error occurred while decrypting the username.
     */
    USERNAME_DECRYPTION_ERROR("ALRB-WSO2-4073", "Error occurred while decrypting the username."),

    /**
     * Error engaging user with the roles.
     */
    ERROR_ON_ENGAGING_USER_WITH_ROLES("ALRB-WSO2-4074", "Error engaging user with the roles."),

    /**
     * Error on obtaining user claim values.
     */
    ERROR_ON_OBTAINING_USER_CLAIM_VALUES("ALRB-WSO2-4075", "Error on obtaining user claim values."),

    /**
     *   Scheme name not supported in debtor account
     */
    SCHEME_NAME_NOT_SUPPORTED_IN_DEBTOR_ACCOUNT("ALRB-WSO2-4076",
            "Scheme name not supported. Please specify a Sort Code"),

    PINCODE_NOT_COMPLEX_ENOUGH("ALRB-WSO2-4077",
            "Pincode not complex enough. Please avoid patterns");

            private final String errorCode;
    private final String message;

    /**
     * Sets the error code.
     *
     * @param errorCode error code
     * @param message   error message
     */
    AlRayanError(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Returns the error code.
     *
     * @return error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the error message.
     *
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the complete Al Rayan Error Message.
     *
     * @return Al Rayan error message
     */
    public String getErrorMessageWithCode() {
        return errorCode + " - " + message;
    }

    @Override
    public String toString() {
        return "AlRayanError{" +
               "errorCode='" + errorCode + '\'' +
               ", message='" + message + '\'' +
               '}';
    }
}
