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

<%@page import="com.wso2.finance.open.banking.common.exception.ConsentMgtException" %>
<%@page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.service.PaymentsConsentMgtService" %>
<%@page import="org.owasp.encoder.Encode"%>
<%@page import="org.wso2.carbon.context.PrivilegedCarbonContext" %>
<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.PaymentUpdateRequestBody" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.util.TransactionStatusEnum" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.UserAccountConsent" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.util.ConsentStatusEnum" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.PermissionEnum" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.service.AccountsConsentMgtService" %>
<%@ page import="java.lang.reflect.Array" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.AccountReference" %>
<html>
<head></head>
<body>

<%
    final String PAYMENTS = "payments";
    final String ACCOUNTS = "accounts";
    final String APPROVE = "approve";
    boolean readyToInvokeAuthorizedFlow = false;
    List<String> allIBANsList = new ArrayList<>();
    List<String> allIBANsList2 = new ArrayList<>();
    List<String> accountsIBANsList = new ArrayList<>();;
    List<String> balancesIBANsList = new ArrayList<>();;
    List<String> transactionsIBANsList = new ArrayList<>();;
    
    //Payments or Accounts
    String consentType = (String) request.getParameter("type");
    //Allowed or Denied
    String consent = (String) request.getParameter("consent");
    
    String tppUniqueId = (String) request.getParameter("tppUniqueId");
    String consentID = (String) request.getParameter("id");
    String psuID = "";
    if (session.getAttribute("username") != null) {
        psuID = session.getAttribute("username").toString();
    }
    
    String permission = (String) request.getParameter("permission");
    
    //Account IBANs are currently hard coded since it's only mock data from mock back end
    if (!PermissionEnum.DEFAULT.toString().equals(permission)) {
        allIBANsList.add("DE2310010010123456788");
        allIBANsList.add("DE2310010010123456789");
    }
    String accounts = (request.getParameter("accountsList"));
    accountsIBANsList.add(accounts);
    String balances = (request.getParameter("balancesList"));
    balancesIBANsList.add(balances);
    String transactions = (request.getParameter("transactionsList"));
    transactionsIBANsList.add(transactions);
    String all = (request.getParameter("allaccountsList"));
    allIBANsList2.add(all);
    
    System.out.println(allIBANsList2);
    
    String approval = "Rejected";
    String approvalForAccounts = "rejected";
    
    if (APPROVE.equals(consent) && PAYMENTS.equals(consentType)) {
        approval = "AcceptedCustomerProfile";
    } else if (APPROVE.equals(consent) && ACCOUNTS.equals(consentType)) {
        approvalForAccounts = "valid";
    }
    
    if (PAYMENTS.equals(consentType)) {
        
        PaymentUpdateRequestBody paymentUpdateRequestBody = new PaymentUpdateRequestBody();
        TransactionStatusEnum statusEnum = TransactionStatusEnum.fromValue(approval);
        paymentUpdateRequestBody.setStatus(statusEnum);
        
        paymentUpdateRequestBody.setPSUId(psuID);
        paymentUpdateRequestBody.setTPPUniqueId(tppUniqueId);
        
        if ("Rejected".equals(approval)) {
            paymentUpdateRequestBody.setRevokedBy(psuID);
            paymentUpdateRequestBody.setReason("Rejected by PSU through authentication webapp");
        }
        
        PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);
        
        try {
            paymentsConsentMgtService.initiatePaymentConfirmationRequest(paymentUpdateRequestBody, consentID);
            readyToInvokeAuthorizedFlow = true;
        } catch (ConsentMgtException e) {
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Error while approving payment consent");
            return;
        }
    } else if (ACCOUNTS.equals(consentType)) {
        
        UserAccountConsent userAccountConsent = new UserAccountConsent();
        ConsentStatusEnum consentStatusEnum = ConsentStatusEnum.fromValue(approvalForAccounts);
        
        userAccountConsent.setStatus(consentStatusEnum);
        userAccountConsent.setConsentId(consentID);
        userAccountConsent.setUserId(psuID);
        userAccountConsent.setTPPUniqueId(tppUniqueId);
        
        //checking which Account lists to be filled according to the user consent
        //Lists that are not filled are set to empty lists to avoid NPE
        if (PermissionEnum.ALL_PSD2.toString().equals(permission)) {
            //All accounts
            userAccountConsent.setAccountsIBANS(allIBANsList);
            userAccountConsent.setBalancesIBANS(allIBANsList);
            userAccountConsent.setTransactionsIBANS(allIBANsList);
        } else if (PermissionEnum.AVAILABLE_ACCOUNTS.toString().equals(permission)) {
            //Accounts only
            userAccountConsent.setAccountsIBANS(allIBANsList);
            userAccountConsent.setBalancesIBANS(Collections.emptyList());
            userAccountConsent.setTransactionsIBANS(Collections.emptyList());
        } else if (PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString().equals(permission)) {
            //Accounts and Balances
            userAccountConsent.setAccountsIBANS(allIBANsList);
            userAccountConsent.setBalancesIBANS(allIBANsList);
            userAccountConsent.setTransactionsIBANS(Collections.emptyList());
            
        } else if (PermissionEnum.DEFAULT.toString().equals(permission)) {
            if (balancesIBANsList != null) {
                userAccountConsent.setBalancesIBANS(balancesIBANsList);
                
            }
            if (accountsIBANsList != null) {
                
                userAccountConsent.setAccountsIBANS(allIBANsList2);
            }
            
            if (transactionsIBANsList != null) {
                userAccountConsent.setTransactionsIBANS(transactionsIBANsList);
            }
            
        }
        
        AccountsConsentMgtService accountsConsentMgtService = (AccountsConsentMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(AccountsConsentMgtService.class, null);
        
        try {
            accountsConsentMgtService.addAccountConsent(userAccountConsent);
            readyToInvokeAuthorizedFlow = true;
        } catch (ConsentMgtException e) {
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Error while approving payment consent");
            return;
        }
    }

    // Invoke authotize flow
    if (readyToInvokeAuthorizedFlow) {
%>

<p>You will be redirected back to the <%=Encode.forHtmlContent((String)request.getParameter("app"))%>. If the
    redirection fails, please click the post button.....</p>


<form method="post" id="oauth2_authz" name="oauth2_authz" action="../oauth2/authorize">
    <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="<%=Encode.forHtmlAttribute((String)request.getParameter("hasApprovedAlways"))%>"/>
    <input type="hidden" name="<%=Constants.SESSION_DATA_KEY_CONSENT%>" value="<%=Encode.forHtmlAttribute((String)request.getParameter(Constants.SESSION_DATA_KEY_CONSENT))%>"/>
    <input type="hidden" name="consent" id="consent" value="<%=Encode.forHtmlAttribute((String)request.getParameter("consent"))%>"/>
    <input type="hidden" name="user" id="user" value="<%=Encode.forHtmlAttribute((String)request.getParameter("user"))%>"/>
    <button type="submit">POST</button>
</form>

<script type="text/javascript">
    document.forms[0].submit();
</script>

<%
    }
%>


</body>
</html>
