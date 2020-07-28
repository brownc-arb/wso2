package com.alrayan.wso2.workflow.appregistration;

import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.AlRayanMailException;
import com.alrayan.wso2.common.model.User;
import com.alrayan.wso2.common.utils.WorkflowUtils;
import com.alrayan.wso2.mail.MailUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.ApplicationRegistrationWSWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is responsible for handling tasks at the time of TPP app creation.
 *
 * @since 1.0.0
 */
public class AppRegistrationWorkFlow extends ApplicationRegistrationWSWorkflowExecutor {

    private static final Logger log = LoggerFactory.getLogger(AppRegistrationWorkFlow.class);
    private static final String PRODUCTION_KEY_TYPE = "PRODUCTION";
    private static final String SYSTEM_PROPERTY_CARBON_HOME = "${carbon.home}";
    private static final long serialVersionUID = 7056802885463006269L;
    private String onBoardingEmailAddress;
    private String onBoardingEmailPassword;
    private String accountsDepartmentEmailAddress;
    private String bankContactNumber;
    private String bankEmailAddress;
    private String developerPortalURL;
    private String appAcceptedEmailTemplate;
    private String appRegisterEmailTemplate;
    private String appRejectedEmailTemplate;
    private String productionApplicationEmailTemplate;

    /**
     * Returns the PSD2 TPP on boarding email address.
     *
     * @return PSD2 TPP on boarding email address
     */
    public String getOnBoardingEmailAddress() {
        return onBoardingEmailAddress;
    }

    /**
     * Sets the PSD2 TPP on boarding email address.
     *
     * @param onBoardingEmailAddress PSD2 TPP on boarding email address
     */
    public void setOnBoardingEmailAddress(String onBoardingEmailAddress) {
        this.onBoardingEmailAddress = onBoardingEmailAddress;
    }

    /**
     * Returns the PSD2 TPP on boarding email password.
     *
     * @return PSD2 TPP on boarding email password
     */
    public String getOnBoardingEmailPassword() {
        return onBoardingEmailPassword;
    }

    /**
     * Sets the PSD2 TPP on boarding email password.
     *
     * @param onBoardingEmailPassword PSD2 TPP on boarding email password
     */
    public void setOnBoardingEmailPassword(String onBoardingEmailPassword) {
        this.onBoardingEmailPassword = onBoardingEmailPassword;
    }

    /**
     * Returns the accounts department email address.
     *
     * @return accounts department email address
     */
    public String getAccountsDepartmentEmailAddress() {
        return accountsDepartmentEmailAddress;
    }

    /**
     * Sets the accounts department email address.
     *
     * @param accountsDepartmentEmailAddress accounts department email address
     */
    public void setAccountsDepartmentEmailAddress(String accountsDepartmentEmailAddress) {
        this.accountsDepartmentEmailAddress = accountsDepartmentEmailAddress;
    }

    /**
     * Returns the back contact number configured.
     *
     * @return back contact number configured
     */
    public String getBankContactNumber() {
        return bankContactNumber;
    }

    /**
     * Sets the configured bank contact number.
     *
     * @param bankContactNumber back contact number
     */
    public void setBankContactNumber(String bankContactNumber) {
        this.bankContactNumber = bankContactNumber;
    }

    /**
     * Returns the bank email address configured.
     *
     * @return bank email address
     */
    public String getBankEmailAddress() {
        return bankEmailAddress;
    }

    /**
     * Sets the configured bank email address.
     *
     * @param bankEmailAddress bank email address
     */
    public void setBankEmailAddress(String bankEmailAddress) {
        this.bankEmailAddress = bankEmailAddress;
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
     * Returns the email template for app accepted.
     *
     * @return email template for app accepted
     */
    public String getAppAcceptedEmailTemplate() {
        if (appAcceptedEmailTemplate.toLowerCase().startsWith(SYSTEM_PROPERTY_CARBON_HOME)) {
            String carbonHome = System.getProperty("carbon.home");
            return appAcceptedEmailTemplate.replace(SYSTEM_PROPERTY_CARBON_HOME, carbonHome);
        }
        return appAcceptedEmailTemplate;
    }

    /**
     * Sets the email template for app accepted.
     *
     * @param appAcceptedEmailTemplate email template for app accepted
     */
    public void setAppAcceptedEmailTemplate(String appAcceptedEmailTemplate) {
        this.appAcceptedEmailTemplate = appAcceptedEmailTemplate;
    }

    /**
     * Returns the app register email template.
     *
     * @return app register email template
     */
    public String getAppRegisterEmailTemplate() {
        if (appRegisterEmailTemplate.toLowerCase().startsWith(SYSTEM_PROPERTY_CARBON_HOME)) {
            String carbonHome = System.getProperty("carbon.home");
            return appRegisterEmailTemplate.replace(SYSTEM_PROPERTY_CARBON_HOME, carbonHome);
        }
        return appRegisterEmailTemplate;
    }

    /**
     * Sets the app register email template.
     *
     * @param appRegisterEmailTemplate app register email template
     */
    public void setAppRegisterEmailTemplate(String appRegisterEmailTemplate) {
        this.appRegisterEmailTemplate = appRegisterEmailTemplate;
    }

    /**
     * Returns the app rejected email template.
     *
     * @return app rejected email template
     */
    public String getAppRejectedEmailTemplate() {
        if (appRejectedEmailTemplate.toLowerCase().startsWith(SYSTEM_PROPERTY_CARBON_HOME)) {
            String carbonHome = System.getProperty("carbon.home");
            return appRejectedEmailTemplate.replace(SYSTEM_PROPERTY_CARBON_HOME, carbonHome);
        }
        return appRejectedEmailTemplate;
    }

    /**
     * Sets the app rejected email template.
     *
     * @param appRejectedEmailTemplate app rejected email template
     */
    public void setAppRejectedEmailTemplate(String appRejectedEmailTemplate) {
        this.appRejectedEmailTemplate = appRejectedEmailTemplate;
    }

    /**
     * Returns the production application email template.
     *
     * @return production application email template
     */
    public String getProductionApplicationEmailTemplate() {
        if (productionApplicationEmailTemplate.toLowerCase().startsWith(SYSTEM_PROPERTY_CARBON_HOME)) {
            String carbonHome = System.getProperty("carbon.home");
            return productionApplicationEmailTemplate.replace(SYSTEM_PROPERTY_CARBON_HOME, carbonHome);
        }
        return productionApplicationEmailTemplate;
    }

    /**
     * Sets the production application email template.
     *
     * @param productionApplicationEmailTemplate production application email template
     */
    public void setProductionApplicationEmailTemplate(String productionApplicationEmailTemplate) {
        this.productionApplicationEmailTemplate = productionApplicationEmailTemplate;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        String keyType = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getKeyType();
        if (keyType.equals(PRODUCTION_KEY_TYPE)) {
            OAuthApplicationInfo oAuthApplicationInfo =
                    ((ApplicationRegistrationWorkflowDTO) workflowDTO).getAppInfoDTO().getOAuthApplicationInfo();
            JSONObject formData = (JSONObject) oAuthApplicationInfo.getParameter("formData");
            String spCertificate = oAuthApplicationInfo.getParameter("sp_certificate").toString();
            oAuthApplicationInfo.removeParameter("formData");
            Map<String, JSONObject> sortedFormDataMap = sort(formData);
            // Send form data
            log.info("Initiating sending application form data mail notification.");
            sendProductionApplicationEmail(workflowDTO, sortedFormDataMap, spCertificate);

            log.info("Initiating sending new prod access request mail notification.");
            // Send app registration email.
            sendAppRegisterEmail(workflowDTO);
            return super.execute(workflowDTO);
        }
        return super.execute(workflowDTO);
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        if ("REJECTED".equals(workflowDTO.getStatus().toString())) {
            // Rejected mail.
            log.info("Sending workflow rejected email to user for workflow reference :" +
                     workflowDTO.getWorkflowReference());
            String[] splitString = workflowDTO.getWorkflowDescription().split("RejectedReason: ");
            String rejectedReason = "";
            if (splitString.length == 2) {
                rejectedReason = splitString[1];
            }
            sendAppRejectedEmail(workflowDTO, rejectedReason);
        } else if ("APPROVED".equals(workflowDTO.getStatus().toString())) {
            // Approved mail
            log.info("Sending workflow approved email to user for workflow reference :" +
                     workflowDTO.getWorkflowReference());
            sendAppAcceptedEmail(workflowDTO);
        }
        return super.complete(workflowDTO);
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String s) throws WorkflowException {
        return null;
    }

    /**
     * Sorts the form data as per the form order.
     *
     * @param formData form data
     * @return sorted form data
     */
    private Map<String, JSONObject> sort(JSONObject formData) {
        Comparator<Map.Entry<String, JSONObject>> valueComparator =
                Comparator.comparingInt(e -> Integer.parseInt(e.getKey()));
        Set<Map.Entry<String, JSONObject>> entrySet = formData.entrySet();
        return entrySet.stream()
                .sorted(valueComparator)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Sends an email to the user informing that the app registration process has being started.
     *
     * @param workflowDTO work flow object
     * @throws WorkflowException thrown when error on sending user email
     */
    private void sendAppRegisterEmail(WorkflowDTO workflowDTO) throws WorkflowException {
        try {
            if (StringUtils.isEmpty(onBoardingEmailAddress)) {
                log.error("TPP on boarding email address not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_ADDRESS_NOT_DEFINED.getErrorMessageWithCode());
            }

            if (StringUtils.isEmpty(onBoardingEmailPassword)) {
                log.error("TPP on boarding email password not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_PASSWORD_NOT_DEFINED.getErrorMessageWithCode());
            }

            String userName = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getUserName();
            User user = WorkflowUtils.getUser(userName, workflowDTO.getTenantId());
            String developerName = user.getFirstName() + " " + user.getLastName();
            String emailBody = getAppRegistrationStartedEmailBody(developerName, workflowDTO.getWorkflowReference());
            MailUtils.sendEmail(onBoardingEmailAddress, onBoardingEmailPassword, user.getUsername(),
                    "Production Application Verification Process Started",
                    emailBody, new HashMap<>());
        } catch (AlRayanMailException e) {
            log.error("Error on constructing app registration process started email content.", e);
            throw new WorkflowException(AlRayanError.ERROR_CONSTRUCTING_EMAIL.getErrorMessageWithCode(), e);
        } catch (UserStoreException e) {
            log.error("Error on obtaining user for the workflow reference.");
            throw new WorkflowException(AlRayanError.ERROR_ON_OBTAINING_USER_FOR_WORKFLOW_REFERENCE
                    .getErrorMessageWithCode(), e);
        }
    }


    /**
     * Sends an email to the accounts department with the application form.
     *
     * @param workflowDTO work flow object
     * @param formData    application form data
     * @throws WorkflowException thrown when error on sending user email
     */
    private void sendProductionApplicationEmail(WorkflowDTO workflowDTO, Map<String, JSONObject> formData,  String spCertificate)
            throws WorkflowException {
        try {
            if (StringUtils.isEmpty(onBoardingEmailAddress)) {
                log.error("TPP on boarding email address not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_ADDRESS_NOT_DEFINED.getErrorMessageWithCode());
            }

            if (StringUtils.isEmpty(onBoardingEmailPassword)) {
                log.error("TPP on boarding email password not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_PASSWORD_NOT_DEFINED.getErrorMessageWithCode());
            }

            String carbonHome = System.getProperty("carbon.home") + File.separator;
            String attachmentFileRoot = carbonHome + "repository" + File.separator + "deployment" + File.separator +
                                        "server" + File.separator + "jaggeryapps" + File.separator + "store" +
                                        File.separator + "temp" + File.separator +
                                        ((ApplicationRegistrationWorkflowDTO) workflowDTO).getApplication().getName() +
                                        File.separator +
                                        ((ApplicationRegistrationWorkflowDTO) workflowDTO).getKeyType() +
                                        File.separator;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Map<String, String> attachments = formData.entrySet().stream()
                    .filter(e -> e.getValue().get("type").equals("file") &&
                                 StringUtils.isNotEmpty((String) e.getValue().get("value")))
                    .map(e -> {
                        JSONObject jsonObject = e.getValue();
                        String fileName = (String) jsonObject.get("value");
                        jsonObject.replace("value", attachmentFileRoot + fileName);
                        return new AbstractMap.SimpleEntry<>(timestamp.getTime() + "-" + fileName, jsonObject);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) (e.getValue().get("value")),
                            (e1, e2) -> e1, LinkedHashMap::new));
            attachments.put("spCertificate", spCertificate);
            Map<String, JSONObject> filteredFormData = formData.entrySet().stream()
                    .map(e -> {
                        if (e.getValue().get("type").equals("file") &&
                            StringUtils.isNotEmpty((String) e.getValue().get("value"))) {
                            JSONObject jsonObject = e.getValue();
                            String fileName = ((String) jsonObject.get("value")).replace(attachmentFileRoot, "");
                            jsonObject.replace("value", timestamp.getTime() + "-" + fileName);
                            return new AbstractMap.SimpleEntry<>(e.getKey(), jsonObject);
                        } else {
                            return e;
                        }
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));
            String userName = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getUserName();
            User user = WorkflowUtils.getUser(userName, workflowDTO.getTenantId());
            String emailBody = getProductionApplicationForm(workflowDTO, user, filteredFormData);
            MailUtils.sendEmail(onBoardingEmailAddress, onBoardingEmailPassword,
                    accountsDepartmentEmailAddress + "," + user.getUsername(),
                    "Production App On Board Request - " +
                    ((ApplicationRegistrationWorkflowDTO) workflowDTO).getApplication().getName()
                    + " by " + userName, emailBody, attachments);

            // Delete attachments
            for (Map.Entry<String, String> attachment : attachments.entrySet()) {
                String filePath = attachment.getValue();
                Path fileToDeletePath = Paths.get(filePath);
                Files.deleteIfExists(fileToDeletePath);
            }
        } catch (AlRayanMailException e) {
            log.error("Error on constructing application form submission email content.", e);
            throw new WorkflowException(AlRayanError.ERROR_CONSTRUCTING_EMAIL.getErrorMessageWithCode(), e);
        } catch (UserStoreException e) {
            log.error("Error on obtaining user for the workflow reference.");
            throw new WorkflowException(AlRayanError.ERROR_ON_OBTAINING_USER_FOR_WORKFLOW_REFERENCE
                    .getErrorMessageWithCode(), e);
        } catch (IOException e) {
            log.error("Error on cleaning up attachments.");
        }
    }

    /**
     * Sends an email to the user informing that the app is accepted.
     *
     * @param workflowDTO work flow object
     * @throws WorkflowException thrown when error on sending user email
     */
    private void sendAppAcceptedEmail(WorkflowDTO workflowDTO) throws WorkflowException {
        try {
            if (StringUtils.isEmpty(onBoardingEmailAddress)) {
                log.error("TPP on boarding email address not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_ADDRESS_NOT_DEFINED.getErrorMessageWithCode());
            }

            if (StringUtils.isEmpty(onBoardingEmailPassword)) {
                log.error("TPP on boarding email password not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_PASSWORD_NOT_DEFINED.getErrorMessageWithCode());
            }

            String userName = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getUserName();
            if (StringUtils.isEmpty(userName)) {
                userName = workflowDTO.getWorkflowDescription().split("application creator - ")[1]
                        .split(" ")[0];
            }
            User user = WorkflowUtils.getUser(userName, workflowDTO.getTenantId());
            String emailBody = getAppAcceptedEmailBody(user.getFirstName() + " " + user.getLastName());
            MailUtils.sendEmail(onBoardingEmailAddress, onBoardingEmailPassword, user.getUsername(),
                    "Production Application Successfully On Boarded", emailBody, new HashMap<>());
        } catch (AlRayanMailException e) {
            log.error("Error on constructing app accepted email content.", e);
            throw new WorkflowException(AlRayanError.ERROR_CONSTRUCTING_EMAIL.getErrorMessageWithCode(), e);
        } catch (UserStoreException e) {
            log.error("Error on obtaining user for the workflow reference.");
            throw new WorkflowException(AlRayanError.ERROR_ON_OBTAINING_USER_FOR_WORKFLOW_REFERENCE
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Sends an email to the user informing that the app is rejected.
     *
     * @param workflowDTO    work flow object
     * @param rejectedReason app rejected reason
     * @throws WorkflowException thrown when error on sending user email
     */
    private void sendAppRejectedEmail(WorkflowDTO workflowDTO, String rejectedReason) throws WorkflowException {
        try {
            if (StringUtils.isEmpty(onBoardingEmailAddress)) {
                log.error("TPP on boarding email address not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_ADDRESS_NOT_DEFINED.getErrorMessageWithCode());
            }

            if (StringUtils.isEmpty(onBoardingEmailPassword)) {
                log.error("TPP on boarding email password not defined in the workflow configuration.");
                throw new WorkflowException(
                        AlRayanError.TPP_ON_BOARDING_EMAIL_PASSWORD_NOT_DEFINED.getErrorMessageWithCode());
            }

            String userName = ((ApplicationRegistrationWorkflowDTO) workflowDTO).getUserName();
            if (StringUtils.isEmpty(userName)) {
                userName = workflowDTO.getWorkflowDescription().split("application creator - ")[1]
                        .split(" ")[0];
            }
            User user = WorkflowUtils.getUser(userName, workflowDTO.getTenantId());
            String emailBody = getAppRejectedEmailBody(user.getFirstName() + " " + user.getLastName(),
                    rejectedReason);
            MailUtils.sendEmail(onBoardingEmailAddress, onBoardingEmailPassword, user.getUsername(),
                    "Production Application Rejected", emailBody,
                    new HashMap<>());
        } catch (AlRayanMailException e) {
            log.error("Error on constructing app rejected email content.", e);
            throw new WorkflowException(AlRayanError.ERROR_CONSTRUCTING_EMAIL.getErrorMessageWithCode(), e);
        } catch (UserStoreException e) {
            log.error("Error on obtaining user for the workflow reference.");
            throw new WorkflowException(AlRayanError.ERROR_ON_OBTAINING_USER_FOR_WORKFLOW_REFERENCE
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Constructs the production application form.
     *
     * @param workflowDTO work flow object
     * @param formData    form data to construct the production application form
     * @return production application body content
     * @throws WorkflowException thrown when error on constructing the production application form
     */
    private String getProductionApplicationForm(WorkflowDTO workflowDTO, User user, Map<String, JSONObject> formData)
            throws WorkflowException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("userEmail", user.getUsername());
            context.put("userName", user.getFirstName() + " " + user.getLastName());
            context.put("applicationName",
                    ((ApplicationRegistrationWorkflowDTO) workflowDTO).getApplication().getName());
            context.put("applicationDescription",
                    ((ApplicationRegistrationWorkflowDTO) workflowDTO).getApplication().getDescription());
            context.put("reference", workflowDTO.getWorkflowReference());
            context.put("formData", formData);

            StringWriter stringWriter = new StringWriter();
            Reader templateReader = new BufferedReader(new FileReader(getProductionApplicationEmailTemplate()));
            Velocity.evaluate(context, stringWriter, "log tag name", templateReader);
            return stringWriter.toString();
        } catch (Exception e) {
            String message = "Error on constructing the production application body via template.";
            log.error(message, e);
            throw new WorkflowException(message, e);
        }
    }

    /**
     * Constructs the email body for app registration started.
     *
     * @param username  name to be addressed in the email
     * @param reference workflow reference
     * @return email body content
     * @throws WorkflowException thrown when error on constructing the email body
     */
    private String getAppRegistrationStartedEmailBody(String username, String reference) throws WorkflowException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("username", username);
            context.put("bankContactNumber", bankContactNumber);
            context.put("bankEmailAddress", bankEmailAddress);
            context.put("reference", reference);

            StringWriter stringWriter = new StringWriter();
            Reader templateReader = new BufferedReader(new FileReader(getAppRegisterEmailTemplate()));
            Velocity.evaluate(context, stringWriter, "log tag name", templateReader);
            return stringWriter.toString();
        } catch (Exception e) {
            String message = "Error on constructing the 'app registration started email' body via template.";
            log.error(message, e);
            throw new WorkflowException(message, e);
        }
    }

    /**
     * Constructs the email body for app accepted.
     *
     * @param username name to be addressed in the email
     * @return email body content
     * @throws WorkflowException thrown when error on constructing the email body
     */
    private String getAppAcceptedEmailBody(String username) throws WorkflowException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("username", username);
            context.put("developerPortalURL", getDeveloperPortalURL());

            StringWriter stringWriter = new StringWriter();
            Reader templateReader = new BufferedReader(new FileReader(getAppAcceptedEmailTemplate()));
            Velocity.evaluate(context, stringWriter, "log tag name", templateReader);
            return stringWriter.toString();
        } catch (Exception e) {
            String message = "Error on constructing the 'app accepted email' body via template.";
            log.error(message, e);
            throw new WorkflowException(message, e);
        }
    }

    /**
     * Constructs the email body for app rejected.
     *
     * @param username           name to be addressed in the email
     * @param reasonForRejection reason for app rejection
     * @return email body content
     * @throws WorkflowException thrown when error on constructing the email body
     */
    private String getAppRejectedEmailBody(String username, String reasonForRejection) throws WorkflowException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("username", username);
            context.put("bankContactNumber", bankContactNumber);
            context.put("bankEmailAddress", bankEmailAddress);
            context.put("reasonForRejection", reasonForRejection);

            StringWriter stringWriter = new StringWriter();
            Reader templateReader = new BufferedReader(new FileReader(getAppRejectedEmailTemplate()));
            Velocity.evaluate(context, stringWriter, "log tag name", templateReader);
            return stringWriter.toString();
        } catch (Exception e) {
            String message = "Error on constructing the 'app rejected email' body via template.";
            log.error(message, e);
            throw new WorkflowException(message, e);
        }
    }
}
