package com.alrayan.wso2.keymanager;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanConstants;
import com.alrayan.wso2.common.AlRayanError;
import com.wso2.finance.open.banking.common.exception.CertificateValidationException;
import com.wso2.finance.open.banking.common.identity.IdentityConstants;
import com.wso2.finance.open.banking.common.util.CommonConstants;
import com.wso2.finance.open.banking.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.finance.open.banking.eidas.certificate.extractor.common.model.CertificateContent;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.common.model.script.xsd.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceStub;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is to override to default key manager behavior as required by PSD2 standard. When creating an application
 * in external key-manager, this implementation creates the application with 2FA engaged as user name password
 * verification / ARB federated authentication followed by VASCO cronto code validation.
 *
 * @since 1.0.0
 */
public class AlRayanSCAKeyManagerClient extends AMDefaultKeyManagerImpl {

    private static final Logger log = LoggerFactory.getLogger(AlRayanSCAKeyManagerClient.class);

    private static final String OAUTH_USERNAME = "username";
    private static final String OAUTH_KEY_TYPE = "key_type";
    private static final String AUTHENTICATION_ADMIN_SERVICE = "AuthenticationAdmin";
    private static final String USER_ADMIN_SERVICE = "UserAdmin";
    private static final String ROLE_APPLICATION = "Application";
    private static final String IDENTITY_APPLICATION_MGT_SERVICE = "IdentityApplicationManagementService";
    private static final String IDENTITY_PROVIDER_MGT_SERVICE = "IdentityProviderMgtService";
    private static final String SANDBOX_ENV = "SANDBOX";

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {
        // Create app and get the app name.
        String applicationName = oauthAppRequest.getOAuthApplicationInfo().getClientName();
        OAuthApplicationInfo oAuthApplicationInfo = super.createApplication(oauthAppRequest);

        String userId = (String) oAuthApplicationInfo.getParameter(OAUTH_USERNAME);
        String userName = MultitenantUtils.getTenantAwareUsername(userId);
        String keyType = (String) oAuthApplicationInfo.getParameter(OAUTH_KEY_TYPE);
        if (keyType != null) {
            applicationName = APIUtil.replaceEmailDomain(userName) + "_" + applicationName + "_" + keyType;
            log.debug("Application created with name : " + applicationName);
        }

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String backendServerURL = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        String adminUsername = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String adminPassword = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        String sessionCookie = getCookieString(adminUsername, adminPassword, backendServerURL);

        // Add/remove application roles.
        addRemoveRolesOfUser(adminUsername, userName, backendServerURL, sessionCookie, applicationName);

        // Set APIM options.
        IdentityApplicationManagementServiceStub stub = setAPIMMGTOptions(backendServerURL, sessionCookie);

        // Set IAM options.
        setIDPMGTOptions(backendServerURL, sessionCookie);

        ServiceProvider serviceProvider = getApplicationServiceProvider(stub, applicationName);
        setAuthenticationSteps(serviceProvider, stub, keyType);

        // Set certs to application.
        setApplicationCerts(oAuthApplicationInfo, applicationName, stub, serviceProvider);

        // Update application.
        updateApplication(stub, serviceProvider);
        log.info("Application is successfully created with the appropriate authenticators.", applicationName);
        return oAuthApplicationInfo;
    }

    /**
     * Sets the authentication steps for the service provider.
     *
     * @param serviceProvider application service provider
     * @param keyType         environment (ex: sandbox or production)
     * @throws APIManagementException thrown when error on setting up authentication steps
     */
    private void setAuthenticationSteps(ServiceProvider serviceProvider,
                                        IdentityApplicationManagementServiceStub stub, String keyType)
            throws APIManagementException {
        AuthenticationStep[] authenticationSteps = new AuthenticationStep[2];
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig.setAuthenticationType("flow");

        // Step 1 - Al Rayan basic authentication
        LocalAuthenticatorConfig localBasicAuthenticatorConfig = new LocalAuthenticatorConfig();
        LocalAuthenticatorConfig[] localBasicAuthenticatorConfigs = new LocalAuthenticatorConfig[1];

        AuthenticationStep authStep1 = new AuthenticationStep();


        localBasicAuthenticatorConfig.setDisplayName(AlRayanConstants.ARB_BASIC_AUTHENTICATOR_FRIENDLY_NAME);
        localBasicAuthenticatorConfig.setName(AlRayanConstants.ARB_BASIC_AUTHENTICATOR_NAME);

        localBasicAuthenticatorConfig.setEnabled(true);
        localBasicAuthenticatorConfigs[0] = localBasicAuthenticatorConfig;

        // Add federated authenticators.
        IdentityProvider[] identityProviders = getFederatedAuthenticators(stub, keyType);

        authStep1.setStepOrder(1);
        authStep1.setLocalAuthenticatorConfigs(localBasicAuthenticatorConfigs);
        authStep1.setFederatedIdentityProviders(identityProviders);
        authStep1.setAttributeStep(true);
        authStep1.setSubjectStep(true);
        authenticationSteps[0] = authStep1;

        // Step 2 - Desktop journey authenticator.
        LocalAuthenticatorConfig localDesktopAuthenticatorConfig = new LocalAuthenticatorConfig();
        LocalAuthenticatorConfig[] localDesktopAuthenticatorConfigs = new LocalAuthenticatorConfig[1];
        AuthenticationStep authStep2 = new AuthenticationStep();

        localDesktopAuthenticatorConfig.setDisplayName(AlRayanConstants.ARB_DESKTOP_CONNECTOR_FRIENDLY_NAME);
        localDesktopAuthenticatorConfig.setEnabled(true);
        localDesktopAuthenticatorConfig.setName(AlRayanConstants.ARB_DESKTOP_CONNECTOR_NAME);
        localDesktopAuthenticatorConfigs[0] = localDesktopAuthenticatorConfig;

        authStep2.setStepOrder(2);
        authStep2.setLocalAuthenticatorConfigs(localDesktopAuthenticatorConfigs);
        authenticationSteps[1] = authStep2;

        localAndOutboundAuthenticationConfig.setAuthenticationSteps(authenticationSteps);
        localAndOutboundAuthenticationConfig.setUseTenantDomainInLocalSubjectIdentifier(true);
        localAndOutboundAuthenticationConfig.setUseUserstoreDomainInLocalSubjectIdentifier(true);

        // Set script.
        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
        scriptConfig.setContent(readConditionalAuthScript(keyType));
        scriptConfig.setEnabled(true);
        localAndOutboundAuthenticationConfig.setAuthenticationScriptConfig(scriptConfig);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
    }

    /**
     * Return the list of configured federated authenticators.
     *
     * @param stub    application management stub
     * @param keyType environment (ex: sandbox or production)
     * @return configured federated authenticators
     * @throws APIManagementException thrown when error on getting configured federated authenticators
     */
    private IdentityProvider[] getFederatedAuthenticators(IdentityApplicationManagementServiceStub stub, String keyType)
            throws APIManagementException {
        try {
            String federatedAuthenticators = keyType.equals(SANDBOX_ENV) ?
                                             AlRayanConfiguration.SANDBOX_FEDERATED_AUTHENTICATORS.getValue() :
                                             AlRayanConfiguration.PRODUCTION_FEDERATED_AUTHENTICATORS.getValue();
            List<String> federatedAuthenticatorsList = Arrays.asList(federatedAuthenticators.split(","));
            List<IdentityProvider> identityProviders = new ArrayList<>();
            IdentityProvider[] federatedIdPs = stub.getAllIdentityProviders();
            if (federatedIdPs != null && federatedIdPs.length > 0) {
                for (IdentityProvider identityProvider : federatedIdPs) {
                    if (federatedAuthenticatorsList.contains(identityProvider.getIdentityProviderName())) {
                        identityProviders.add(identityProvider);
                    }
                }
            }
            final int identityProviderArrayLength = identityProviders.size();
            return identityProviders.toArray(new IdentityProvider[identityProviderArrayLength]);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            throw new APIManagementException(AlRayanError.ERROR_UPDATE_APPLICATION.getErrorMessageWithCode(), e);
        } catch (RemoteException e) {
            throw new APIManagementException(AlRayanError.ERROR_MAKING_REMOTE_CALL_ON_APP_CREATION
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Reads the adaptive authentication script content.
     *
     * @param keyType environment (ex: sandbox or production)
     * @return contents of the adaptive authentication script
     * @throws APIManagementException thrown when error on reading adaptive authentication script
     */
    private String readConditionalAuthScript(String keyType) throws APIManagementException {
        try {
            String fileName = keyType.equals(SANDBOX_ENV) ?
                              AlRayanConfiguration.CONDITIONAL_AUTH_SCRIPT_FILE_SANDBOX.getValue() :
                              AlRayanConfiguration.CONDITIONAL_AUTH_SCRIPT_FILE_PRODUCTION.getValue();
            Path filePath = Paths.get(CarbonUtils.getCarbonConfigDirPath(), "finance", fileName);
            File conditionalAuthScriptFile = filePath.toFile();
            if (!conditionalAuthScriptFile.exists()) {
                InputStream inputStream =
                        keyType.equals(SANDBOX_ENV) ?
                        getClass().getResourceAsStream("/conditional.auth.script.sandbox.js") :
                        getClass().getResourceAsStream("/conditional.auth.script.prod.js");
                return IOUtils.toString(inputStream);
            }
            return FileUtils.readFileToString(filePath.toFile());
        } catch (IOException e) {
            throw new APIManagementException(AlRayanError.ERROR_READING_ADAPTIVE_AUTH_SCRIPT
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Updates the application.
     *
     * @param stub            application management stub
     * @param serviceProvider application service provider
     * @throws APIManagementException thrown when error on updating the application
     */
    private void updateApplication(IdentityApplicationManagementServiceStub stub, ServiceProvider serviceProvider)
            throws APIManagementException {
        try {
            stub.updateApplication(serviceProvider);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            throw new APIManagementException(AlRayanError.ERROR_UPDATE_APPLICATION.getErrorMessageWithCode(), e);
        } catch (RemoteException e) {
            throw new APIManagementException(AlRayanError.ERROR_MAKING_REMOTE_CALL_ON_APP_CREATION
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Returns the application service provider.
     *
     * @param stub            application management stub
     * @param applicationName application name
     * @return service provider for app
     * @throws APIManagementException thrown when error on getting service provider for app
     */
    private ServiceProvider getApplicationServiceProvider(IdentityApplicationManagementServiceStub stub,
                                                          String applicationName) throws APIManagementException {
        try {
            ServiceProvider serviceProvider = stub.getApplication(applicationName);
            if (serviceProvider == null) {
                throw new APIManagementException("Application has not been created properly with name :" +
                                                 applicationName);
            }
            return serviceProvider;
        } catch (RemoteException e) {
            throw new APIManagementException(AlRayanError.ERROR_MAKING_REMOTE_CALL_ON_APP_CREATION
                    .getErrorMessageWithCode(), e);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            throw new APIManagementException(
                    "Error occurred while getting application service provider.", e);
        }
    }

    /**
     * Sets IAM options.
     *
     * @param backendServerURL IAM server URL
     * @param sessionCookie    authenticated user session cookie
     * @throws APIManagementException thrown when error on setting IAM options
     */
    private void setIDPMGTOptions(String backendServerURL, String sessionCookie) throws APIManagementException {
        try {
            String idPMgtServiceURL = backendServerURL + IDENTITY_PROVIDER_MGT_SERVICE;
            IdentityProviderMgtServiceStub idPMgtStub = new IdentityProviderMgtServiceStub(idPMgtServiceURL);
            ServiceClient idPMgtClient = idPMgtStub._getServiceClient();
            Options idPMgtOptions = idPMgtClient.getOptions();
            idPMgtOptions.setManageSession(true);
            idPMgtOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
        } catch (AxisFault axisFault) {
            throw new APIManagementException(AlRayanError.AXIS_FAULT_WHEN_INVOKING_REMOTE_CALL
                    .getErrorMessageWithCode(), axisFault);
        }
    }

    /**
     * Sets API Manager options.
     *
     * @param backendServerURL APIM server URL
     * @param sessionCookie    authenticated user session cookie
     * @return application service management stub
     * @throws APIManagementException thrown when error on setting APIM options
     */
    private IdentityApplicationManagementServiceStub setAPIMMGTOptions(String backendServerURL, String sessionCookie)
            throws APIManagementException {
        try {
            String appMgtServiceURL = backendServerURL + IDENTITY_APPLICATION_MGT_SERVICE;
            IdentityApplicationManagementServiceStub stub =
                    new IdentityApplicationManagementServiceStub(appMgtServiceURL);
            ServiceClient appMgtClient = stub._getServiceClient();
            Options appMgtOption = appMgtClient.getOptions();
            appMgtOption.setManageSession(true);
            appMgtOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            return stub;
        } catch (AxisFault axisFault) {
            throw new APIManagementException(AlRayanError.AXIS_FAULT_WHEN_INVOKING_REMOTE_CALL
                    .getErrorMessageWithCode(), axisFault);
        }
    }

    /**
     * Returns the session cookie string of the authenticated user.
     *
     * @param adminUsername    admin username
     * @param adminPassword    admin password
     * @param backendServerURL backend server URL
     * @return session cookie string of the authenticated user
     * @throws APIManagementException thrown when error on getting the authenticated user cookie string
     */
    private String getCookieString(String adminUsername, String adminPassword, String backendServerURL)
            throws APIManagementException {
        try {
            String authenticationServiceURL = backendServerURL + AUTHENTICATION_ADMIN_SERVICE;
            AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(authenticationServiceURL);
            if (!authenticationAdminStub.login(adminUsername, adminPassword, "localhost")) {
                throw new APIManagementException(AlRayanError.ERROR_AUTHENTICATING_USER.getErrorMessageWithCode());
            }
            ServiceContext serviceContext = authenticationAdminStub._getServiceClient().getLastOperationContext()
                    .getServiceContext();
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } catch (LoginAuthenticationExceptionException e) {
            throw new APIManagementException(AlRayanError.ERROR_AUTHENTICATING_USER.getErrorMessageWithCode(), e);
        } catch (AxisFault axisFault) {
            throw new APIManagementException(AlRayanError.AXIS_FAULT_WHEN_INVOKING_REMOTE_CALL
                    .getErrorMessageWithCode(), axisFault);
        } catch (RemoteException e) {
            throw new APIManagementException(AlRayanError.ERROR_MAKING_REMOTE_CALL_ON_APP_CREATION
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Add/remove application roles.
     *
     * @param adminUsername    admin username
     * @param userName         user's username
     * @param backendServerURL backend service URL
     * @param sessionCookie    authenticated user session cookie string
     * @param applicationName  created application name
     * @throws APIManagementException thrown when error on adding/removing application roles
     */
    private void addRemoveRolesOfUser(String adminUsername, String userName, String backendServerURL,
                                      String sessionCookie, String applicationName) throws APIManagementException {
        try {
            String userAdminServiceURL = backendServerURL + USER_ADMIN_SERVICE;
            UserAdminStub userAdminStub = new UserAdminStub(userAdminServiceURL);
            ServiceClient userAdminClient = userAdminStub._getServiceClient();
            Options userAdminOption = userAdminClient.getOptions();
            userAdminOption.setManageSession(true);
            userAdminOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            if (!adminUsername.equals(userName)) {
                userAdminStub.addRemoveRolesOfUser(adminUsername,
                        new String[]{ROLE_APPLICATION + "/" + applicationName}, null);
            }
        } catch (UserAdminUserAdminException e) {
            throw new APIManagementException(AlRayanError.ERROR_ON_ADD_REMOVE_APPLICATION_ROLE
                    .getErrorMessageWithCode(), e);
        } catch (AxisFault axisFault) {
            throw new APIManagementException(AlRayanError.AXIS_FAULT_WHEN_INVOKING_REMOTE_CALL
                    .getErrorMessageWithCode(), axisFault);
        } catch (RemoteException e) {
            throw new APIManagementException(AlRayanError.ERROR_MAKING_REMOTE_CALL_ON_APP_CREATION
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Set certs to the application.
     * <p>
     * The application will be deleted if adding certs fails
     *
     * @param oAuthApplicationInfo oAuth application info of the created application
     * @param applicationName      application name
     * @param stub                 application management service stub
     * @param serviceProvider      service provider
     * @throws APIManagementException thrown when error on adding certs to application
     */
    private void setApplicationCerts(OAuthApplicationInfo oAuthApplicationInfo, String applicationName,
                                     IdentityApplicationManagementServiceStub stub, ServiceProvider serviceProvider)
            throws APIManagementException {
        try {
            String spCertificate = (String) oAuthApplicationInfo.getParameter("sp_certificate");
            if (spCertificate != null && !spCertificate.isEmpty()) {
                String certificate = new String(Base64.decode(spCertificate), StandardCharsets.UTF_8);
                IdentityApplicationManagementUtil.getCertData(certificate);
                serviceProvider.setCertificateContent(certificate);
            } else {
                log.error("Invalid application certificate data provided, hence removing the application: " +
                          applicationName);
                deleteApplication(applicationName, stub);
                throw new APIManagementException(AlRayanError.APPLICATION_CERT_CONTENT_IS_EMPTY
                        .getErrorMessageWithCode());
            }

            // If the provided certificate contains the PSD2 eIDAS certificate attributes, extract the
            // Organizational ID from the SP certificate and add as a SP property in KM.
            // This will allow the ability to use separate certificates for signing and transport for users that
            // use Sign UP flow during TPP registration.
            if (spCertificate != null && !spCertificate.isEmpty()) {
                X509Certificate spCerObject = parseSPApplicationCertStirng(spCertificate);
                ArrayList<ServiceProviderProperty> spPropList = new ArrayList<>(Arrays.asList
                        (serviceProvider.getSpProperties()));
                if (spCerObject != null) {
                    try {
                        log.debug("Checking whether the provided TPP certificate contains the eIDAS attributes");

                        CertificateContent spEIDASCert = CertificateContentExtractor.extract(spCerObject);

                        log.debug("eIDAS certificate found. Extracting the organization id from the certificate");

                        ServiceProviderProperty orgIDProperty = new ServiceProviderProperty();
                        orgIDProperty.setName(CommonConstants.ORGANIZATION_ID);
                        orgIDProperty.setValue(spEIDASCert.getPspAuthorisationNumber());
                        orgIDProperty.setDisplayName(CommonConstants.ORGANIZATION_ID_DISPLAY_NAME);
                        spPropList.add(orgIDProperty);
                        serviceProvider.setSpProperties(spPropList.toArray(
                                new ServiceProviderProperty[spPropList.size()]));
                    } catch (CertificateValidationException e) {
                        // The CertificateValidationException is thrown if the certificate does not contain
                        // PSD2 eIDAS attributes. This exception has been ignored as this component does not
                        // enforce eIDAS certificates.
                        if (log.isDebugEnabled()) {
                            log.debug("Provided certificate does not contain PSD2 eIDAS certificate " +
                                    "attributes");
                        }
                    }
                }
            }
        } catch (CertificateException e) {
            deleteApplication(applicationName, stub);
            throw new APIManagementException(AlRayanError.INVALID_APPLICATION_CERT_DATA.getErrorMessageWithCode(), e);
        }
    }

    /**
     * Delete application.
     * <p>
     * This method will be invoked when the updating of the application with appropriate information fails
     *
     * @param applicationName application name
     * @param stub            application management stub
     * @throws APIManagementException thrown when error on deleting the application
     */
    private void deleteApplication(String applicationName, IdentityApplicationManagementServiceStub stub)
            throws APIManagementException {
        try {
            stub.deleteApplication(applicationName);
        } catch (RemoteException e) {
            throw new APIManagementException(AlRayanError.ERROR_MAKING_REMOTE_CALL_ON_APP_CREATION
                    .getErrorMessageWithCode(), e);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            throw new APIManagementException(AlRayanError.ERROR_ON_ADDING_CERT_TO_APP.getErrorMessageWithCode(), e);
        }
    }

    /**
     * Parse the certificate string from the SP application
     *
     * @param spCertificate SP certificate string
     * @return certificate object
     * @throws CertificateException when an error occurs while parsing the SP certificate
     */
    private X509Certificate parseSPApplicationCertStirng(String spCertificate) throws CertificateException {
        String spCert = new String(Base64.decode(spCertificate), StandardCharsets.UTF_8);
        byte[] decoded = Base64.decode(spCert
                .replaceAll(IdentityConstants.BEGIN_CERT, StringUtils.EMPTY)
                .replaceAll(IdentityConstants.END_CERT, StringUtils.EMPTY)
        );
        return (X509Certificate) CertificateFactory.getInstance("X509")
                .generateCertificate(new ByteArrayInputStream(decoded));
    }
}
