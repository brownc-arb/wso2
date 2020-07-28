package com.alrayan.wso2.common;

/**
 * Defines the constants common across the Al Rayan WSO2 code base.
 *
 * @since 1.0.0
 */
public class AlRayanConstants {

    // Other constants
    public static final String CONF_FILE_NAME = "alrayan-identity.properties";

    // Claims
    public static final String CLAIM_PROFILE = "default";
    public static final String WSO2_DEFAULT_CLAIM_DIALECT = "http://wso2.org/claims";
    public static final String DBP_CLAIM_PREFIX = "/dbp";
    public static final String CLAIM_PIN_CODE = WSO2_DEFAULT_CLAIM_DIALECT + DBP_CLAIM_PREFIX + "pincode";
    public static final String CLAIM_ACTING_USERNAME = WSO2_DEFAULT_CLAIM_DIALECT + DBP_CLAIM_PREFIX + "username";
    public static final String CLAIM_URL_ACCOUNT_DISABLED = "http://wso2.org/claims/identity/accountDisabled";
    public static final String CLAIM_URL_ACCOUNT_LOCKED = "http://wso2.org/claims/identity/accountLocked";

    // Custom database properties
    public static final String IS_DBP_USER_EXISTING_SQL = "IsDBPUserExistingSQL";
    public static final String GET_DBP_PIN_CODE_FOR_DBP_USERNAME_SQL = "GetDBPPINCodeForDBPUserNameSQL";
    public static final String GET_DBP_PIN_CODE_FOR_USERNAME_SQL = "GetDBPPINCodeForUserNameSQL";
    public static final String UPDATE_DBP_PIN_CODE_FOR_USERNAME_SQL = "UpdateDBPPINCodeForUserNameSQL";
    public static final String UPDATE_DBP_PIN_CODE_FOR_DBP_USERNAME_SQL = "UpdateDBPPINCodeForDBPUserNameSQL";
    public static final String GET_USERNAME_FOR_DBP_USERNAME_SQL = "GetUserNameForDBPUsernameSQL";
    public static final String GET_DBP_USERNAME_FOR_USERNAME_SQL = "GetDBPUsernameForUserNameSQL";

    // Authenticators
    public static final String ARB_DESKTOP_CONNECTOR_FRIENDLY_NAME = "ARBDesktopAuthenticator";
    public static final String ARB_DESKTOP_CONNECTOR_NAME = "ARBDesktopAuthenticator";
    public static final String ARB_BASIC_AUTHENTICATOR_NAME = "AlRayanBasicAuthenticator";
    public static final String ARB_BASIC_AUTHENTICATOR_FRIENDLY_NAME = "alrayanbasicauth";
    public static final String ARB_MOBILE_CONNECTOR_FRIENDLY_NAME = "ARBMobileAuthenticator";
    public static final String ARB_MOBILE_CONNECTOR_NAME = "ARBMobileAuthenticator";
}
