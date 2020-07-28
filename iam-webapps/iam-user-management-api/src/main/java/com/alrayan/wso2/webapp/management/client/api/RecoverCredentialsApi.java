package com.alrayan.wso2.webapp.management.client.api;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import com.alrayan.wso2.user.core.util.UserManagementUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.RecoveryScenarios;
import org.wso2.carbon.identity.recovery.RecoverySteps;
import org.wso2.carbon.identity.recovery.model.UserRecoveryData;
import org.wso2.carbon.identity.recovery.store.JDBCRecoveryDataStore;
import org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.recovery.util.Utils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This API is responsible for generating the confirmation key for reset password.
 *
 * @since 1.0.0
 */
public class RecoverCredentialsApi {

    private static final Log log = LogFactory.getLog(RecoverCredentialsApi.class);

    /**
     * Returns the reset password secret key.
     *
     * @param username username related to the reset password request
     * @throws IdentityRecoveryException thrown when error on getting the password recovery secret key
     */
    public String getRecoverPasswordConfirmationKey(String username) throws IdentityRecoveryException {
        String userName = IdentityUtil.addDomainToName(username,
                AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue());
        User identityUser = User.getUserFromUserName(userName);
        if (StringUtils.isBlank(identityUser.getTenantDomain())) {
            identityUser.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            log.info("SendRecoveryNotification :Tenant domain is not in the request. set to default for user : " +
                     identityUser.getUserName());
        }

        if (StringUtils.isBlank(identityUser.getUserStoreDomain())) {
            identityUser.setUserStoreDomain(IdentityUtil.getPrimaryDomainName());
            log.info("SendRecoveryNotification :User store domain is not in the request. set to default for user : " +
                     identityUser.getUserName());
        }

        UserRecoveryDataStore userRecoveryDataStore = JDBCRecoveryDataStore.getInstance();
        boolean isUserExists = isUserExists(identityUser);
        if (!isUserExists) {
            log.error("User does not available in the system.");
            throw new IdentityRecoveryException(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode());
        }

        if (Utils.isAccountDisabled(identityUser)) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_DISABLED_ACCOUNT, identityUser.getUserName());
        } else if (Utils.isAccountLocked(identityUser)) {
            throw Utils.handleClientException(
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_LOCKED_ACCOUNT, identityUser.getUserName());
        }

        userRecoveryDataStore.invalidate(identityUser);

        String secretKey = UUIDGenerator.generateUUID();
        UserRecoveryData recoveryDataDO = new UserRecoveryData(identityUser, secretKey,
                RecoveryScenarios.NOTIFICATION_BASED_PW_RECOVERY, RecoverySteps.UPDATE_PASSWORD);
        userRecoveryDataStore.store(recoveryDataDO);

        return secretKey;
    }

    /**
     * Returns the digital banking username for the given salesforce ID.
     *
     * @param salesforceID salesforce ID
     * @return digital banking username
     * @throws IdentityRecoveryException thrown when error on obtaining digital banking username
     */
    public String getUsernameForSalesforceId(String salesforceID) throws IdentityRecoveryException {
        try {
            AlRayanUserStoreManager userStoreManager = UserManagementUtil.getAlRayanUserManagerService();
            return userStoreManager.getDBPUserNameForUsername(salesforceID, userStoreManager.getTenantId());
        } catch (UserStoreException e) {
            throw new IdentityRecoveryException(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Returns whether the user exists or not.
     *
     * @param user user
     * @return {@code true} if the user exists, {@code false} otherwise
     * @throws IdentityRecoveryServerException thrown when error on checking user existence
     */
    private boolean isUserExists(User user) throws IdentityRecoveryServerException {
        try {
            UserStoreManager userStoreManager = UserManagementUtil.getAlRayanUserManagerService();
            return userStoreManager.isExistingUser(user.getUserName());
        } catch (UserStoreException e) {
            throw new IdentityRecoveryServerException(AlRayanError.INTERNAL_SERVER_ERROR_WITH_USERSTORE
                    .getErrorMessageWithCode(), e);
        }
    }
}
