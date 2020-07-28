package com.alrayan.wso2.user.core.util;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * Utility class to handle common functionality related to web application.
 *
 * @since 1.0.0
 */
public class UserManagementUtil {

    /**
     * Returns the {@link UserStoreManager} instance for the super tenant.
     *
     * @return {@link UserStoreManager} instance for the super tenant
     * @throws UserStoreException throws when an error occurred while retrieving UserStoreManager
     */
    public static UserStoreManager getUserManagerService() throws org.wso2.carbon.user.api.UserStoreException {
        PrivilegedCarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        UserRealmService realmService = (UserRealmService) context.getOSGiService(UserRealmService.class, null);
        return (UserStoreManager) realmService
                .getTenantUserRealm(CarbonConstants.SUPER_TENANT_ID).getUserStoreManager();
    }

    /**
     * Returns the {@link AlRayanUserStoreManager} instance for the super tenant.
     *
     * @return {@link AlRayanUserStoreManager} instance for the super tenant
     * @throws UserStoreException throws when an error occurred while retrieving AlRayanUserStoreManager
     */
    public static AlRayanUserStoreManager getAlRayanUserManagerService()
            throws org.wso2.carbon.user.api.UserStoreException {
        return (AlRayanUserStoreManager) (getUserManagerService()
                .getSecondaryUserStoreManager(AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue()));
    }

    /**
     * Returns whether the user exists.
     *
     * @param username salesforce ID
     * @return {@code true} if the user exists, {@code false} otherwise
     * @throws org.wso2.carbon.user.api.UserStoreException thrown when error on checking user existence
     */
    public static boolean isUserExist(String username) throws org.wso2.carbon.user.api.UserStoreException {
        String usernameWithDomain = UserCoreUtil.addDomainToName(username,
                AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue());
        AlRayanUserStoreManager userStoreManager = getAlRayanUserManagerService();
        return userStoreManager.isExistingUser(usernameWithDomain);
    }

    /**
     * Returns whether the DBP user exists or not.
     *
     * @param dbpUsername Digital banking username
     * @return {@code true} if the digital banking user exists, {@code false} otherwise
     * @throws org.wso2.carbon.user.api.UserStoreException thrown when error on checking digital banking user existence
     */
    public static boolean isDBPUserExist(String dbpUsername)
            throws org.wso2.carbon.user.api.UserStoreException {
        AlRayanUserStoreManager userStoreManager = getAlRayanUserManagerService();
        int tenantId = userStoreManager.getTenantId();
        return userStoreManager.isDBPUserExist(dbpUsername, tenantId);
    }


    public static boolean canAuthenticate(String username, String password)
            throws org.wso2.carbon.user.api.UserStoreException {
        boolean isAuthenticated = getUserManagerService().authenticate(username, password);
        return isAuthenticated;
    }


    /**
     * Creates an authenticated user instance for the given user name.
     *
     * @param username        username
     * @param userStoreDomain user store domain
     * @return authenticated user instance
     */
    public static AuthenticatedUser buildAuthenticatedUser(String username, String userStoreDomain,
                                                           String tenantDomain, boolean isFederated) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserStoreDomain(userStoreDomain);
        authenticatedUser.setTenantDomain(tenantDomain);
        authenticatedUser.setUserName(username);
        authenticatedUser.setFederatedUser(isFederated);
        authenticatedUser.setAuthenticatedSubjectIdentifier(UserCoreUtil.addDomainToName(username, userStoreDomain));
        return authenticatedUser;
    }
}
