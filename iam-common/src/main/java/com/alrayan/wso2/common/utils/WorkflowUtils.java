package com.alrayan.wso2.common.utils;

import com.alrayan.wso2.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Utility class for managing workflows.
 *
 * @since 1.0.0
 */
public class WorkflowUtils {

    private static Logger log = LoggerFactory.getLogger(WorkflowUtils.class);

    /**
     * Adds the given user to the given user role.
     *
     * @param username username to add role to
     * @param tenantId tenant id
     * @param roleName role name to be added
     * @throws UserStoreException thrown when error on adding user to the user role
     */
    public static void addUserToRole(String username, int tenantId, String roleName)
            throws UserStoreException {
        RealmService realmService = getRealmService(username);
        addUserToRole(username, tenantId, roleName, realmService);
    }

    /**
     * Adds the given user to the given user role.
     *
     * @param username     username to add role to
     * @param tenantDomain tenant domain
     * @param roleName     role name to be added
     * @throws UserStoreException thrown when error on adding user to the user role
     */
    public static void addUserToRole(String username, String tenantDomain, String roleName)
            throws UserStoreException {
        RealmService realmService = getRealmService(username);
        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        addUserToRole(username, tenantId, roleName, realmService);
    }

    /**
     * Returns the TPP user information.
     *
     * @param username work flow username
     * @param tenantId tenant ID
     * @return user TPP user info
     * @throws UserStoreException thrown when error on obtaining user information for workflow reference
     */
    public static User getUser(String username, int tenantId) throws UserStoreException {
        RealmService realmService = getRealmService(username);
        return getUser(username, tenantId, realmService);
    }

    /**
     * Returns the TPP user information.
     *
     * @param username     work flow username
     * @param tenantDomain tenant domain
     * @return user TPP user info
     * @throws UserStoreException thrown when error on obtaining user information for workflow reference
     */
    public static User getUser(String username, String tenantDomain) throws UserStoreException {
        RealmService realmService = getRealmService(username);
        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        return getUser(username, tenantId, realmService);

    }

    /**
     * Returns the TPP user information.
     *
     * @param username     work flow username
     * @param tenantId     tenant ID
     * @param realmService realm service
     * @return user TPP user info
     * @throws UserStoreException thrown when error on obtaining user information for workflow reference
     */
    private static User getUser(String username, int tenantId, RealmService realmService) throws UserStoreException {
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            User user = new User();
            user.setFirstName(userStoreManager.getUserClaimValue(tenantAwareUserName,
                    "http://wso2.org/claims/givenname", null));
            user.setLastName(userStoreManager.getUserClaimValue(tenantAwareUserName,
                    "http://wso2.org/claims/lastname", null));
            user.setUsername(userStoreManager.getUserClaimValue(tenantAwareUserName,
                    "http://wso2.org/claims/emailaddress", null));
            user.setRolesEnrolled(userStoreManager.getUserClaimValue(tenantAwareUserName,
                    "http://wso2.org/claims/pspCompetentAuthorityRole", null));
            return user;
        } catch (UserStoreException e) {
            String message = "Error while getting user: " + username + " during workflow.";
            throw new UserStoreException(message, e);
        }
    }

    /**
     * Returns the realm service for the given username.
     *
     * @param username username
     * @return realm service
     * @throws UserStoreException error in getting the realm service
     */
    private static RealmService getRealmService(String username) throws UserStoreException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setUsername(username);
        RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String message = "RealmService is not initialized.";
            log.error(message);
            throw new UserStoreException(message);
        }
        return realmService;
    }

    /**
     * Adds the given user to the given user role.
     *
     * @param username     username to add role to
     * @param tenantId     tenant id
     * @param roleName     role name to be added
     * @param realmService realm service
     * @throws UserStoreException thrown when error on adding user to the user role
     */
    private static void addUserToRole(String username, int tenantId, String roleName, RealmService realmService)
            throws UserStoreException {
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            userStoreManager.updateRoleListOfUser(username, new String[]{}, new String[]{roleName});
        } catch (UserStoreException e) {
            String message = "Error while engaging role " + roleName + " to user " + username;
            throw new UserStoreException(message, e);
        }
    }
}
