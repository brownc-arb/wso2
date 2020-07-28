~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
~
~ This software is the property of WSO2 Inc. and its suppliers, if any.
~ Dissemination of any information or reproduction of any material contained
~ herein is strictly forbidden, unless permitted by WSO2 in accordance with
~ the WSO2 Commercial License available at http://wso2.com/licenses. For specific
~ language governing the permissions and limitations under this license,
~ please see the license as well as any agreement you’ve entered into with
~ WSO2 governing the purchase of this software and any associated services.

<%@page import="com.wso2.finance.open.banking.common.exception.ConsentMgtException" %>
<%@page
        import="com.wso2.finance.open.banking.consent.mgt.stet.v140.mgt.service.PaymentsConsentMgtService" %>
<%@page
        import="org.owasp.encoder.Encode" %>
<%@page import="org.wso2.carbon.context.PrivilegedCarbonContext" %>
<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page
        import="com.wso2.finance.open.banking.consent.mgt.stet.v140.mgt.model.AdvancedPaymentConsent" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.stet.v140.mgt.util.PaymentInformationStatusEnum" %>

<html>
<head></head>
<body>
<%
    final String PAYMENTS = "payments";
    final String APPROVE = "approve";
    boolean readyToInvokeAuthorizedFlow = false;
    
    //Payments or Accounts
    String consentType = (String) request.getParameter("type");
    //Allowed or Denied
    String consent = (String) request.getParameter("consent");
    
    String consentID = (String) request.getParameter("id");
    String debtorAccount = (String) request.getParameter("paymentAccount");
    String psuID = "";
    if (session.getAttribute("username") != null) {
        psuID = session.getAttribute("username").toString();
    }
    
    String approval = "Rejected";
    
    if (APPROVE.equals(consent) && PAYMENTS.equals(consentType)) {
        approval = "AcceptedCustomerProfile";
    }
    
    if (PAYMENTS.equals(consentType)) {
        
        AdvancedPaymentConsent advancedPaymentConsent = new AdvancedPaymentConsent();
        PaymentInformationStatusEnum statusEnum = PaymentInformationStatusEnum.fromValue(approval);
        advancedPaymentConsent.setStatus(statusEnum.toString());
        
        advancedPaymentConsent.setUserId(psuID);
        advancedPaymentConsent.setPaymentRequestId(consentID);
        advancedPaymentConsent.setDebtorAccount(debtorAccount);
        
        if ("Rejected".equals(approval)) {
            advancedPaymentConsent.setRevokedBy(psuID);
            advancedPaymentConsent.setReason("Rejected by PSU through authentication webapp");
        }
        
        PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);
        
        try {
            paymentsConsentMgtService.addUserConsent(advancedPaymentConsent);
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

<p>You will be redirected back to the <%=Encode.forHtmlContent((String) request.getParameter("app"))%>. If the
    redirection fails, please click the post button.....</p>


<form method="post" id="oauth2_authz" name="oauth2_authz" action="../oauth2/authorize">
    <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways"
           value="<%=Encode.forHtmlAttribute((String)request.getParameter("hasApprovedAlways"))%>"/>
    <input type="hidden" name="<%=Constants.SESSION_DATA_KEY_CONSENT%>"
           value="<%=Encode.forHtmlAttribute((String)request.getParameter(Constants.SESSION_DATA_KEY_CONSENT))%>"/>
    <input type="hidden" name="consent" id="consent"
           value="<%=Encode.forHtmlAttribute((String)request.getParameter("consent"))%>"/>
    <input type="hidden" name="user" id="user"
           value="<%=Encode.forHtmlAttribute((String)request.getParameter("user"))%>"/>
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