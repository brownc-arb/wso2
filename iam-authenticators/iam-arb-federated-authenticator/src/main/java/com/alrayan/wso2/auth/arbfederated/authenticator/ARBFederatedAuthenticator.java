package com.alrayan.wso2.auth.arbfederated.authenticator;

import com.alrayan.wso2.auth.arbfederated.response.ARBFederatedResponseProcessor;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Federated authenticator to handle authentication with a 3rd party platform (DBP).
 *
 * @since 1.0.0
 */
public class ARBFederatedAuthenticator extends AbstractApplicationAuthenticator
        implements FederatedApplicationAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(ARBFederatedAuthenticator.class);
    private static final long serialVersionUID = -1754559341308243658L;
    private static final String CONNECTOR_FRIENDLY_NAME = "ARBFederatedAuthenticator";
    private static final String CONNECTOR_NAME = "ARBFederatedAuthenticator";
    private static final String CALLBACK_URL_PROPERTY = "callbackUrl";
    private static final String OAUTH_ENDPOINT_PROPERTY = "oauthendpoint";
    private static final String LOGIN_TYPE = "ARBFederated";
    private static final String OAUTH2_GRANT_TYPE_CODE = "code";
    private static final String OAUTH2_PARAM_STATE = "state";
    private static final String OAUTH2_PARAM_ERROR = "error";

    // Constants required on retry.
    private static final String USER_NAME = "salesforceId";
    private static final String DBP_RESPONSE = "dbpResponse";
    private static final String FAILED_USERNAME = "&failedUsername=";
    private static final String ERROR_CODE = "&errorCode=";
    private static final String AL_RAYAN_ERROR_CODE = "&alrayanErrorCode=";
    private static final String AL_RAYAN_ERROR_MESSAGE = "&alrayanErrorMessage=";
    private static final String AUTHENTICATORS = "&authenticators=";
    private static final String UTF_8 = "UTF-8";
    private static final String SALESFORCE_ID_NOT_SPECIFIED_ERROR_CODE = "SFIDNULL";

    /**
     * {@inheritDoc}
     * <p>
     * The value of the state parameter should be [random string],ARBFederated in order to proceed + code parameter
     * should be available
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        log.trace("Inside ARBFederatedAuthenticator.canHandle()");
        return (request.getParameter(OAUTH2_PARAM_STATE) != null
                && getLoginType(request))
               || request.getParameter(OAUTH2_PARAM_ERROR) != null;
    }

    @Override
    public String getFriendlyName() {
        return CONNECTOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return CONNECTOR_NAME;
    }

    @Override
    public List<Property> getConfigurationProperties() {
        List<Property> configProperties = new ArrayList<>();

        // authorization endpoint
        Property authorizationEndpoint = new Property();
        authorizationEndpoint.setDisplayName("Authorization endpoint");
        authorizationEndpoint.setName(OAUTH_ENDPOINT_PROPERTY);
        authorizationEndpoint.setDescription("Enter value corresponding to authorization endpoint.");
        authorizationEndpoint.setRequired(true);
        authorizationEndpoint.setDisplayOrder(0);
        configProperties.add(authorizationEndpoint);

        // Callback URL
        Property callbackUrl = new Property();
        callbackUrl.setDisplayName("Callback URL");
        callbackUrl.setName(CALLBACK_URL_PROPERTY);
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
                                        ERROR_CODE + errorCode + FAILED_USERNAME +
                                        URLEncoder.encode(request.getParameter(USER_NAME), UTF_8) +
                                        AL_RAYAN_ERROR_CODE + AlRayanError.USER_ACCOUNT_LOCKED.getErrorCode() +
                                        AL_RAYAN_ERROR_MESSAGE + AlRayanError.USER_ACCOUNT_LOCKED.getMessage() +
                                        "&remainingAttempts=0";
                            } else {
                                authorizationEP =
                                        response.encodeRedirectURL(authorizationEP + ("?" + queryParams)) +
                                        ERROR_CODE + errorCode + FAILED_USERNAME +
                                        URLEncoder.encode(request.getParameter(USER_NAME), UTF_8) +
                                        AL_RAYAN_ERROR_CODE + AlRayanError.USER_ACCOUNT_LOCKED.getErrorCode() +
                                        AL_RAYAN_ERROR_MESSAGE + AlRayanError.USER_ACCOUNT_LOCKED.getMessage();
                            }
                            response.sendRedirect(authorizationEP);
                            break;
                        case UserCoreConstants.ErrorCode.USER_DOES_NOT_EXIST:
                            retryParam = retryParam + ERROR_CODE + errorCode + FAILED_USERNAME + URLEncoder
                                    .encode(request.getParameter(USER_NAME), UTF_8) +
                                         AL_RAYAN_ERROR_CODE + AlRayanError.USER_DOES_NOT_EXISTS.getErrorCode() +
                                         AL_RAYAN_ERROR_MESSAGE + AlRayanError.USER_DOES_NOT_EXISTS.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams) + AUTHENTICATORS +
                                                  getName() + ":" + idpName + retryParam);
                            break;
                        case IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE:
                            retryParam = retryParam + ERROR_CODE + errorCode + FAILED_USERNAME +
                                         URLEncoder.encode(request.getParameter(USER_NAME), UTF_8) +
                                         AL_RAYAN_ERROR_CODE + AlRayanError.USER_ACCOUNT_DISABLED.getErrorCode() +
                                         AL_RAYAN_ERROR_MESSAGE + AlRayanError.USER_ACCOUNT_DISABLED.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams)
                                                  + AUTHENTICATORS + getName() + ":" + idpName + retryParam);
                            break;
                        case SALESFORCE_ID_NOT_SPECIFIED_ERROR_CODE:
                            retryParam = retryParam + ERROR_CODE + errorCode + FAILED_USERNAME + "" +
                                         AL_RAYAN_ERROR_CODE + AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorCode() +
                                         AL_RAYAN_ERROR_MESSAGE + AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams)
                                                  + AUTHENTICATORS + getName() + ":" + idpName + retryParam);
                            break;
                        default:
                            retryParam = retryParam + ERROR_CODE + errorCode
                                         + FAILED_USERNAME + URLEncoder.encode(request.getParameter(USER_NAME), UTF_8) +
                                         AL_RAYAN_ERROR_CODE +
                                         AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getErrorCode() +
                                         AL_RAYAN_ERROR_MESSAGE +
                                         AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getMessage();
                            response.sendRedirect(authorizationEP + ("?" + queryParams) + AUTHENTICATORS +
                                                  getName() + ":" + idpName + retryParam);
                            break;
                    }
                } else {
                    log.debug("Unknown identity error code.");
                    response.sendRedirect(authorizationEP + ("?" + queryParams) + AUTHENTICATORS +
                                          getName() + ":" + idpName + retryParam);
                }
            } else {
                String callbackurl = getCallbackUrl(authenticatorProperties);
                String state = context.getContextIdentifier() + "," + LOGIN_TYPE;
                String sessionDataKey = request.getParameter("sessionDataKey");
                OAuthClientRequest authzRequest;
                authzRequest = OAuthClientRequest
                        .authorizationLocation(authorizationEP)
                        .setRedirectURI(callbackurl)
                        .setParameter("sessionDataKey", sessionDataKey)
                        .setResponseType(OAUTH2_GRANT_TYPE_CODE)
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
        try {
            String dbpResponse = request.getParameter(DBP_RESPONSE);

            ARBFederatedResponseProcessor arbFederatedResponseProcessor = new ARBFederatedResponseProcessor();

            if (!arbFederatedResponseProcessor.verifyDBPSignature(context, dbpResponse)) {
                log.error("Signature validation is failed");
                throw new AuthenticationFailedException("Signature validation failed");
            }

            String username = arbFederatedResponseProcessor.retirveUsername(dbpResponse);

            if (StringUtils.isEmpty(username)) {
                IdentityErrorMsgContext customErrorMessageContext =
                        new IdentityErrorMsgContext(SALESFORCE_ID_NOT_SPECIFIED_ERROR_CODE);
                IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
                throw new AuthenticationFailedException(
                        AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorMessageWithCode());
            }
            username = MultitenantUtils.getTenantAwareUsername(username);
            String tenantDomain = MultitenantUtils.getTenantDomain(username);

            // Validate user account
            UserStoreManager userStoreManager = UserManagementUtil.getAlRayanUserManagerService();
            boolean isUserExists = userStoreManager.isExistingUser(username);
            if (!isUserExists) {
                IdentityErrorMsgContext customErrorMessageContext =
                        new IdentityErrorMsgContext(UserCoreConstants.ErrorCode.USER_DOES_NOT_EXIST);
                IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
                throw new AuthenticationFailedException(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode());
            }

            Map<String, String> values = userStoreManager
                    .getUserClaimValues(username,
                            new String[]{AlRayanConstants.CLAIM_URL_ACCOUNT_LOCKED,
                                         AlRayanConstants.CLAIM_URL_ACCOUNT_DISABLED}, AlRayanConstants.CLAIM_PROFILE);

            // Check account lock
            boolean accountLocked = Boolean.parseBoolean(values.get(AlRayanConstants.CLAIM_URL_ACCOUNT_LOCKED));
            if (accountLocked) {
                IdentityErrorMsgContext customErrorMessageContext = new IdentityErrorMsgContext(UserCoreConstants
                        .ErrorCode.USER_IS_LOCKED);
                IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
                throw new AuthenticationFailedException(AlRayanError.USER_ACCOUNT_LOCKED.getErrorMessageWithCode());
            }

            // Check account disable
            boolean accountDisable = Boolean.parseBoolean(values.get(AlRayanConstants.CLAIM_URL_ACCOUNT_DISABLED));
            if (accountDisable) {
                IdentityErrorMsgContext customErrorMessageContext =
                        new IdentityErrorMsgContext(IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE);
                IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
                throw new AuthenticationFailedException(AlRayanError.USER_ACCOUNT_DISABLED.getErrorMessageWithCode());
            }
            AuthenticatedUser authenticatedUser = UserManagementUtil.buildAuthenticatedUser(username,
                    AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue(), tenantDomain, false);
            context.setSubject(authenticatedUser);
        } catch (UserStoreException e) {
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                            .getErrorCode());
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode());
        }
    }

    /**
     * Returns the authorization server endpoint.
     *
     * @param authenticatorProperties authenticator properties
     * @return authorization server endpoint
     */
    private String getAuthorizationServerEndpoint(Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(OAUTH_ENDPOINT_PROPERTY);
    }

    /**
     * Returns the callback URL.
     *
     * @param authenticatorProperties authenticator properties.
     * @return callback URL
     */
    private String getCallbackUrl(Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(CALLBACK_URL_PROPERTY);
    }

    /**
     * Check whether the state contain login type or not.
     *
     * @param request the request
     * @return login type
     */
    private Boolean getLoginType(HttpServletRequest request) {
        String state = request.getParameter(OAUTH2_PARAM_STATE);
        if (StringUtils.isNotEmpty(state)) {
            return state.contains(LOGIN_TYPE);
        } else {
            return false;
        }
    }
}
