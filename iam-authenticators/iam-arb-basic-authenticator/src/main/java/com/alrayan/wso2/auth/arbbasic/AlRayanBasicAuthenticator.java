package com.alrayan.wso2.auth.arbbasic;

import com.alrayan.wso2.auth.arbbasic.internal.AlRayanBasicAuthenticatorServiceComponent;
import com.alrayan.wso2.common.AlRayanConstants;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import com.alrayan.wso2.user.core.util.UserManagementUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticator;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticatorConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
//import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authenticator to handle Al Rayan basic authentication.
 *
 * @since 1.0.0
 */
public class AlRayanBasicAuthenticator extends BasicAuthenticator {

    private static final long serialVersionUID = 8357478081967746586L;
    private static final Logger log = LoggerFactory.getLogger(BasicAuthenticator.class);
    private static final String PASSWORD_PROPERTY = "PASSWORD_PROPERTY";
    private static final String RE_CAPTCHA_USER_DOMAIN = "user-domain-recaptcha";

    @Override
    public String getFriendlyName() {
        return AlRayanConstants.ARB_BASIC_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return AlRayanConstants.ARB_BASIC_AUTHENTICATOR_NAME;
    }


    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        try {
            String username = request.getParameter(BasicAuthenticatorConstants.USER_NAME);
            String password = request.getParameter(BasicAuthenticatorConstants.PASSWORD);

            // User should enter the digital banking username.
            // Get salesforce ID (username) for digital banking username.
            AlRayanUserStoreManager alRayanUserStoreManager = UserManagementUtil.getAlRayanUserManagerService();
            int tenantId = alRayanUserStoreManager.getTenantId();
            String salesforceId = alRayanUserStoreManager.getUserNameForDBPUserName(username, tenantId);

            if (salesforceId == null) {
                salesforceId = "Invalid_User";
            }

            // Do basic authentication.
            doBasicAuthentication(request, context, salesforceId, password);
            context.addEndpointParam("userRememberedName", username);


        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new AuthenticationFailedException(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Do the functionality of the basic authenticator.
     * <p>
     * This is similar to the {@code processAuthenticationResponse} in the basic authenticator. The reason for this is,
     * <p>
     * 1) Actual user name should be the subject of the authentication context.
     * <p>
     * 2) Avoid an extra db call in the userstore by passing the actual username to the method (this is why the
     * method is duplicated).
     *
     * @param request  HTTP request
     * @param context  authentication context
     * @param username salesforce ID
     * @param password user password
     * @throws AuthenticationFailedException thrown when error on authenticating user
     */
    private void doBasicAuthentication(HttpServletRequest request, AuthenticationContext context, String username,
                                       String password) throws AuthenticationFailedException {

        Map<String, Object> authProperties = context.getProperties();
        if (authProperties == null) {
            authProperties = new HashMap<>();
            context.setProperties(authProperties);
        }

        Map<String, String> runtimeParams = getRuntimeParams(context);
        if (runtimeParams != null) {
            String usernameFromContext = runtimeParams.get(FrameworkConstants.JSAttributes.JS_OPTIONS_USERNAME);
            if (usernameFromContext != null && !usernameFromContext.equals(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("Username set for identifier first login: " + usernameFromContext + " and username " +
                            "submitted from login page" + username + " does not match.");
                }
                log.info("Username set for identifier first login: " + usernameFromContext + " and username " +
                        "submitted from login page" + username + " does not match.");
                throw new InvalidCredentialsException("Credential mismatch.");
            }
        }

        authProperties.put(PASSWORD_PROPERTY, password);

        boolean isAuthenticated;
        UserStoreManager userStoreManager;
        // Reset RE_CAPTCHA_USER_DOMAIN thread local variable before the authentication
        IdentityUtil.threadLocalProperties.get().remove(RE_CAPTCHA_USER_DOMAIN);
        // Check the authentication
        try {
            int tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
            UserRealm userRealm = AlRayanBasicAuthenticatorServiceComponent.getRealmService()
                    .getTenantUserRealm(tenantId);
            if (userRealm != null) {
                userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                isAuthenticated = userStoreManager.authenticate(
                        MultitenantUtils.getTenantAwareUsername(username), password);
            } else {
                throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " +
                        tenantId, User.getUserFromUserName(username));
            }
        } catch (IdentityRuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("BasicAuthentication failed while trying to get the tenant ID of the user " + username, e);
            }
            log.info("BasicAuthentication failed while trying to get the tenant ID of the user " + username, e);
            throw new AuthenticationFailedException(e.getMessage(), User.getUserFromUserName(username), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("BasicAuthentication failed while trying to authenticate", e);
            }
            log.info("BasicAuthentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), User.getUserFromUserName(username), e);
        }

        if (!isAuthenticated) {
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to invalid credentials");
            }
            log.info("User authentication failed due to invalid credentials");
            if (IdentityUtil.threadLocalProperties.get().get(RE_CAPTCHA_USER_DOMAIN) != null) {
                username = IdentityUtil.addDomainToName(
                        username, IdentityUtil.threadLocalProperties.get().get(RE_CAPTCHA_USER_DOMAIN).toString());
            }
            IdentityUtil.threadLocalProperties.get().remove(RE_CAPTCHA_USER_DOMAIN);
            throw new InvalidCredentialsException("User authentication failed due to invalid credentials",
                    User.getUserFromUserName(username));
        }


        String tenantDomain = MultitenantUtils.getTenantDomain(username);

        //TODO: user tenant domain has to be an attribute in the AuthenticationContext
        authProperties.put("user-tenant-domain", tenantDomain);

        username = FrameworkUtils.prependUserStoreDomainToName(username);

        if (getAuthenticatorConfig().getParameterMap() != null) {
            String userNameUri = getAuthenticatorConfig().getParameterMap().get("UserNameAttributeClaimUri");
            if (StringUtils.isNotBlank(userNameUri)) {
                boolean multipleAttributeEnable;
                String domain = UserCoreUtil.getDomainFromThreadLocal();
                if (StringUtils.isNotBlank(domain)) {
                    multipleAttributeEnable = Boolean.parseBoolean(userStoreManager.getSecondaryUserStoreManager(domain)
                            .getRealmConfiguration().getUserStoreProperty("MultipleAttributeEnable"));
                } else {
                    multipleAttributeEnable = Boolean.parseBoolean(userStoreManager.
                            getRealmConfiguration().getUserStoreProperty("MultipleAttributeEnable"));
                }
                if (multipleAttributeEnable) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("Searching for UserNameAttribute value for user " + username +
                                    " for claim uri : " + userNameUri);
                        }
                        log.info("Searching for UserNameAttribute value for user " + username +
                                " for claim uri : " + userNameUri);
                        String usernameValue = userStoreManager.
                                getUserClaimValue(MultitenantUtils.getTenantAwareUsername(username), userNameUri, null);
                        if (StringUtils.isNotBlank(usernameValue)) {
                            tenantDomain = MultitenantUtils.getTenantDomain(username);
                            usernameValue = FrameworkUtils.prependUserStoreDomainToName(usernameValue);
                            username = usernameValue + "@" + tenantDomain;
                            if (log.isDebugEnabled()) {
                                log.debug("UserNameAttribute is found for user. Value is :  " + username);
                            }
                            log.info("UserNameAttribute is found for user. Value is :  " + username);
                        }
                    } catch (UserStoreException e) {
                        //ignore  but log in debug
                        if (log.isDebugEnabled()) {
                            log.debug("Error while retrieving UserNameAttribute for user : " + username, e);
                        }
                        log.info("Error while retrieving UserNameAttribute for user : " + username, e);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("MultipleAttribute is not enabled for user store domain : " + domain + " " +
                                "Therefore UserNameAttribute is not retrieved");
                    }
                    log.info("MultipleAttribute is not enabled for user store domain : " + domain + " " +
                            "Therefore UserNameAttribute is not retrieved");
                }
            }
        }

        context.setSubject(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username));
        String rememberMe = request.getParameter("chkRemember");
        context.addEndpointParam("loggedInUser",
                AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username));
        if ("on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

}
