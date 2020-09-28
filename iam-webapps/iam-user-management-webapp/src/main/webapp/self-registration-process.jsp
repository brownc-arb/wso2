<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="com.alrayan.wso2.common.AlRayanConfiguration" %>
<%@ page import="com.alrayan.wso2.common.AlRayanConstants" %>
<%@ page import="com.alrayan.wso2.common.AlRayanError" %>
<%@ page import="com.alrayan.wso2.common.crypto.ARBSignatureHandler"%>
<%@ page import="com.alrayan.wso2.common.crypto.ARBAsymmetricKeyCryptoProvider"%>
<%@ page import="com.alrayan.wso2.common.exception.PINCodeNotFoundException" %>
<%@ page import="com.alrayan.wso2.common.exception.PlatformValidationException" %>
<%@ page import="com.alrayan.wso2.common.exception.StringDecryptionException" %>
<%@ page import="com.alrayan.wso2.common.exception.UserNameNotFoundException" %>
<%@ page import="com.alrayan.wso2.common.exception.UserNameNotUniqueException" %>
<%@ page import="com.alrayan.wso2.common.exception.PINCodeNotComplexException" %>
<%@ page import="com.alrayan.wso2.user.core.util.UserManagementUtil" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointConstants" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointUtil" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementServiceUtil" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.ApiException" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.api.SelfRegisterApi" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.api.UsernameRecoveryApi" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.Claim" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.Error" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.Property" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.SelfRegistrationUser" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.SelfUserRegistrationRequest" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.User" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.apache.commons.collections.map.HashedMap" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.user.core.util.UserCoreUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.security.cert.CertificateException" %>
<%@ page import="java.security.cert.X509Certificate" %>
<%@ page import="java.security.PrivateKey" %>
<%@ page import="java.security.PublicKey" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.identity.recovery.IdentityRecoveryConstants" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.security.KeyStoreException" %>
<%@ page import="java.security.NoSuchAlgorithmException" %>
<%@ page import="java.security.UnrecoverableKeyException" %>
<%@ page import="com.alrayan.wso2.common.utils.KeyStoreUtils" %>

<%@ page import="uk.co.alrayan.PinUtils" %>

<jsp:directive.include file="localize.jsp"/>

    <html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>User portal</title>

        <link rel="icon" href="images/Favicon.jpg" type="image/x-icon"/>
        <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
        <link href="css/Roboto.css" rel="stylesheet">
        <link href="css/custom-common.css" rel="stylesheet">

        <!--[if lt IE 9]>
        <script src="js/html5shiv.min.js"></script>
        <script src="js/respond.min.js"></script>
        <![endif]-->
    </head>

    <body>

    <!-- header -->
	<header class="header header-default">
		<div class="container-fluid">
		   <div class="pull-left brand float-remove-xs text-center-xs brand-container">
			  <img src="images/Al_Rayan_Logo-150min.jpg" class="logo" alt="Alrayan Open Banking"/>
			  <h2 class="text-center-sm text-center-xs text-center-md text-right">User portal</h2>
		   </div>
		</div>
	</header>

    <!-- page content -->
    <div class="container-fluid body-wrapper">

        <%
            String username;
            String encryptedUsername;
            // Validate whether the request is of the same platform and is not directly posted.
            username = request.getParameter("username");
            encryptedUsername = username;
            if (StringUtils.isEmpty(username)) {
                request.setAttribute("error", true);
                request.setAttribute("errorCode", AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorCode());
                request.setAttribute("errorMsg", AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getMessage());
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }

            if("true".equals(AlRayanConfiguration.USER_REGISTRATION_ENCRYPTION_ENABLED.getValue())) {
                 try {
                    byte[] salesforceIdBytes = Base64.getDecoder().decode(encryptedUsername);
                    PrivateKey privateKey = KeyStoreUtils
                            .getPrivateKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                                    AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                                    AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());
                     username = KeyStoreUtils.decryptFromPrivateKey(privateKey, salesforceIdBytes);
                 }
                 catch (StringDecryptionException | KeyStoreException | CertificateException |
                         NoSuchAlgorithmException | IllegalArgumentException |
                         UnrecoverableKeyException e) {
                        request.setAttribute("error", true);
                        request.setAttribute("errorCode", AlRayanError.PLATFORM_VALIDATION_FAILED.getErrorCode());
                        request.setAttribute("errorMsg", AlRayanError.PLATFORM_VALIDATION_FAILED.getMessage());
                        request.getRequestDispatcher("error.jsp").forward(request, response);
                        return;
                }
            }

            if("true".equals(AlRayanConfiguration.USER_REGISTRATION_SIGNATURE_VALIDATION_ENABLED.getValue())) {
            //validate for the signature
                String dotconnectnonse = request.getParameter("dotconnectnonse");

                if (StringUtils.isEmpty(dotconnectnonse)) {
                    request.setAttribute("error", true);
                    request.setAttribute("errorMsg", "Signature value cannot be empty");
                    request.getRequestDispatcher("error.jsp").forward(request, response);
                    return;
                }

                PublicKey publicKey;
                X509Certificate x509Certificate;
                ARBSignatureHandler arbSignatureHandler = new ARBSignatureHandler();
                boolean verified;

                try {
                    x509Certificate = ARBAsymmetricKeyCryptoProvider
                            .getServerCert(AlRayanConfiguration.DOTCONNECT_PUBLIC_CERT_ALIAS.getValue());
                    publicKey = x509Certificate.getPublicKey();
                    verified = arbSignatureHandler.verifySignature(encryptedUsername, dotconnectnonse, publicKey);
                } catch (Exception e) {
                    request.setAttribute("error", true);
                    request.setAttribute("errorMsg", "Signature validation failed");
                    request.getRequestDispatcher("error.jsp").forward(request, response);
                    return;
                }

                if(!verified) {
                    request.setAttribute("error", true);
                    request.setAttribute("errorMsg", "Signature Verification failed");
                    request.getRequestDispatcher("error.jsp").forward(request, response);
                    return;
                }
            }

        %>
        <%
            // Set domain to username.
            username = UserCoreUtil
                    .addDomainToName(username, AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue());

            boolean isSelfRegistrationWithVerification =
                    Boolean.parseBoolean(request.getParameter("isSelfRegistrationWithVerification"));

            String userLocale = request.getHeader("Accept-Language");
            String dbpUsername = request.getParameter(AlRayanConstants.CLAIM_ACTING_USERNAME);
            String dbpPinCode = request.getParameter(AlRayanConstants.CLAIM_PIN_CODE);
            String password = request.getParameter("password");
            String callback = request.getParameter("callback");
            String originalCallback = request.getParameter("originalCallback");
            String tenantDomain = request.getParameter("tenantDomain");
            String consent = request.getParameter("consent");
            String policyURL = IdentityManagementServiceUtil.getInstance().getServiceContextURL().replace("/services",
                    "/authenticationendpoint/privacy_policy.do");
            if (StringUtils.isNotEmpty(consent)) {
                consent = IdentityManagementEndpointUtil.buildConsentForResidentIDP
                        (username, consent, "USA",
                                IdentityManagementEndpointConstants.Consent.COLLECTION_METHOD_SELF_REGISTRATION,
                                IdentityManagementEndpointConstants.Consent.LANGUAGE_ENGLISH, policyURL,
                                IdentityManagementEndpointConstants.Consent.EXPLICIT_CONSENT_TYPE,
                                true, false, IdentityManagementEndpointConstants.Consent.INFINITE_TERMINATION);
            }
            if (StringUtils.isBlank(originalCallback)) {
                originalCallback = IdentityManagementEndpointUtil.getUserPortalUrl(
                        application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
            }

            if (StringUtils.isBlank(username)) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "Username.cannot.be.empty"));
                if (isSelfRegistrationWithVerification) {
                    request.getRequestDispatcher("self-registration-with-verification.jsp").forward(request, response);
                } else {
                    request.getRequestDispatcher("self-registration-without-verification.jsp").forward(request, response);
                }
            }

            if (StringUtils.isBlank(password)) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "Password.cannot.be.empty"));
                if (isSelfRegistrationWithVerification) {
                    request.getRequestDispatcher("self-registration-with-verification.jsp").forward(request, response);
                } else {
                    request.getRequestDispatcher("self-registration-without-verification.jsp").forward(request, response);
                }
            }

            session.setAttribute("username", username);
            User user = IdentityManagementServiceUtil.getInstance().getUser(username);
            Claim[] claims = new Claim[0];
            List<Claim> claimsList;
            UsernameRecoveryApi usernameRecoveryApi = new UsernameRecoveryApi();
            try {
                claimsList = usernameRecoveryApi.claimsGet(null);
                if (claimsList != null) {
                    claims = claimsList.toArray(new Claim[claimsList.size()]);
                }
            } catch (ApiException e) {
                Error error = new Gson().fromJson(e.getMessage(), Error.class);
                request.setAttribute("error", true);
                if (error != null) {
                    request.setAttribute("errorMsg", error.getDescription());
                    request.setAttribute("errorCode", error.getCode());
                }

                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }


            List<Claim> userClaimList = new ArrayList<>();
            try {
                for (Claim claim : claims) {
                    if (StringUtils.isNotBlank(request.getParameter(claim.getUri()))) {
                        Claim userClaim = new Claim();
                        userClaim.setUri(claim.getUri());
                        userClaim.setValue(request.getParameter(claim.getUri()));
                        userClaimList.add(userClaim);

                    } else if (claim.getUri().trim().equals("http://wso2.org/claims/locality")
                            && StringUtils.isNotBlank(userLocale)) {

                        Claim localeClaim = new Claim();
                        localeClaim.setUri(claim.getUri());
                        localeClaim.setValue(userLocale.split(",")[0].replace('-','_'));
                        userClaimList.add(localeClaim);
                    }
                }

                // Validate digital banking username.
                if(StringUtils.isBlank(dbpUsername)){
                    throw new UserNameNotFoundException(AlRayanError
                            .ACTING_USERNAME_CLAIM_NOT_SPECIFIED.getErrorMessageWithCode());
                }

                if(UserManagementUtil.isDBPUserExist(dbpUsername)){
                    throw new UserNameNotUniqueException(AlRayanError
                            .USER_WITH_SAME_ACTING_USERNAME_FOUND.getErrorMessageWithCode());
                }

                // Validate PIN code
                if(StringUtils.isBlank(dbpPinCode)){
                    throw new PINCodeNotFoundException(AlRayanError
                            .PIN_CODE_NOT_SPECIFIED.getErrorMessageWithCode());
                }

                int retVal2 = uk.co.alrayan.PinUtils.checkPin(dbpPinCode);
                if (retVal2 < 0) {
                    throw new PINCodeNotComplexException(AlRayanError.PINCODE_NOT_COMPLEX_ENOUGH.getErrorMessageWithCode());
                }

                SelfRegistrationUser selfRegistrationUser = new SelfRegistrationUser();
                selfRegistrationUser.setUsername(user.getUsername());
                selfRegistrationUser.setTenantDomain(user.getTenantDomain());
                selfRegistrationUser.setRealm(user.getRealm());
                selfRegistrationUser.setPassword(password);
                selfRegistrationUser.setClaims(userClaimList);

                List<Property> properties = new ArrayList<>();
                Property sessionKey = new Property();
                sessionKey.setKey("callback");
                sessionKey.setValue(URLEncoder.encode(originalCallback, "UTF-8"));

                Property consentProperty = new Property();
                consentProperty.setKey("consent");
                consentProperty.setValue(consent);
                properties.add(sessionKey);
                properties.add(consentProperty);


                SelfUserRegistrationRequest selfUserRegistrationRequest = new SelfUserRegistrationRequest();
                selfUserRegistrationRequest.setUser(selfRegistrationUser);
                selfUserRegistrationRequest.setProperties(properties);

                Map<String, String> requestHeaders = new HashedMap();
                if(request.getParameter("g-recaptcha-response") != null) {
                    requestHeaders.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
                }

                SelfRegisterApi selfRegisterApi = new SelfRegisterApi();
                selfRegisterApi.mePostCall(selfUserRegistrationRequest, requestHeaders);
                request.setAttribute("callback", callback);
                request.getRequestDispatcher("self-registration-complete.jsp").forward(request, response);

            } catch (Exception e) {
                Error error;
                if(e instanceof UserNameNotFoundException) {
                    error = new Error();
                    error.setCode(AlRayanError
                            .ACTING_USERNAME_CLAIM_NOT_SPECIFIED.getErrorCode());
                    error.setDescription(AlRayanError
                            .ACTING_USERNAME_CLAIM_NOT_SPECIFIED.getMessage());
                } else if(e instanceof UserNameNotUniqueException) {
                    error = new Error();
                    error.setCode(AlRayanError
                            .USER_WITH_SAME_ACTING_USERNAME_FOUND.getErrorCode());
                    error.setDescription(AlRayanError
                            .USER_WITH_SAME_ACTING_USERNAME_FOUND.getMessage());
                } else if(e instanceof PINCodeNotFoundException) {
                    error = new Error();
                    error.setCode(AlRayanError
                            .PIN_CODE_NOT_SPECIFIED.getErrorCode());
                    error.setDescription(AlRayanError
                            .PIN_CODE_NOT_SPECIFIED.getMessage());
                } else if(e instanceof PINCodeNotComplexException) {
                    error = new Error();
                    error.setCode(AlRayanError.PINCODE_NOT_COMPLEX_ENOUGH.getErrorCode());
                    error.setDescription(AlRayanError
                                                .PINCODE_NOT_COMPLEX_ENOUGH.getMessage());
                } else
                    /*
                        this code does not make sense. e.getMessage will not be JSON!
                    {
                    error = new Gson().fromJson(e.getMessage(), Error.class);
                    if (error != null) {
                        Map<String, String> enumLookUp =
                                Arrays.stream(IdentityRecoveryConstants.ErrorMessages.values())
                                        .collect(Collectors
                                                .toMap(IdentityRecoveryConstants.ErrorMessages::getCode,
                                                        IdentityRecoveryConstants.ErrorMessages::getMessage));
                        String errorMessage = enumLookUp.get(error.getCode());
                        if(StringUtils.isEmpty(errorMessage)){
                            error.setCode(AlRayanError.UNKNOWN_ERROR_WHILE_USER_REGISTRATION.getErrorCode());
                            error.setDescription(AlRayanError.UNKNOWN_ERROR_WHILE_USER_REGISTRATION.getMessage());
                        } else {
                            error.setDescription(errorMessage);
                        }
                    } else */
                    {
                        error = new Error();
                        error.setCode(AlRayanError.UNKNOWN_ERROR_WHILE_USER_REGISTRATION.getErrorCode());
                        error.setDescription(AlRayanError.UNKNOWN_ERROR_WHILE_USER_REGISTRATION.getMessage());
                    }
                }
                request.setAttribute("error", true);
                if (error != null) {
                    request.setAttribute("errorMsg", error.getDescription());
                    request.setAttribute("errorCode", error.getCode());
                }
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        %>
    </div>

    <!-- footer -->
    <footer class="footer">
        <div class="container-fluid">
            <p>&copy; Copyright <script>document.write(new Date().getFullYear());</script> <a title="AlRayan Bank" href="https://www.alrayanbank.co.uk/" target="_blank">
                        			<i class="icon fw fw-alrayan"></i> Al Rayan Bank PLC</a></p>
        </div>
    </footer>

    <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
    <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>


    </body>
    </html>
