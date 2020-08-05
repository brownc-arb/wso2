package com.alrayan.wso2.workflow.signup;

import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.AlRayanMailException;
import com.alrayan.wso2.common.model.User;
import com.alrayan.wso2.common.utils.WorkflowUtils;
import com.alrayan.wso2.mail.MailUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.UserSignUpSimpleWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible for sending an email during the TPP registration.
 *
 * @since 1.0.0
 */
public class UserSignUpEmailSender extends UserSignUpSimpleWorkflowExecutor {

    private static final long serialVersionUID = 3256337508511477587L;
    private static final String SYSTEM_PROPERTY_CARBON_HOME = "${carbon.home}";
    private static Logger log = LoggerFactory.getLogger(UserSignUpEmailSender.class);
    private String onboardingEmailAddress;
    private String onboardingEmailPassword;
    private String developerPortalURL;
    private String userRegisterSuccessEmailTemplate;
    private String aispRoleName;
    private String pispRoleName;
    private String piispRoleName;

    /**
     * Returns the PSD2 TPP on boarding email address.
     *
     * @return PSD2 TPP on boarding email address
     */
    public String getOnboardingEmailAddress() {
        return onboardingEmailAddress;
    }

    /**
     * Sets the PSD2 TPP on boarding email address.
     *
     * @param onboardingEmailAddress PSD2 TPP on boarding email address
     */
    public void setOnboardingEmailAddress(String onboardingEmailAddress) {
        this.onboardingEmailAddress = onboardingEmailAddress;
    }

    /**
     * Returns the PSD2 TPP on boarding email password.
     *
     * @return PSD2 TPP on boarding email password
     */
    public String getOnboardingEmailPassword() {
        return onboardingEmailPassword;
    }

    /**
     * Sets the PSD2 TPP on boarding email password.
     *
     * @param onboardingEmailPassword PSD2 TPP on boarding email password
     */
    public void setOnboardingEmailPassword(String onboardingEmailPassword) {
        this.onboardingEmailPassword = onboardingEmailPassword;
    }

    /**
     * Returns the developer portal URL.
     *
     * @return developer portal URL
     */
    public String getDeveloperPortalURL() {
        return developerPortalURL != null ? developerPortalURL : "https://localhost:9443/store/site/pages/login.jag";
    }

    /**
     * Sets the developer portal URL.
     *
     * @param developerPortalURL developer portal URL
     */
    public void setDeveloperPortalURL(String developerPortalURL) {
        this.developerPortalURL = developerPortalURL;
    }

    /**
     * Returns the user registration successful email template path.
     *
     * @return user registration successful email template
     */
    public String getUserRegisterSuccessEmailTemplate() {
        if (userRegisterSuccessEmailTemplate.toLowerCase().startsWith(SYSTEM_PROPERTY_CARBON_HOME)) {
            String carbonHome = System.getProperty("carbon.home");
            return userRegisterSuccessEmailTemplate.replace(SYSTEM_PROPERTY_CARBON_HOME, carbonHome);
        }
        return userRegisterSuccessEmailTemplate;
    }

    /**
     * Sets the user registration successful email template.
     *
     * @param userRegisterSuccessEmailTemplate user registration successful email template
     */
    public void setUserRegisterSuccessEmailTemplate(String userRegisterSuccessEmailTemplate) {
        this.userRegisterSuccessEmailTemplate = userRegisterSuccessEmailTemplate;
    }

    /**
     * Returns the AISP role name.
     *
     * @return AISP role name
     */
    public String getAispRoleName() {
        return aispRoleName != null ? aispRoleName : "Internal/aispRole";
    }

    /**
     * Sets the AISP role name.
     *
     * @param aispRoleName AISP role name
     */
    public void setAispRoleName(String aispRoleName) {
        this.aispRoleName = aispRoleName;
    }

    /**
     * Returns the PISP role name.
     *
     * @return PISP role name
     */
    public String getPispRoleName() {
        return pispRoleName != null ? pispRoleName : "Internal/pispRole";
    }

    /**
     * Sets the PISP role name.
     *
     * @param pispRoleName PISP role name
     */
    public void setPispRoleName(String pispRoleName) {
        this.pispRoleName = pispRoleName;
    }

    /**
     * Returns the PIISP role name.
     *
     * @return PIISP role name
     */
    public String getPiispRoleName() {
        return piispRoleName != null ? piispRoleName : "Internal/piispRole";
    }

    /**
     * Sets the PIISP role name.
     *
     * @param piispRoleName PIISP role name
     */
    public void setPiispRoleName(String piispRoleName) {
        this.piispRoleName = piispRoleName;
    }

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_USER_SIGNUP;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String args) throws WorkflowException {
        return null;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        try {
            log.debug("Sending registration completed email to user.");
            User user = WorkflowUtils.getUser(workflowDTO.getWorkflowReference(), workflowDTO.getTenantId());
            addUserToRoles(user, workflowDTO.getTenantId());
            sendEmail(user);
            log.debug("Registration success email sent.");
            return super.execute(workflowDTO);
        } catch (UserStoreException e) {
            log.error("Error on obtaining user for the workflow reference.");
            throw new WorkflowException(AlRayanError.ERROR_ON_OBTAINING_USER_FOR_WORKFLOW_REFERENCE
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Sends an email to the user informing that the registration is a success.
     *
     * @param user user to send mail to
     * @throws WorkflowException thrown when error on sending user email
     */
    private void sendEmail(User user) throws WorkflowException {
        try {
            if (StringUtils.isEmpty(onboardingEmailAddress)) {
                log.error("TPP on boarding email address not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_ADDRESS_NOT_DEFINED.getErrorMessageWithCode());
            }

            if (StringUtils.isEmpty(onboardingEmailPassword)) {
                log.error("TPP on boarding email password not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_PASSWORD_NOT_DEFINED.getErrorMessageWithCode());
            }
            String userEmailAddress = user.getUsername();
            MailUtils.sendEmail(onboardingEmailAddress, onboardingEmailPassword, userEmailAddress,
                    "Registration Successful",
                    getEmailBody(user.getFirstName() + " " + user.getLastName()), new HashMap<>());
        } catch (AlRayanMailException e) {
            log.error("Error on constructing registration successful email content.", e);
            throw new WorkflowException(AlRayanError.ERROR_CONSTRUCTING_EMAIL.getErrorMessageWithCode(), e);
        }
    }

    /**
     * Adds the roles to the user.
     *
     * @param user     user to add roles to
     * @param tenantId tenant id of the user
     * @throws WorkflowException thrown when error on adding roles to the user
     */
    private void addUserToRoles(User user, int tenantId) throws WorkflowException {
        try {
            String[] roleList = user.getRolesEnrolled().split(",");
            for (String role : roleList) {
                switch (role) {
                    case "AISP":
                        WorkflowUtils.addUserToRole(user.getUsername(), tenantId, getAispRoleName());
                        break;
                    case "PISP":
                        WorkflowUtils.addUserToRole(user.getUsername(), tenantId, getPispRoleName());
                        break;
                    case "PIISP":
                        WorkflowUtils.addUserToRole(user.getUsername(), tenantId, getPiispRoleName());
                        break;
                }
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage());
            throw new WorkflowException(AlRayanError.ERROR_ON_ENGAGING_USER_WITH_ROLES.getErrorMessageWithCode(), e);
        }
    }

    /**
     * Constructs the email body and returns the string of the email body.
     *
     * @param username name to be addressed in the email
     * @return email body content
     * @throws WorkflowException thrown when error on constructing the email body
     */
    private String getEmailBody(String username) throws WorkflowException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("username", username);
            context.put("developerPortalURL", getDeveloperPortalURL());

            StringWriter stringWriter = new StringWriter();
            Reader templateReader = new BufferedReader(new FileReader(getUserRegisterSuccessEmailTemplate()));
            Velocity.evaluate(context, stringWriter, "log tag name", templateReader);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new WorkflowException("Error on constructing the registration email success body via template.", e);
        }
    }
}
