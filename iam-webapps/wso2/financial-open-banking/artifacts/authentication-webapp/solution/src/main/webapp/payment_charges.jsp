<%@ page
        import="com.alrayan.wso2.common.AlRayanError" %>
<%@ page
        import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page
        import="org.apache.commons.lang3.StringUtils" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.client.UKBankChargesAPI" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.client.model.PaymentChargesRequestInfo" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.model.PaymentChargesResponse" %>
<%@ page import="java.util.Map" %>
<%@ include file="localize.jsp" %>
<%@ page import = "java.util.ResourceBundle" %>
<% ResourceBundle resource = ResourceBundle.getBundle("alrayan-errorCodes");%>

<%
    String accountId = request.getParameter("accountId");
    String paymentChargesRequestInfoJSON = request.getParameter("paymentChargesRequestInfo");
    String app = request.getParameter("appName");
    String paymentType = request.getParameter("paymentType");
    String consentID = request.getParameter("consentID");
    PaymentChargesRequestInfo paymentChargesRequestInfo = new ObjectMapper()
            .readValue(paymentChargesRequestInfoJSON, PaymentChargesRequestInfo.class);
    String payerAccountIdentification = paymentChargesRequestInfo.getPayerAccountIdentification();
    if (StringUtils.isNotEmpty(payerAccountIdentification) &&
        !payerAccountIdentification.equals(accountId)) {
        session.invalidate();
        %>
            <div class="alert alert-danger acc-err" id="bankChargesErrorDiv" style=""><b>Error loading bank charges !!! </b>
            <%=AlRayanError.PASSED_DEBTOR_ACCOUNT_DOES_NOT_BELONG_TO_USER.getErrorMessageWithCode()%></div>
        <%
    } else {
        // Get bank charges now.
        paymentChargesRequestInfo.setPayerAccountIdentification(accountId);
        if("Batch File Payment".equals(paymentType)) {
                    paymentChargesRequestInfo.setPayerReference("FILEPAYMENT___" + consentID + "___" + paymentChargesRequestInfo.getPayerReference());
                }
        Map<String, Object> paymentChargesResponseMessage =
                UKBankChargesAPI.getBankCharges(paymentChargesRequestInfo, app);
        if ("true".equals(paymentChargesResponseMessage.get("isError"))) {
            String errorMessage = (String) paymentChargesResponseMessage.get("alRayanErrorMessage");
            session.invalidate();
        %>
        <div class="alert alert-danger acc-err" id="bankChargesErrorDiv" style=""><b>Error loading bank charges !!! </b>
            <%=errorMessage%></div>
        <%
        } else {
            PaymentChargesResponse paymentChargesResponse = (PaymentChargesResponse)
                    paymentChargesResponseMessage.get("paymentChargesResponse");
            if(paymentChargesResponse == null) {
               String errorCode = (String) paymentChargesResponseMessage.get("BackEndErrorCode");
               if (resource.getString(errorCode) != null) { //show error if defined in alrayan-errorCodes.properties
                %>
                    <div class="alert alert-danger acc-err" id="bankChargesErrorDiv" style="">
                        <b><%=resource.getString(errorCode)+" !!!"%></b>
                    </div>
                <%
                } else { %>
                    <div class="alert alert-danger acc-err" id="bankChargesErrorDiv" style="">
                        <b>Error loading bank charges !!!</b>
                    </div>
                <%}
            } else {
                %>
                <div>
                    <label class="control-label">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "payment.charges")%>
                    </label>
                    <ul class="scope">
                        <li>
                            <%=AuthenticationEndpointUtil.i18n(resourceBundle, "payment.currency") +
                               " : " + paymentChargesResponse.getPaymentCurrency() %>
                        </li>
                        <%
                            if(!"Domestic Payments".equals(paymentType)) { %>
                                <li><%=AuthenticationEndpointUtil.i18n(resourceBundle, "payment.exchange.rate") +
                                       " : " + paymentChargesResponse.getPaymentExchangeRate() %>
                                </li>
                            <% }
                        %>
                        <li><%=AuthenticationEndpointUtil.i18n(resourceBundle, "payment.charges") +
                               " : " + paymentChargesResponse.getPaymentCharges() %>
                        </li>
                    </ul>
                </div>
                <%
            }
        }
    }
%>
