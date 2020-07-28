package com.alrayan.wso2.auth.arbmobile.util;


/**
 * ARBMobileAuthenticator constants.
 *
 * @since 1.0.0
 */
public class ARBMobileConstants {

    public static final long serialVersionUID = -1754559341308243658L;
    public static final String CONNECTOR_FRIENDLY_NAME = "ARBMobileAuthenticator";
    public static final String CONNECTOR_NAME = "ARBMobileAuthenticator";
    public static final String CALLBACK_URL_PROPERTY = "callbackUrl";
    public static final String OAUTH_ENDPOINT_PROPERTY = "oauthendpoint";
    public static final String LOGIN_TYPE = "ARBMobile";
    public static final String OAUTH2_GRANT_TYPE_CODE = "code";
    public static final String OAUTH2_PARAM_STATE = "state";
    public static final String OAUTH2_PARAM_ERROR = "error";
    public static final String CLIENT_ID = "client_id";


    // Constants required on retry.
    public static final String USER_NAME = "salesforceId";
    public static final String FAILED_USERNAME = "&failedUsername=";
    public static final String ERROR_CODE = "&errorCode=";
    public static final String AL_RAYAN_ERROR_CODE = "&alrayanErrorCode=";
    public static final String AL_RAYAN_ERROR_MESSAGE = "&alrayanErrorMessage=";
    public static final String AUTHENTICATORS = "&authenticators=";
    public static final String UTF_8 = "UTF-8";
    public static final String SALESFORCE_ID_NOT_SPECIFIED_ERROR_CODE = "SFIDNULL";
    public static final String ERROR_CONSENT_VALIDATION_FAILED_ERROR_CODE = "CONSENTVALIDATIONFAILED";

    //consent management related constants
    public static final String AWAITING_AUTHORISATION = "AwaitingAuthorisation";
    public static final String IS_ERROR = "isError";
    public static final String EXPIRATION_DATE_TIME = "Expiration Date Time";
    public static final String TRANSACTION_FROM_DATE_TIME = "Transaction From Date Time";
    public static final String TRANSACTION_TO_DATE_TIME = "Transaction To Date Time";
    public static final String PERMISSIONS = "permissions";
    public static final String DATES = "dates";
    public static final String USER_CONSENT = "consent";

}
