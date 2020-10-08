<%--
 ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 ~
 ~ This software is the property of WSO2 Inc. and its suppliers, if any.
 ~ Dissemination of any information or reproduction of any material contained
 ~ herein is strictly forbidden, unless permitted by WSO2 in accordance with
 ~ the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 ~ language governing the permissions and limitations under this license,
 ~ please see the license as well as any agreement you’ve entered into with
 ~ WSO2 governing the purchase of this software and any associated services.
--%>

<%@page import="com.wso2.finance.open.banking.common.config.uk.UKSpecConfigParser" %>
<%@page import="com.wso2.finance.open.banking.common.exception.ConsentMgtException" %>
<%@page import="com.wso2.finance.open.banking.common.exception.OpenBankingException" %>
<%@page import="com.wso2.finance.open.banking.management.information.reporting.common.model.AccountRequest" %>
<%@page import="com.wso2.finance.open.banking.management.information.reporting.common.model.FundsConfirmationRequest" %>
<%@page import="com.wso2.finance.open.banking.management.information.reporting.common.model.PaymentRequest" %>
<%@page import="com.wso2.finance.open.banking.management.information.reporting.common.service.OBReportingDataService" %>
<%@page import="com.wso2.finance.open.banking.management.information.reporting.common.util.AnalyticsUtil" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.model.MultipleAuthorizationData" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.model.MultipleAuthorizationUser" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.service.MultipleAuthorizationMgtService" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.util.MultipleAuthorizationStatusEnum" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.util.MultipleAuthorizationUserStatusEnum" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.model.AdvancedAccountConsent" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.model.AdvancedFundsConfirmationConsent" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.model.AdvancedPaymentConsent" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.model.DebtorAccount" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.model.FundsConfirmationSetupResponse" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.service.AccountsConsentMgtService" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.service.FundsConfirmationConsentMgtService" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.service.PaymentsConsentMgtService" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.context.PrivilegedCarbonContext" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.DebtorAccountRetriever" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.MultiAuthenticationSubmissionAPI" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.MultipleAuthenticationDataRetriever" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="java.time.Instant" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.stream.StreamSupport" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.model.MultipleAuthorizationData" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.model.MultipleAuthorizationUser" %>
<%@ page import="com.wso2.finance.open.banking.common.exception.OpenBankingException" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.util.MultipleAuthorizationUserStatusEnum" %>
<%@ page import="com.wso2.finance.open.banking.multiple.authorization.mgmt.util.MultipleAuthorizationStatusEnum" %>
<%@ page import="com.alrayan.wso2.vasco.authentication.AuthUserSigGenCommand" %>
<%@ page import="org.wso2.carbon.user.core.util.UserCoreUtil" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.nio.file.Path" %>
<%@ page import="java.nio.file.Paths" %>
<%@ page import="java.util.Properties" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.client.MultipleAuthenticationDataRetriever" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.model.FundsConfirmationSetupResponse" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.wso2.finance.open.banking.common.config.uk.UKSpecConfigParser" %>
<%@ page import="java.time.Instant" %>
<html>
<head>
    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
</head>
<body>
<%
    final String ACCOUNTS = "accounts";
    final String PAYMENTS = "payments";
    final String FUNDS_CONFIRMATIONS = "fundsconfirmations";
    final String APPROVE = "approve";

    final String APPROVAL_STATE_REJECTED = "Rejected";
    final String APPROVAL_STATE_AUTHORIZED = "Authorised";

    boolean readyToInvokeAuthorizedFlow = false;
    boolean isAuthUserSuccess = false;

    String consentType = (String) request.getParameter("type");
    String callBackURL = (String)request.getParameter("callback_url");
    String vascoChallengeKey = (String)request.getParameter("vascoChallengeKey");
    String cronto = (String)request.getParameter("cronto");
    String appName = (String)request.getParameter("app");
    String consentId = (String) request.getParameter("id");
    String consent = (String) request.getParameter("consent");
    String username = (String) session.getAttribute("username");
    String userTenantDomain = (String) session.getAttribute("userTenantDomain");
    String tenantAwareUserName = username + "@" + userTenantDomain;

    boolean isReauthorization = request.getParameter("isReauthorization") == null ? false : Boolean.valueOf(request.getParameter("isReauthorization"));
    String approval = APPROVAL_STATE_REJECTED;
    if (APPROVE.equals(consent) && ACCOUNTS.equals(consentType)) {
        approval = APPROVAL_STATE_AUTHORIZED;
    }
    if (APPROVE.equals(consent) && PAYMENTS.equals(consentType)) {
        approval = "AcceptedCustomerProfile";
    }
    if (APPROVE.equals(consent) && FUNDS_CONFIRMATIONS.equals(consentType)) {
        approval = "Authorised";
    }

    if (ACCOUNTS.equals(consentType)) {

        String[] account = request.getParameterValues("accounts[]");
        List<String> accounts = account == null ? new ArrayList<>(Arrays.asList(new String[]{""}))
                : new ArrayList<String>(Arrays.asList(account));
        OBReportingDataService OBReportingDataService = AnalyticsUtil.getAnalyticsDataRetrieverService();
        AccountRequest accountRequest = new AccountRequest();
        String clientId = (String) session.getAttribute("clientId");
        long unixTimestamp = Instant.now().getEpochSecond();
        accounts.remove("default");
        AdvancedAccountConsent aac = new AdvancedAccountConsent();
        aac.setAccountConsentID((String) request.getParameter("id"));
        aac.setApproval(approval);
        aac.setCollectionMethod("test");
        aac.setAccountIds(accounts);
        aac.setUserId(tenantAwareUserName);

        if (UKSpecConfigParser.getInstance().isMAEnabled()) {
            accountRequest.setConsentId((String) request.getParameter("id"));
            accountRequest.setUserId(tenantAwareUserName);
            accountRequest.setClientId(clientId);
            accountRequest.setReAuth(isReauthorization);
            accountRequest.setDebtorAccountIds(accounts);
            accountRequest.setAuthorisationStatus(approval);
            accountRequest.setTimestamp(unixTimestamp);

            OBReportingDataService.publishAccountsDataToAnalytics(accountRequest);
        }

        AccountsConsentMgtService accountsConsentMgtService = (AccountsConsentMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(AccountsConsentMgtService.class, null);
        try {
            if (isReauthorization) {
                accountsConsentMgtService.updateAccountConsent(aac);
            } else {
                accountsConsentMgtService.addAccountConsent(aac);
            }
            readyToInvokeAuthorizedFlow = true;
        } catch (ConsentMgtException e) {
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Error while approving account consent");
            return;
        }

    } else if (FUNDS_CONFIRMATIONS.equals(consentType)) {

        AdvancedFundsConfirmationConsent fundsConfirmationConsent = new AdvancedFundsConfirmationConsent();
        FundsConfirmationConsentMgtService fundsConfirmationConsentMgtService = (FundsConfirmationConsentMgtService)
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(FundsConfirmationConsentMgtService
                        .class, null);
        OBReportingDataService OBReportingDataService = AnalyticsUtil.getAnalyticsDataRetrieverService();
        FundsConfirmationRequest fundsConfirmationRequest = new FundsConfirmationRequest();
        String clientId = (String) session.getAttribute("clientId");
        long unixTimestamp = Instant.now().getEpochSecond();
        //get account id
        FundsConfirmationSetupResponse setupResponse = null;
        try {
            setupResponse = fundsConfirmationConsentMgtService.getConsentByConsentId(consentId,
                    (String) request.getAttribute("app"));
        } catch (ConsentMgtException e) {
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Error while approving account consent");
            return;
        }
        fundsConfirmationConsent.setAccountId(setupResponse.getData().getDebtorAccount().getIdentification());
        fundsConfirmationConsent.setConsentId(consentId);
        fundsConfirmationConsent.setApproval(approval);
        fundsConfirmationConsent.setCollectionMethod("test");
        fundsConfirmationConsent.setUserId(tenantAwareUserName);

        if (UKSpecConfigParser.getInstance().isMAEnabled()) {
            fundsConfirmationRequest.setConsentId(consentId);
            fundsConfirmationRequest.setUserId(tenantAwareUserName);
            fundsConfirmationRequest.setClientId(clientId);
            fundsConfirmationRequest.setDebtorId(setupResponse.getData().getDebtorAccount().getIdentification());
            fundsConfirmationRequest.setAuthorisationStatus(approval);
            fundsConfirmationRequest.setTimestamp(unixTimestamp);

            OBReportingDataService.publishFundsConfirmationDataToAnalytics(fundsConfirmationRequest);
        }

        try {
            if (isReauthorization) {
                fundsConfirmationConsentMgtService.updateFundsConfirmationConsentStatus(fundsConfirmationConsent);
            } else {
                fundsConfirmationConsentMgtService.updateFundsConfirmationConsent(fundsConfirmationConsent);
            }
            readyToInvokeAuthorizedFlow = true;
        } catch (ConsentMgtException e) {
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Error while approving account consent");
            return;
        }
    } else if (PAYMENTS.equals(consentType)) {

        String paymentAccount = (String) request.getParameter("paymentAccount");
		String paymentAmount = (String)request.getParameter("paymentAmount");
        String clientId = (String) session.getAttribute("clientId");
        String sessionExpiry = (String) session.getAttribute("MultiAuthExpiry");
        String multipleAuthorizationType = (String) session.getAttribute("MultiAuthType");
        String specVersion = (String) session.getAttribute("spec_version");
        long unixTimestamp = Instant.now().getEpochSecond();
		String BUSINESS_PATTERN = "payment.account.business.patten";
        String BUSINESS_LIMIT = "payment.account.business.maxLimit";
        String PERSONAL_LIMIT = "payment.account.personal.maxLimit";
        String CARBON_HOME = "carbon.home";
        String REGEX = null;
        double businessLimit = 0.0;
        double personalLimit = 0.0;

        OBReportingDataService OBReportingDataService = AnalyticsUtil.getAnalyticsDataRetrieverService();

		String carbonHome = System.getProperty(CARBON_HOME);
        Path path = Paths.get(carbonHome,"repository","conf","finance","alrayan-identity.properties");

		// read configuration from alrayan-identity.properties files
        FileInputStream fileInputStream = null;
        try{
            fileInputStream = new FileInputStream(path.toFile());
            Properties properties = new Properties();
            properties.load(fileInputStream);
        	REGEX = properties.getProperty(BUSINESS_PATTERN);
        	businessLimit = Double.parseDouble(properties.getProperty(BUSINESS_LIMIT));
        	personalLimit = Double.parseDouble(properties.getProperty(PERSONAL_LIMIT));
        } catch (IOException e) {
            throw new IOException("Error while reading the alrayan-identity.properties");
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        //check the account Type
        if(Pattern.compile(REGEX).matcher(paymentAccount.substring(6)).lookingAt()) {
            //Business account limit check
            if(Double.parseDouble(paymentAmount) > businessLimit) {
                session.invalidate();
                response.sendRedirect(callBackURL+"?error=Over_limit&statusMsg=Amount+is+over+the+defined+limit+for+account+type");
                return;
            }
        } else {
            //Personal account limit check
            if(Double.parseDouble(paymentAmount) > personalLimit) {
                session.invalidate();
                response.sendRedirect(callBackURL+"?error=Over_limit&statusMsg=Amount+is+over+the+defined+limit+for+account+type");
                return;
            }
        }

        if (APPROVE.equals(consent)) {
            if ("UK300".equals(specVersion)) {
                approval = "Authorised";
            } else {
                approval = "AcceptedCustomerProfile";
            }
        }

        boolean shouldAddUserConsent = true;
        boolean multiAuth = false;

        // Multiple Authorization Handling
        MultipleAuthorizationUserStatusEnum statusEnum = null;
        MultipleAuthorizationData multiAuthSession = null;

        if (StringUtils.isNotEmpty(multipleAuthorizationType) && multipleAuthorizationType.equals("Any")) {
            if (StringUtils.isNotEmpty(username) && username.contains("/")) {
                username = username.split("/")[1];
            }

            // Get Current Multiple Authentication Session
            Map<String, Object> dataMap = MultipleAuthenticationDataRetriever
                    .getMultipleAuthorizationSession(consentId, username);

            // Show Validation results if error
            if ("false".equals(dataMap.get(MultipleAuthenticationDataRetriever.Keys.IS_ERROR))) {
                multiAuthSession = (MultipleAuthorizationData) dataMap
                        .get(MultipleAuthenticationDataRetriever.Keys.MULTI_AUTH_DATA);
            } else {
                session.invalidate();
                response.sendRedirect(String.format("retry.do?status=Error&statusMsg=%s",
                        dataMap.get(MultipleAuthenticationDataRetriever.Keys.IS_ERROR)));
            }

            MultipleAuthorizationMgtService mgtService = (MultipleAuthorizationMgtService) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getOSGiService(MultipleAuthorizationMgtService.class, null);

            // Initiate New Multiple Authentication Session
            if (multiAuthSession == null) {
                Map<String, String> parameters = new HashMap<>();
                // Username is in the format USERSTORE/SDFCID eg : PSU.ALRAYAN.WSO2/SDFC-PC0008123456

                parameters.put("userID", username);
                parameters.put("consentID", consentId);
                // Get Payable Accounts for user
                String payableAccounts = DebtorAccountRetriever.getPayableAccounts(parameters, appName);
                if(payableAccounts== null) {
                    String isError = "This PSU doesnt have any Payable Acoounts";
                    session.invalidate();
                    response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                    return;
                }
                JSONArray accountArray = new JSONObject(payableAccounts).getJSONArray("data");

                // Retrieve Selected user account
                JSONObject selectedAccount = (JSONObject) StreamSupport.stream(accountArray.spliterator(), false)
                        .filter(account -> ((JSONObject) account).get("account_id").toString().equals(paymentAccount))
                        .findFirst().orElse(null);

                if (selectedAccount != null) {

                    // Ensure authorization method for selected account is multiple
                    if ("multiple".equals(selectedAccount.get("authorizationMethod").toString())) {

                        JSONArray userArray = selectedAccount.getJSONArray("authorizationUsers");
                        // Build MultipleAuthorization Detail
                        Date expiryDate = new Date(Long.parseLong(sessionExpiry));
                        MultipleAuthorizationData.Builder builder = new MultipleAuthorizationData
                                .Builder(expiryDate, clientId, consentId, paymentAccount);

                        StreamSupport.stream(userArray.spliterator(), false).forEach(obj -> {
                            JSONObject user = (JSONObject) obj;
                            MultipleAuthorizationUser authorizationUser = new MultipleAuthorizationUser();
                            builder.addUser(user.get("user_id").toString());
                        });

                        multiAuthSession = builder.build();

                        try {
                            mgtService.initiateMultipleAuthorization(multiAuthSession);
                        } catch (OpenBankingException e) {
                            session.invalidate();
                            response.sendRedirect("retry.do?status=Error&statusMsg=Error while initiating " +
                                    "Multiple Authorization");
                        }
                    }
                }
            } else {
                shouldAddUserConsent = false;
            }

            if (APPROVE.equals(consent)) {
                statusEnum = MultipleAuthorizationUserStatusEnum.APPROVED;
            } else {
                statusEnum = MultipleAuthorizationUserStatusEnum.REJECTED;
            }

            // Update Multi Authentication User
            if (multiAuthSession != null) {
                MultipleAuthorizationData currentMultiAuthData = null;
                try {
                    multiAuthSession = mgtService.addUserAuthorizationByConsentId(consentId, username, statusEnum);
                } catch (OpenBankingException e) {
                    session.invalidate();
                    response.sendRedirect(String.format("retry.do?status=Error&statusMsg=Error updating " +
                            "Multiple Authorization status - %s", e.getMessage()));
                }
                try {
                    currentMultiAuthData = mgtService.getMultipleAuthorizationByConsentId(consentId);
                    if (MultipleAuthorizationStatusEnum.AUTHORISED.toString()
                            .equals(currentMultiAuthData.getStatus())) {
                        MultiAuthenticationSubmissionAPI.multiAuthorisationSubmission(consentId, appName);
                    }
                } catch (OpenBankingException e) {
                    session.invalidate();
                    response.sendRedirect(String.format("retry.do?status=Error&statusMsg=Error while getting " +
                            "Multiple Authorization Submission status - %s", e.getMessage()));
                }
            }
        }

        PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);

        PaymentRequest paymentRequest = new PaymentRequest();

        if (UKSpecConfigParser.getInstance().isMAEnabled()) {
            paymentRequest.setConsentId(consentId);
            paymentRequest.setUserId(tenantAwareUserName);
            paymentRequest.setDebtorId(paymentAccount);
            paymentRequest.setAuthorisationStatus(approval);
            paymentRequest.setMultiAuth(multiAuth);
            paymentRequest.setClientId(clientId);
            paymentRequest.setTimestamp(unixTimestamp);

            OBReportingDataService.publishPaymentsDataToAnalytics(paymentRequest);
        }

        if (shouldAddUserConsent) {
            AdvancedPaymentConsent apc = new AdvancedPaymentConsent();
            apc.setPaymentId(consentId);
            apc.setApproval(approval);
            apc.setCollectionMethod("test");
            apc.setUserId(tenantAwareUserName);

            DebtorAccount da = new DebtorAccount();
            da.setSchemeName("SortCodeAccountNumber");
            da.setIdentification(paymentAccount);
            da.setName("");
            da.setSecondaryIdentification("");
            apc.setDebtorAccount(da);

            try {
                paymentsConsentMgtService.addUserConsent(apc);
            } catch (ConsentMgtException e) {
                session.invalidate();
                response.sendRedirect("retry.do?status=Error&statusMsg=Error while approving payment consent");
                return;
            }
        }

        readyToInvokeAuthorizedFlow = true;
    }

    AuthUserSigGenCommand authUserSigGenCommand = new AuthUserSigGenCommand(UserCoreUtil.removeDomainFromName(username),
                        cronto, vascoChallengeKey, appName);
    isAuthUserSuccess = authUserSigGenCommand.execute();
    if (!isAuthUserSuccess) {  %>
           <script type="text/javascript">
               datapublisher("<%= consentType %>","<%= username %>","<%=consentId%>");
               window.location.href = "<%=callBackURL%>?error=Invalid_Cronto_Code&statusMsg=Given_Cronto_code_is_incorrect._Please_try_again";
               function datapublisher(type,username,consentId) {
                  $.post('authentication_datapublisher.jsp',{type: type, username: username, consentId:consentId});
                }
           </script>
        <%
    } else {

            // Invoke authotize flow
            if (readyToInvokeAuthorizedFlow) {
            %>

                <p>You will be redirected back to the <%=Encode.forHtmlContent((String) request.getParameter("app"))%>. If the
                    redirection fails, please click the post button.....</p>

                <form method="post" id="oauth2_authz" name="oauth2_authz" action="../commonauth">
                    <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="<%=Encode.forHtmlAttribute((String)request.getParameter("hasApprovedAlways"))%>"/>
                    <input type="hidden" name="<%=Constants.SESSION_DATA_KEY_CONSENT%>" value="<%=Encode.forHtmlAttribute((String)request.getParameter(Constants.SESSION_DATA_KEY_CONSENT))%>"/>
                    <input type="hidden" name="consent" id="consent" value="<%=Encode.forHtmlAttribute((String)request.getParameter("consent"))%>"/>
                    <input type="hidden" name="user" id="user" value="<%=Encode.forHtmlAttribute((String) session.getAttribute("username"))%>"/>
                    <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
                    <button type="submit">POST</button>
                </form>

                <script type="text/javascript">
                    document.forms[0].submit();
                </script>

                <%
            } else {}
           

    }
	%>
</body>
</html>
