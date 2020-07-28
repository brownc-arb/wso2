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
<%@ page import="com.alrayan.wso2.common.AlRayanError" %>
<%@ page import="com.alrayan.wso2.common.crypto.ARBSignatureHandler"%>
<%@ page import="com.alrayan.wso2.common.crypto.ARBAsymmetricKeyCryptoProvider"%>
<%@ page import="com.alrayan.wso2.common.exception.StringDecryptionException" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointConstants" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointUtil" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.ApiException" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.api.RecoverCredentialsApi" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.api.UsernameRecoveryApi" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.Claim" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.Error" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.UserClaim" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.recovery.IdentityRecoveryException" %>
<%@ page import="java.security.cert.CertificateException" %>
<%@ page import="java.security.cert.X509Certificate" %>
<%@ page import="java.security.PrivateKey" %>
<%@ page import="java.security.PublicKey" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.util.List" %>
<%@ page import="java.security.KeyStoreException" %>
<%@ page import="java.security.NoSuchAlgorithmException" %>
<%@ page import="java.security.UnrecoverableKeyException" %>
<%@ page import="com.alrayan.wso2.common.utils.KeyStoreUtils" %>
<jsp:directive.include file="localize.jsp"/>

<%
    boolean isPasswordRecoveryEmailConfirmation =
            Boolean.parseBoolean(request.getParameter("isPasswordRecoveryEmailConfirmation"));
    boolean isUsernameRecovery = Boolean.parseBoolean(request.getParameter("isUsernameRecovery"));
    boolean isDBBasedRecovery = Boolean.parseBoolean(request.getParameter("isDBBasedRecovery"));
    boolean isDBBasedRecoveryOld = Boolean.parseBoolean(request.getParameter("isDBBasedRecoveryOld"));

    // Common parameters for password recovery with email and self registration with email
    String username = request.getParameter("username");
    String confirmationKey = request.getParameter("confirmationKey");
    String callback = request.getParameter("callback");
    String tenantDomain = request.getParameter("tenantDomain");

    if (StringUtils.isBlank(callback)) {
        callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
    }

    // Password recovery parameters
    String recoveryOption = request.getParameter("recoveryOption");


    if (isUsernameRecovery) {
        // Username Recovery Scenario
        List<Claim> claims;
        UsernameRecoveryApi usernameRecoveryApi = new UsernameRecoveryApi();
        try {
            claims = usernameRecoveryApi.claimsGet(null);
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

        List<UserClaim> claimDTOList = new ArrayList<UserClaim>();

        for (Claim claimDTO : claims) {
            if (StringUtils.equals(claimDTO.getUri(),
                    IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM) ||
                    StringUtils.equals(claimDTO.getUri(),
                            IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM) ||
                    StringUtils.equals(claimDTO.getUri(),
                            IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM)) {
                if (StringUtils.isNotBlank(request.getParameter(claimDTO.getUri()))) {
                    UserClaim userClaim = new UserClaim();
                    userClaim.setUri(claimDTO.getUri());
                    userClaim.setValue(request.getParameter(claimDTO.getUri()));
                    claimDTOList.add(userClaim);
                }
            }
        }

        try {
            usernameRecoveryApi.recoverUsernamePost(claimDTOList, tenantDomain, null);
            request.setAttribute("callback", callback);
            request.getRequestDispatcher("username-recovery-complete.jsp").forward(request, response);
        } catch (ApiException e) {
            if (e.getCode() == 204) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "No.valid.user.found"));
                request.getRequestDispatcher("recoverusername.do").forward(request, response);
                return;
            }

            Error error = new Gson().fromJson(e.getMessage(), Error.class);
            request.setAttribute("error", true);
            if (error != null) {
                request.setAttribute("errorMsg", error.getDescription());
                request.setAttribute("errorCode", error.getCode());
            }
            request.getRequestDispatcher("recoverusername.do").forward(request, response);
            return;
        }

    } else {
        if (isPasswordRecoveryEmailConfirmation) {
            session.setAttribute("username", username);
            session.setAttribute("confirmationKey", confirmationKey);
            request.setAttribute("callback", callback);
            request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        } else if (isDBBasedRecovery) {
            String digitalBankingUsername;
            // Validate for the signature
            if("true".equals(AlRayanConfiguration.USER_REGISTRATION_SIGNATURE_VALIDATION_ENABLED.getValue())) {
            String dotconnectnonse = request.getParameter("dotconnectnonse");
            if (StringUtils.isEmpty(dotconnectnonse)) {
                response.sendRedirect(callback + "?error=" + true +
                                      "&errorMsg=Signature value cannot be empty");
                return;
            }
            PublicKey publicKey = null;
            X509Certificate x509Certificate;
            ARBSignatureHandler arbSignatureHandler = new ARBSignatureHandler();
            boolean verified = false;

            try {
                x509Certificate = ARBAsymmetricKeyCryptoProvider.getServerCert(AlRayanConfiguration.DOTCONNECT_PUBLIC_CERT_ALIAS.getValue());
                publicKey = x509Certificate.getPublicKey();
                verified = arbSignatureHandler.verifySignature(username, dotconnectnonse, publicKey);
            } catch (Exception e) {
                response.sendRedirect(callback + "?error=" + true +
                                      "&errorMsg=Signature validation failed");
                return;
            }

            if(!verified) {
                response.sendRedirect(callback + "?error=" + true +
                                      "&errorMsg=Signature Verification failed");
                return;
            }
         }

            try {
                if (StringUtils.isEmpty(username)) {
                    response.sendRedirect(callback + "?error=" + true + "&errorCode=" +
                                          AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorCode() +
                                          "&errorMsg=" +
                                          AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getMessage());
                    return;
                }

               if("true".equals(AlRayanConfiguration.USER_RECOVERY_ENCRYPTION_ENABLED.getValue())) {
                // Decrypt username
                byte[] salesforceIdBytes = Base64.getDecoder().decode(username);
                   PrivateKey privateKey = KeyStoreUtils
                           .getPrivateKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                                   AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                                   AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());
                   username = KeyStoreUtils
                        .decryptFromPrivateKey(privateKey, salesforceIdBytes);
                 }
                RecoverCredentialsApi recoverCredentialsApi = new RecoverCredentialsApi();
                confirmationKey = recoverCredentialsApi.getRecoverPasswordConfirmationKey(username);
                digitalBankingUsername = recoverCredentialsApi.getUsernameForSalesforceId(username);
            } catch (IdentityRecoveryException e) {
                response.sendRedirect(callback + "?error=" + true +
                                      "&errorMsg=" + e.getErrorDescription());
                return;
            } catch(StringDecryptionException | KeyStoreException | CertificateException |
                    NoSuchAlgorithmException | UnrecoverableKeyException |
                    IllegalArgumentException e) {
                response.sendRedirect(callback + "?error=" + true + "&errorCode=" +
                                      AlRayanError.SALESFORCE_ID_DECRYPTION_FAILURE.getErrorCode() +
                                      "&errorMsg=" +
                                      AlRayanError.SALESFORCE_ID_DECRYPTION_FAILURE.getMessage());
                return;
            }


            //These vales are added in both the session and request as the DBP which is using the IAM as a iframe is
            //having some issues related to session failures
            session.setAttribute("confirmationKey", confirmationKey);
            session.setAttribute("callback", callback);
            request.setAttribute("confirmationKey", confirmationKey);
            request.setAttribute("callback", callback);
            request.setAttribute("isDBBasedRecovery", true);
            request.setAttribute("digitalBankingUsername", digitalBankingUsername);
            request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        } else if (isDBBasedRecoveryOld) {
            String digitalBankingUsername;

            try {
                if (StringUtils.isEmpty(username)) {
                    response.sendRedirect(callback + "?error=" + true + "&errorCode=" +
                            AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getErrorCode() +
                            "&errorMsg=" +
                            AlRayanError.SALESFORCE_ID_NOT_SPECIFIED.getMessage());
                    return;
                }

                RecoverCredentialsApi recoverCredentialsApi = new RecoverCredentialsApi();
                confirmationKey = recoverCredentialsApi.getRecoverPasswordConfirmationKey(username);
            } catch (IdentityRecoveryException e) {
                response.sendRedirect(callback + "?error=" + true +
                        "&errorMsg=" + e.getErrorDescription());
                return;
            }


            //These vales are added in both the session and request as the DBP which is using the IAM as a iframe is
            //having some issues related to session failures
            session.setAttribute("confirmationKey", confirmationKey);
            session.setAttribute("callback", callback);
            session.setAttribute("digitalBankingUsername", username);
            request.setAttribute("confirmationKey", confirmationKey);
            request.setAttribute("callback", callback);
            request.setAttribute("isDBBasedRecoveryOld", true);
            request.setAttribute("digitalBankingUsername", username);
            request.getRequestDispatcher("password-old-reset.jsp").forward(request, response);
        }
        else {
            request.setAttribute("username", username);
            session.setAttribute("username", username);

            if (IdentityManagementEndpointConstants.PasswordRecoveryOptions.EMAIL.equals(recoveryOption)) {
                request.setAttribute("callback", callback);
                request.getRequestDispatcher("password-recovery-notify.jsp").forward(request, response);
            } else if (IdentityManagementEndpointConstants.PasswordRecoveryOptions.SECURITY_QUESTIONS
                    .equals(recoveryOption)) {
                request.setAttribute("callback", callback);
                request.getRequestDispatcher("challenge-question-request.jsp?username=" + username).forward(request,
                        response);
            } else {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "Unknown.password.recovery.option"));
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        }
    }
%>
<html>
<head>
    <title>Al Rayan Bank - username and password recovery</title>
</head>
<body>

</body>
</html>