package com.alrayan.wso2.auth.arbdesktop;

import com.alrayan.wso2.common.AlRayanConstants;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.user.core.util.UserManagementUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Custom authenticator to handle the desktop journey authentication.
 *
 * @since 1.0.0
 */
public class
ARBDesktopAuthenticator extends AbstractApplicationAuthenticator implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 22479544628204612L;
    private static final Logger log = LoggerFactory.getLogger(ARBDesktopAuthenticator.class);
    private static final String CONSENT = "consent";
    private static final String CONSENT_PROMPTED = "consentPrompted";
    private static final String CONSENT_APPROVE = "approve";

    // Constants required on retry.
    private static final String ERROR_CODE = "&errorCode=";
    private static final String FAILED_USERNAME = "&failedUsername=";
    private static final String AL_RAYAN_ERROR_CODE = "&alrayanErrorCode=";
    private static final String AL_RAYAN_ERROR_MESSAGE = "&alrayanErrorMessage=";
    private static final String USER_PARAM = "user";
    private static final String UTF_8 = "UTF-8";
    private static final String AUTHENTICATORS = "&authenticators=";
    private static final String AUTH_FAILURE_MASSAGE = "&statusMsg=";
    private static final String AUTH_FAILURE_TITLE = "&status=";
    private static final String LOCAL = "LOCAL";
    private static final String CONSENT_NOT_APPROVED = "ConsentNotApproved";

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        String consent = httpServletRequest.getParameter(CONSENT);
        return consent != null;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    @Override
    public String getName() {
        return AlRayanConstants.ARB_DESKTOP_CONNECTOR_NAME;
    }

    @Override
    public String getFriendlyName() {
        return AlRayanConstants.ARB_DESKTOP_CONNECTOR_FRIENDLY_NAME;
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

            String retryEP = ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL();
            String queryParams = context.getContextIdIncludedQueryParams();
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true";
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
                    String username = request.getParameter(USER_PARAM);
                    username = StringUtils.isNotEmpty(username) ?
                               UserCoreUtil.removeDomainFromName(username) :
                               username;
                    switch (errorCode) {
                        case UserCoreConstants.ErrorCode.USER_IS_LOCKED:
                            if (remainingAttempts == 0) {
                                retryEP = response.encodeRedirectURL(retryEP + ("?" + queryParams)) +
                                          ERROR_CODE + errorCode + FAILED_USERNAME +
                                          URLEncoder.encode(username, UTF_8) + AL_RAYAN_ERROR_CODE +
                                          AlRayanError.USER_ACCOUNT_LOCKED.getErrorCode() +
                                          AL_RAYAN_ERROR_MESSAGE + AlRayanError.USER_ACCOUNT_LOCKED.getMessage() +
                                          "&remainingAttempts=0" + AUTH_FAILURE_MASSAGE +
                                          AlRayanError.USER_ACCOUNT_LOCKED.getMessage() +
                                          AUTH_FAILURE_TITLE + "Error - " +
                                          AlRayanError.USER_ACCOUNT_LOCKED.getErrorCode();
                            } else {
                                retryEP = response.encodeRedirectURL(retryEP + ("?" + queryParams)) +
                                          ERROR_CODE + errorCode + FAILED_USERNAME +
                                          URLEncoder.encode(username, UTF_8) + AL_RAYAN_ERROR_CODE +
                                          AlRayanError.USER_ACCOUNT_LOCKED.getErrorCode() +
                                          AL_RAYAN_ERROR_MESSAGE + AlRayanError.USER_ACCOUNT_LOCKED.getMessage() +
                                          AUTH_FAILURE_MASSAGE +
                                          AlRayanError.USER_ACCOUNT_LOCKED.getMessage() +
                                          AUTH_FAILURE_TITLE + "Error - " + AlRayanError.USER_ACCOUNT_LOCKED
                                                  .getErrorCode();
                            }
                            response.sendRedirect(retryEP);
                            break;
                        case IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE:
                            retryParam = retryParam + ERROR_CODE + errorCode + FAILED_USERNAME +
                                         URLEncoder.encode(username, UTF_8) + AL_RAYAN_ERROR_CODE +
                                         AlRayanError.USER_ACCOUNT_DISABLED.getErrorCode() +
                                         AL_RAYAN_ERROR_MESSAGE + AlRayanError.USER_ACCOUNT_DISABLED.getMessage() +
                                         AUTH_FAILURE_MASSAGE +
                                         AlRayanError.USER_ACCOUNT_DISABLED.getMessage() +
                                         AUTH_FAILURE_TITLE + "Error - " + AlRayanError.USER_ACCOUNT_DISABLED
                                                 .getErrorCode();
                            response.sendRedirect(retryEP + ("?" + queryParams)
                                                  + AUTHENTICATORS + getName() + ":" + LOCAL + retryParam);
                            break;
                        case CONSENT_NOT_APPROVED:
                            retryParam = retryParam + ERROR_CODE + errorCode + FAILED_USERNAME +
                                         URLEncoder.encode(username, UTF_8) + AL_RAYAN_ERROR_CODE +
                                         AlRayanError.CONSENT_NOT_APPROVED.getErrorCode() +
                                         AL_RAYAN_ERROR_MESSAGE + AlRayanError.CONSENT_NOT_APPROVED.getMessage() +
                                         AUTH_FAILURE_MASSAGE +
                                         AlRayanError.CONSENT_NOT_APPROVED.getMessage() +
                                         AUTH_FAILURE_TITLE + "Error - " + AlRayanError.CONSENT_NOT_APPROVED
                                                 .getErrorCode();
                            response.sendRedirect(retryEP + ("?" + queryParams)
                                                  + AUTHENTICATORS + getName() + ":" + LOCAL + retryParam);
                            break;
                        default:
                            retryParam = retryParam + ERROR_CODE + errorCode
                                         + FAILED_USERNAME + URLEncoder.encode(username, UTF_8) + AL_RAYAN_ERROR_CODE +
                                         AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getErrorCode() +
                                         AL_RAYAN_ERROR_MESSAGE +
                                         AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getMessage() +
                                         AUTH_FAILURE_MASSAGE +
                                         AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getMessage() +
                                         AUTH_FAILURE_TITLE + "Error - " + AlRayanError.
                                    INTERNAL_SERVER_ERROR_WITH_USERSTORE.getErrorCode();
                            response.sendRedirect(retryEP + ("?" + queryParams) + AUTHENTICATORS +
                                                  getName() + ":" + LOCAL + retryParam);
                            break;
                    }
                } else {
                    log.debug("Unknown identity error code.");
                    response.sendRedirect(retryEP + ("?" + queryParams) + AUTHENTICATORS +
                                          getName() + ":" + LOCAL + retryParam);
                }
            } else {
                AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
                if (authenticatedUser == null) {
                    log.debug("User not available in AuthenticationContext. Returning");
                }
                handlePreConsent(request, response, context);
            }
        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }


    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        try {
            // Validate user account
            String username = request.getParameter(USER_PARAM);
            UserStoreManager userStoreManager = UserManagementUtil.getUserManagerService();

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

            Map<String, Object> authProperties = context.getProperties();
            if (authProperties == null) {
                authProperties = new HashMap<>();
                context.setProperties(authProperties);
            }
            String consent = request.getParameter(CONSENT);
            if (StringUtils.isEmpty(consent) || !consent.equals(CONSENT_APPROVE)) {
                IdentityErrorMsgContext customErrorMessageContext = new IdentityErrorMsgContext(CONSENT_NOT_APPROVED);
                IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
                throw new AuthenticationFailedException(AlRayanError.CONSENT_NOT_APPROVED.getErrorMessageWithCode());
            }
            context.setSubject(AuthenticatedUser
                    .createLocalAuthenticatedUserFromSubjectIdentifier(getAuthenticatedUser(context)
                            .getAuthenticatedSubjectIdentifier()));
        } catch (UserStoreException e) {
            log.error(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE.getMessage(), e);
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                            .getErrorCode());
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new AuthenticationFailedException(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode());
        }
    }

    @Override
    protected boolean retryAuthenticationEnabled(AuthenticationContext context) {
        return true;
    }

    /**
     * Returns the authenticated user from the previous authentication step.
     *
     * @param context authentication context
     * @return authenticated user from the previous authentication step
     */
    private AuthenticatedUser getAuthenticatedUser(AuthenticationContext context) {
        int currentStep = context.getCurrentStep();
        return (context.getSequenceConfig().getStepMap()).get(currentStep - 1).getAuthenticatedUser();
    }

    /**
     * Prompts the consent page.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @param context  authentication context
     * @throws AuthenticationFailedException thrown when error on authentication failure
     */
    @SuppressWarnings("unchecked")
    private void handlePreConsent(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationContext context) throws AuthenticationFailedException {
        try {
            URIBuilder uriBuilder = getUriBuilder(request, context);
            response.sendRedirect(uriBuilder.build().toString());
            context.addParameter(CONSENT_PROMPTED, true);
        } catch (IOException e) {
            log.error("Error while redirecting to consent page.", e);
            throw new AuthenticationFailedException("Error while redirecting to consent page.", e);
        } catch (URISyntaxException e) {
            log.error("Error while building redirect URI.", e);
            throw new AuthenticationFailedException("Error while building redirect URI.", e);
        }
    }

    /**
     * Returns the consent page URL.
     *
     * @param request HTTP request
     * @param context HTTP response
     * @return consent page URL
     * @throws URISyntaxException           thrown when error on URI syntax error
     * @throws UnsupportedEncodingException thrown when error on unsupported encoding
     */
    private URIBuilder getUriBuilder(HttpServletRequest request, AuthenticationContext context)
            throws URISyntaxException, UnsupportedEncodingException {
        String consentEndpointURL = FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(context.getRequestType())
                                    ? OAuth2Util.OAuthURL.getOIDCConsentPageUrl()
                                    : OAuth2Util.OAuthURL.getOAuth2ConsentPageUrl();
        URIBuilder uriBuilder = new URIBuilder(consentEndpointURL);
        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(), context.getCallerSessionKey(),
                        context.getContextIdentifier());
        ServiceProvider serviceProvider = context.getSequenceConfig().getApplicationConfig().getServiceProvider();

        Map<String, String> params = splitQuery(queryParams);
        uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY, getContextIdentifier(request));
        uriBuilder.addParameter("authenticators", getName() + ":LOCAL");


        context.addEndpointParam("loggedInUser", getAuthenticatedUser(context).getAuthenticatedSubjectIdentifier());
        context.addEndpointParam("userTenantDomain", getAuthenticatedUser(context).getTenantDomain());
        context.addEndpointParam("spQueryParams", queryParams);
        context.addEndpointParam("scope", params.get("scope"));
        context.addEndpointParam("application", serviceProvider.getApplicationName());
        return uriBuilder;
    }

    /**
     * Returns a map of query parameters from the given query param string.
     *
     * @param queryParamsString query parameter string
     * @return map of query parameters
     * @throws UnsupportedEncodingException thrown when error on un-supported encoding
     */
    private Map<String, String> splitQuery(String queryParamsString) throws UnsupportedEncodingException {
        final Map<String, String> queryParams = new HashMap<>();
        final String[] pairs = queryParamsString.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            final String value =
                    idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            queryParams.put(key, value);
        }
        return queryParams;
    }
}
