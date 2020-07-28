package com.alrayan.wso2.webapp.apimuserrecovery.client;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.model.User;
import com.alrayan.wso2.common.utils.WorkflowUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.user.api.UserStoreException;

import java.rmi.RemoteException;

/**
 * This class is responsible for handling operations with {@link UserInformationRecoveryServiceStub}.
 *
 * @since 1.0.0
 */
public class UserInformationRecoveryClient {

    private UserInformationRecoveryServiceStub stub;
    private static final Logger log = LoggerFactory.getLogger(UserInformationRecoveryClient.class);

    /**
     * Creates an instance of {@link UserInformationRecoveryClient}.
     *
     * @throws APIManagementException throws when error on initiating instance
     */
    public UserInformationRecoveryClient() throws APIManagementException {
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String configuredServerURL = AlRayanConfiguration.USER_INFO_RECOVERY_SERVER_URL.getValue();
        String configuredUsername = AlRayanConfiguration.USER_INFO_RECOVERY_SERVER_USERNAME.getValue();
        String configuredPassword = AlRayanConfiguration.USER_INFO_RECOVERY_SERVER_PASSWORD.getValue();
        String serverUrl = configuredServerURL.toLowerCase().startsWith("$config{") ?
                           config.getFirstProperty(StringUtils.substring(configuredServerURL, 8, -1)) :
                           StringUtils.substring(configuredServerURL, 8, -1);
        String username = configuredUsername.toLowerCase().startsWith("$config{") ?
                          config.getFirstProperty(StringUtils.substring(configuredUsername, 8, -1)) :
                          StringUtils.substring(configuredUsername, 8, -1);
        String password = configuredPassword.toLowerCase().startsWith("$config{") ?
                          config.getFirstProperty(StringUtils.substring(configuredPassword, 8, -1)) :
                          StringUtils.substring(configuredPassword, 8, -1);
        if (StringUtils.isEmpty(serverUrl)) {
            throw new APIManagementException("Required connection details for the key management server not provided");
        } else {
            try {
                this.stub = new UserInformationRecoveryServiceStub(serverUrl + "UserInformationRecoveryService");
                Options option = this.stub._getServiceClient().getOptions();
                HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
                auth.setUsername(username);
                auth.setPassword(password);
                auth.setPreemptiveAuthentication(true);
                option.setProperty("_NTLM_DIGEST_BASIC_AUTHENTICATION_", auth);
                option.setManageSession(true);
            } catch (AxisFault fault) {
                throw new APIManagementException(
                        "Error while initializing the User Information Recovery admin service stub", fault);
            }
        }
    }

    /**
     * Returns the generated captcha.
     *
     * @return the generated captcha
     * @throws AxisFault thrown when error on generating captcha
     */
    public CaptchaInfoBean generateCaptcha() throws AxisFault {
        try {
            return stub.getCaptcha();
        } catch (RemoteException | UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            String message = "Error on obtaining captcha.";
            log.error(message, e);
            throw new AxisFault(message, e);
        }
    }

    /**
     * Confirms the user self registration.
     *
     * @param userName     user name
     * @param code         confirmation code
     * @param captcha      captcha
     * @param tenantDomain tenant domain
     * @return user self registration confirmation result
     * @throws RemoteException thrown when self registration error
     */
    public VerificationBean confirmUserSelfRegistration(String userName, String code, CaptchaInfoBean captcha,
                                                        String tenantDomain) throws RemoteException {
        try {
            VerificationBean verificationBean = stub.confirmUserSelfRegistration(userName, code, captcha, tenantDomain);
            if (!verificationBean.isErrorSpecified()) {
                User user = WorkflowUtils.getUser(userName, tenantDomain);
                addUserToRoles(user, tenantDomain);
            }
            return verificationBean;
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            String message = "Error on confirming self registration.";
            log.error(message, e);
            throw new RemoteException(message, e);
        } catch (UserStoreException | WorkflowException e) {
            String message = "Error on obtaining user information for the given username.";
            log.error(message, e);
            throw new RemoteException(message, e);
        }
    }

    /**
     * Adds the roles to the user.
     *
     * @param user         user to add roles to
     * @param tenantDomain tenant domain of the user
     * @throws WorkflowException thrown when error on adding roles to the user
     */
    private void addUserToRoles(User user, String tenantDomain) throws WorkflowException {
        try {
            String[] roleList = user.getRolesEnrolled().split(",");
            for (String role : roleList) {
                switch (role) {
                    case "AISP":
                        WorkflowUtils.addUserToRole(user.getUsername(), tenantDomain,
                                AlRayanConfiguration.USER_INFO_RECOVERY_AISP_ROLE_NAME.getValue());
                        break;
                    case "PISP":
                        WorkflowUtils.addUserToRole(user.getUsername(), tenantDomain,
                                AlRayanConfiguration.USER_INFO_RECOVERY_PISP_ROLE_NAME.getValue());
                        break;
                    case "PIISP":
                        WorkflowUtils.addUserToRole(user.getUsername(), tenantDomain,
                                AlRayanConfiguration.USER_INFO_RECOVERY_PIISP_ROLE_NAME.getValue());
                        break;
                }
            }
            WorkflowUtils.addUserToRole(user.getUsername(), tenantDomain, "Internal/subscriber");
        } catch (UserStoreException e) {
            log.error(e.getMessage());
            throw new WorkflowException(AlRayanError.ERROR_ON_ENGAGING_USER_WITH_ROLES.getErrorMessageWithCode(), e);
        }
    }
}
