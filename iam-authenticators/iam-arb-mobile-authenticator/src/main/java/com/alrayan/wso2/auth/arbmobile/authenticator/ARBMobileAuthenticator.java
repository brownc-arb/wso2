package com.alrayan.wso2.auth.arbmobile.authenticator;

import com.alrayan.wso2.auth.arbmobile.consent.ARBMobileConsentProcessor;
import com.alrayan.wso2.auth.arbmobile.util.ARBMobileConstants;
import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanConstants;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.user.core.util.UserManagementUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Federated authenticator to handle authentication with a 3rd party platform (DBP).
 *
 * @since 1.0.0
 */
public class ARBMobileAuthenticator extends AbstractApplicationAuthenticator
        implements FederatedApplicationAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(ARBMobileAuthenticator.class);
    private static final long serialVersionUID = -1754559341308243658L;
    ARBMobileConsentProcessor arbMobileConsentProcessor = new ARBMobileConsentProcessor();

    /**
     * {@inheritDoc}
     * <p>
     * The value of the state parameter should be [random string],ARBFederated in order to proceed + code parameter
     * should be available
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        log.trace("Inside ARBMobileAuthenticator.canHandle()");
        return (request.getParameter(ARBMobileConstants.OAUTH2_PARAM_STATE) != null
                && getLoginType(request))
                || request.getParameter(ARBMobileConstants.OAUTH2_PARAM_ERROR) != null;
    }

    @Override
    public String getFriendlyName() {
        return ARBMobileConstants.CONNECTOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return ARBMobileConstants.CONNECTOR_NAME;
    }

    @Override
    public List<Property> getConfigurationProperties() {
        List<Property> configProperties = new ArrayList<>();

        // authorization endpoint
        Property authorizationEndpoint = new Property();
        authorizationEndpoint.setDisplayName("Authorization endpoint");
        authorizationEndpoint.setName(ARBMobileConstants.OAUTH_ENDPOINT_PROPERTY);
        authorizationEndpoint.setDescription("Enter value corresponding to authorization endpoint.");
        authorizationEndpoint.setRequired(true);
        authorizationEndpoint.setDisplayOrder(0);
        configProperties.add(authorizationEndpoint);

        // Callback URL
        Property callbackUrl = new Property();
        callbackUrl.setDisplayName("Callback URL");
        callbackUrl.setName(ARBMobileConstants.CALLBACK_URL_PROPERTY);
        callbackUrl.setDescription("Enter value corresponding to callback url.");
        callbackUrl.setRequired(true);
        callbackUrl.setDisplayOrder(1);
        configProperties.add(callbackUrl);

        return configProperties;
    }

    @Override
    protected boolean retryAuthenticationEnabled(AuthenticationContext context) {
        return true;
    }

    @Override
    protected boolean isRedirectToMultiOptionPageOnFailure() {
        return false;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

        try {
            Map<String, String> parameterMap = getAuthenticatorConfig().getParameterMap();
            String showAuthFailureReason = null;
            if (parameterMap != null) {
                showAuthFailureReason = parameterMap.get("showAuthFailureReason");
                log.debug("showAuthFailureReason has been set as : " + showAuthFailureReason);
            }

            Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();
            if (authenticatorProperties == null) {
                throw new AuthenticationFailedException("Authenticator Properties cannot be null");
            }
            String authorizationEP = getAuthorizationServerEndpoint(authenticatorProperties);
            String queryParams = context.getContextIdIncludedQueryParams();
            String retryParam = "";
            String idpName = context.getExternalIdP().getIdentityProvider().getIdentityProviderName();
            String callbackurl = getCallbackUrl(authenticatorProperties);

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            if (context.getProperty("UserTenantDomainMismatch") != null &&
                    (Boolean) context.getProperty("UserTenantDomainMismatch")) {
                retryParam = "&authFailure=true&authFailureMsg=user.tenant.domain.mismatch.message";
                context.setProperty("UserTenantDomainMismatch", false);
            }

            IdentityErrorMsgContext errorContext = IdentityUtil.getIdentityErrorMsg();
            IdentityUtil.clearIdentityErrorMsg();
            if (errorContext != null && errorContext.getErrorCode() != null) {
                log.debug("Identity error message context is not null");
                String errorCode = errorContext.getErrorCode();

                if ("true".equals(showAuthFailureReason)) {
                    int remainingAttempts =
                            errorContext.getMaximumLoginAttempts() - errorContext.getFailedLoginAttempts();
                    switch (errorCode) {
                        case UserCoreConstants.ErrorCode.USER_IS_LOCKED:
                            if (remainingAttempts == 0) {
                                authorizationEP =
                                        response.encodeRedirectURL(authorizationEP + ("?" + queryParams)) +
                                                ARBMobileConstants.ERROR_CODE + errorCode +
                                                ARBMobileConstants.FAILED_USERNAME +
                                                URLEncoder.encode(request.getParameter(ARBMobileConstants.USER_NAME),
                                                        ARBMobileConstants.UTF_8) +
                                                ARBMobileConstants.AL_RAYAN_ERROR_CODE +
                                                AlRayanError.USER_ACCOUNT_LOCKED.getErrorCode() +
                                                ARBMobileConstants.AL_RAYAN_ERROR_MESSAGE +
                                                AlRayanError.USER_ACCOUNT_LOCKED.getMessage() +
                                                "&remainingAttempts=0";
                            } else {
                                authorizationEP =
                                        response.encodeRedirectURL(authorizationEP + ("?" + queryParams)) +
                                                ARBMobileConstants.ERROR_CODE + errorCode + ARBMobileConstants.
                                                FAILED_USERNAME +
                                                URLEncoder.encode(request.getParameter(ARBMobileConstants.USER_NAME),
                                                        ARBMobileConstants.UTF_8) +
                                                ARBMobileConstants.AL_RAYAN_ERROR_CODE +
                                                AlRayanError.USER_ACCOUNT_LOCKED.getErrorCode() +
                                                ARBMobileConstants.AL_RAYAN_ERROR_MESSAGE +
                                                AlRayanError.USER_ACCOUNT_LOCKED.getMessage();
                            }
                            response.sendRedirect(authorizationEP);
                            break;
                        case UserCoreConstants.ErrorCode.USER_DOES_NOT_EXIST:
                            retryParam = retryParam + ARBMobileConstants.ERROR_CODE + errorCode +
                                    ARBMobileConstants.FAILED_USERNAME + URLEncoder
                                    .encode(request.getParameter(ARBMobileConstants.USER_NAME),
                                            ARBMobileConstants.UTF_8) +
                                    ARBMobileConstants.AL_RAYAN_ERROR_CODE + AlRayanError.
                                    USER_DOES_NOT_EXISTS.getErrorCode() +
                                    ARBMobileConstants.AL_RAYAN_ERROR_MESSAGE + AlRayanError.
                                    USER_DOES_NOT_EXISTS.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams) +
                                    ARBMobileConstants.AUTHENTICATORS +
                                    getName() + ":" + idpName + retryParam);
                            break;
                        case IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE:
                            retryParam = retryParam + ARBMobileConstants.ERROR_CODE + errorCode +
                                    ARBMobileConstants.FAILED_USERNAME +
                                    ARBMobileConstants.AL_RAYAN_ERROR_CODE + AlRayanError.
                                    USER_ACCOUNT_DISABLED.getErrorCode() +
                                    ARBMobileConstants.AL_RAYAN_ERROR_MESSAGE + AlRayanError.
                                    USER_ACCOUNT_DISABLED.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams)
                                    + ARBMobileConstants.AUTHENTICATORS + getName() + ":" +
                                    idpName + retryParam);
                            break;
                        case ARBMobileConstants.SALESFORCE_ID_NOT_SPECIFIED_ERROR_CODE:
                            retryParam = retryParam + ARBMobileConstants.ERROR_CODE + errorCode +
                                    ARBMobileConstants.FAILED_USERNAME + "" +
                                    ARBMobileConstants.AL_RAYAN_ERROR_CODE +
                                    AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorCode() +
                                    ARBMobileConstants.AL_RAYAN_ERROR_MESSAGE +
                                    AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams)
                                    + ARBMobileConstants.AUTHENTICATORS + getName() + ":" +
                                    idpName + retryParam);
                            break;
                        case ARBMobileConstants.ERROR_CONSENT_VALIDATION_FAILED_ERROR_CODE:
                            retryParam = retryParam + ARBMobileConstants.ERROR_CODE + errorCode +
                                    ARBMobileConstants.FAILED_USERNAME + "" +
                                    ARBMobileConstants.AL_RAYAN_ERROR_CODE +
                                    AlRayanError.ERROR_CONSENT_VALIDATION_FAILED.getErrorCode() +
                                    ARBMobileConstants.AL_RAYAN_ERROR_MESSAGE +
                                    AlRayanError.ERROR_CONSENT_VALIDATION_FAILED.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams)
                                    + ARBMobileConstants.AUTHENTICATORS + getName() + ":" +
                                    idpName + retryParam);
                            break;
                        default:
                            retryParam = retryParam + ARBMobileConstants.ERROR_CODE + errorCode
                                    + ARBMobileConstants.FAILED_USERNAME + URLEncoder.encode(request.
                                    getParameter(ARBMobileConstants.USER_NAME), ARBMobileConstants.UTF_8) +
                                    ARBMobileConstants.AL_RAYAN_ERROR_CODE +
                                    AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getErrorCode() +
                                    ARBMobileConstants.AL_RAYAN_ERROR_MESSAGE +
                                    AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams) +
                                    ARBMobileConstants.AUTHENTICATORS +
                                    getName() + ":" + idpName + retryParam);
                            break;
                    }
                } else {
                    log.debug("Unknown identity error code.");
                    response.sendRedirect(authorizationEP + ("?" + queryParams) + ARBMobileConstants.AUTHENTICATORS +
                            getName() + ":" + idpName + retryParam);
                }
            } else {
                String clientId = context.getAuthenticationRequest().getRequestQueryParam("client_id")[0];
                String consentId = arbMobileConsentProcessor.getConsentId(
                        context.getAuthenticationRequest().getRequestQueryParam("request")[0], clientId);
                String scope = arbMobileConsentProcessor.getConsentScope(context.getAuthenticationRequest().
                        getRequestQueryParam("request")[0]);

                if (StringUtils.isEmpty(clientId)) {
                    throw new AuthenticationFailedException(AlRayanError.ERROR_CLIENT_DETAILS_NOT_FOUND.getMessage());
                }
                if (StringUtils.isEmpty(consentId)) {
                    throw new AuthenticationFailedException(AlRayanError.
                            ERROR_CONSENT_INFORMATION_NOT_FOUND.getMessage());
                }

                String state = context.getContextIdentifier() + "," + ARBMobileConstants.LOGIN_TYPE;
                String sessionDataKey = request.getParameter("sessionDataKey");

                org.json.JSONArray consentValues = new org.json.JSONArray().put(
                        arbMobileConsentProcessor.getConsentDetails(consentId, clientId));

                SecretKey secretKey = arbMobileConsentProcessor.getSymmetricKeyForConsentEncryption();
                String consent = arbMobileConsentProcessor.
                        generateSecureMessage(consentValues, scope, context, secretKey);

                context.setProperty("cryptokey", secretKey);

                OAuthClientRequest authzRequest;
                authzRequest = OAuthClientRequest
                        .authorizationLocation(authorizationEP)
                        .setRedirectURI(callbackurl)
                        .setParameter("sessionDataKey", sessionDataKey)
                        .setParameter("consent", consent)
                         .setResponseType(ARBMobileConstants.OAUTH2_GRANT_TYPE_CODE)
                        .setState(state).buildQueryMessage();
                String loginPage = authzRequest.getLocationUri();

                response.sendRedirect(loginPage);
            }
        } catch (IOException | OAuthSystemException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        if (request.getSession().getAttribute("contextIdentifier") == null) {
            request.getSession().setAttribute("contextIdentifier", request.getParameter("sessionDataKey"));
            return request.getParameter("sessionDataKey");
        } else {
            return (String) request.getSession().getAttribute("contextIdentifier");
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

        String username = request.getParameter(ARBMobileConstants.USER_NAME);
        Map<String, String> claimValues;
        boolean isUserExists = false;

        if (log.isDebugEnabled()) {
            log.debug("User response has been received from the application");
        }

        if (StringUtils.isEmpty(username)) {
            log.error(AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorMessageWithCode());
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(ARBMobileConstants.SALESFORCE_ID_NOT_SPECIFIED_ERROR_CODE);
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(
                    AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorMessageWithCode());
        }
        username = MultitenantUtils.getTenantAwareUsername(username);
        String tenantDomain = MultitenantUtils.getTenantDomain(username);

        // Validate user account
        UserStoreManager userStoreManager = null;
        try {
            userStoreManager = UserManagementUtil.getAlRayanUserManagerService();
            isUserExists = userStoreManager.isExistingUser(username);
        } catch (UserStoreException e) {
            log.error(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getErrorMessageWithCode(), e);
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                            .getErrorCode());
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode());
        }

        if (!isUserExists) {
            log.error(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode());
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(UserCoreConstants.ErrorCode.USER_DOES_NOT_EXIST);
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode());
        }

        try {
            claimValues = userStoreManager
                    .getUserClaimValues(username,
                            new String[]{AlRayanConstants.CLAIM_URL_ACCOUNT_LOCKED,
                                    AlRayanConstants.CLAIM_URL_ACCOUNT_DISABLED}, AlRayanConstants.CLAIM_PROFILE);
        } catch (UserStoreException e) {
            log.error(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode(), e);
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                            .getErrorCode());
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode());
        }

        // Check account lock
        boolean accountLocked = Boolean.parseBoolean(claimValues.get(AlRayanConstants.CLAIM_URL_ACCOUNT_LOCKED));
        if (accountLocked) {
            log.error(AlRayanError.USER_ACCOUNT_LOCKED.getErrorMessageWithCode());
            IdentityErrorMsgContext customErrorMessageContext = new IdentityErrorMsgContext(UserCoreConstants
                    .ErrorCode.USER_IS_LOCKED);
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.USER_ACCOUNT_LOCKED.getErrorMessageWithCode());
        }

        // Check account disable
        boolean accountDisable = Boolean.parseBoolean(claimValues.get(AlRayanConstants.CLAIM_URL_ACCOUNT_DISABLED));
        if (accountDisable) {
            log.error(AlRayanError.USER_ACCOUNT_DISABLED.getErrorMessageWithCode());
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE);
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.USER_ACCOUNT_DISABLED.getErrorMessageWithCode());
        }

        //validate consent
        String consentDetails = request.getParameter(ARBMobileConstants.USER_CONSENT);
        String clientId = context.getAuthenticationRequest().getRequestQueryParam(ARBMobileConstants.CLIENT_ID)[0];
        String consentId = arbMobileConsentProcessor.getConsentId(
                context.getAuthenticationRequest().getRequestQueryParam("request")[0], clientId);

        if ("true".equals(AlRayanConfiguration.CONSENT_JWT_SIGNATURE_VERIFICATION_ENABLED.getValue())) {

            if (log.isDebugEnabled()) {
                log.debug("Signature verification is enabled");
            }

            if (!arbMobileConsentProcessor.verifyconsentSignature(context, consentDetails)) {
                log.error(AlRayanError.SIGNATURE_VALIDATION_FAILED_FOR_CONSENT.getErrorMessageWithCode());
                throw new AuthenticationFailedException
                        (AlRayanError.SIGNATURE_VALIDATION_FAILED_FOR_CONSENT.getErrorMessageWithCode());
            }
            log.debug("Consent signature verification has been successfully done");
        }


        SecretKey secretKey = (SecretKey) context.getParameter("cryptokey");

        if (!arbMobileConsentProcessor.validateConsentData(consentDetails, clientId, consentId, secretKey)) {
            log.error(AlRayanError.ERROR_CONSENT_VALIDATION_FAILED.getErrorMessageWithCode());
            IdentityErrorMsgContext customErrorMessageContext = new IdentityErrorMsgContext(ARBMobileConstants.
                    ERROR_CONSENT_VALIDATION_FAILED_ERROR_CODE);
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.
                    ERROR_CONSENT_VALIDATION_FAILED.getErrorMessageWithCode());
        }

        arbMobileConsentProcessor.updateConsentDetails(consentDetails, consentId, username);


        AuthenticatedUser authenticatedUser = UserManagementUtil.buildAuthenticatedUser(username,
                AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue(), tenantDomain, false);
        context.setSubject(authenticatedUser);
    }

    /**
     * Returns the authorization server endpoint.
     *
     * @param authenticatorProperties authenticator properties
     * @return authorization server endpoint
     */
    private String getAuthorizationServerEndpoint(Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(ARBMobileConstants.OAUTH_ENDPOINT_PROPERTY);
    }

    /**
     * Returns the callback URL.
     *
     * @param authenticatorProperties authenticator properties.
     * @return callback URL
     */
    private String getCallbackUrl(Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(ARBMobileConstants.CALLBACK_URL_PROPERTY);
    }

    /**
     * Check whether the state contain login type or not.
     *
     * @param request the request
     * @return login type
     */
    private Boolean getLoginType(HttpServletRequest request) {
        String state = request.getParameter(ARBMobileConstants.OAUTH2_PARAM_STATE);
        if (StringUtils.isNotEmpty(state)) {
            return state.contains(ARBMobileConstants.LOGIN_TYPE);
        } else {
            return false;
        }
    }
}
