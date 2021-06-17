<!doctype html>
<%--
  ~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="com.wso2.finance.open.banking.common.config.uk.UKSpecConfigParser" %>
<%@ page import="com.wso2.finance.open.banking.common.util.CommonConstants" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.AccountReference" %>
<%@ page import="com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.PermissionEnum" %>
<%@ page import="com.wso2.finance.open.banking.management.information.reporting.common.model.AccountRequest" %>
<%@ page import="com.wso2.finance.open.banking.management.information.reporting.common.model.FundsConfirmationRequest" %>
<%@ page import="com.wso2.finance.open.banking.management.information.reporting.common.model.PaymentRequest" %>
<%@ page import="com.wso2.finance.open.banking.management.information.reporting.common.service.OBReportingDataService" %>
<%@ page import="com.wso2.finance.open.banking.management.information.reporting.common.util.AnalyticsUtil" %>
<%@ page import="com.wso2.finance.open.banking.uk.consent.mgt.model.DebtorAccount" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.APIDataRetriever"%>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.DebtorAccountRetriever"%>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.MultipleAuthenticationDataRetriever" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.SensitiveDataRetriever" %>
<%@ page import="org.wso2.carbon.identity.oauth.dao.OAuthAppDAO" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.UKAPIDataRetriever" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="org.wso2.carbon.user.core.util.UserCoreUtil" %>
<%@ page import="java.io.Serializable" %>
<%@ page import="java.time.Instant" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Optional" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.stream.Stream" %>
<%@ page import="org.apache.axiom.util.base64.Base64Utils" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.model.PaymentChargesRequestInfo" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="com.alrayan.wso2.common.AlRayanError" %>
<%@include file="localize.jsp" %>
<%@ page import="com.wso2.finance.open.banking.management.information.reporting.common.model.AuthenticationRequest" %>
<%
    String requestObject = null;
    String loggedInUser = null;
    String app = null;
    String spQueryParams = null;
    String scopeString = null;
    String[] scopeArray = null;
    String clientID = "null";
    final String PERMISSION = "Permission";
    final String IS_ERROR = "isError";
    final String LOGGED_IN_USER = "loggedInUser";
    final String APPLICATION = "application";
    final String SP_QUERY_PARAMS = "spQueryParams";
    final String SCOPE = "scope";
    final String ACCESS_TOKEN = "accessToken";
    final String DISPLAY_SCOPES = "displayScopes";
    final String IDENTIFICATION = "Identification";
    String paymentDatasetForVasco = "";
    String userTenantDomain;
    PaymentChargesRequestInfo paymentChargesRequestInfo = null;
	final String REDIRECT_URI = "redirect_uri";
    String paymentType = "";

    String accessToken = config.getServletContext().getInitParameter(ACCESS_TOKEN);
    boolean displayScopes = Boolean.parseBoolean(config.getServletContext().getInitParameter(DISPLAY_SCOPES));

    String sessionDataKey = request.getParameter(("sessionDataKey"));

    // String consentId = request.getParameter("consentID");

    Map<String, Serializable> sensitiveDataMap = SensitiveDataRetriever.getSensitiveDataWithSessionKey(sessionDataKey);


    if ("false".equals(sensitiveDataMap.get(IS_ERROR))) {
        loggedInUser = (String) sensitiveDataMap.get(LOGGED_IN_USER);
        app = (String) sensitiveDataMap.get(APPLICATION);
        spQueryParams = (String) sensitiveDataMap.get(SP_QUERY_PARAMS);
        scopeString = (String) sensitiveDataMap.get(SCOPE);
        app = (String)sensitiveDataMap.get("application");
        clientID = (String)sensitiveDataMap.get("client_id");
        userTenantDomain = (String)sensitiveDataMap.get("userTenantDomain");
    } else {
        String isError = (String) sensitiveDataMap.get(IS_ERROR);
        session.invalidate();
        response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
        return;
    }

    OAuthAppDAO oAuthAppDAO = new OAuthAppDAO();
    String callbackUrl = oAuthAppDAO.getAppInformation(clientID).getCallbackUrl();

    String appName = app != null ? app.split("_")[1] : "";

    //appName is set to use in the multi authorization section in confirm flow
    session.setAttribute("appName", app);

    if (scopeString != null) {
        scopeArray = scopeString.split(" ");
    }

    // Remove domain from logged in user.
    loggedInUser = UserCoreUtil.removeDomainFromName(loggedInUser);

    boolean isDefaultConsent = false;

    if (scopeArray != null && scopeArray.length > 0 && Arrays.asList(scopeArray).contains("api_store")) {
        isDefaultConsent = true;
    }
    String[] requestedClaimList = new String[0];
    String[] mandatoryClaimList = new String[0];
    if (request.getParameter(Constants.REQUESTED_CLAIMS) != null) {
        requestedClaimList = request.getParameter(Constants.REQUESTED_CLAIMS).split(Constants.CLAIM_SEPARATOR);
    }
    if (request.getParameter(Constants.MANDATORY_CLAIMS) != null) {
        mandatoryClaimList = request.getParameter(Constants.MANDATORY_CLAIMS).split(Constants.CLAIM_SEPARATOR);
    }

    /*This parameter decides whether the consent page will only be used to get consent for sharing claims with the
    Service Provider. If this param is 'true' and user has already given consents for the OIDC scopes, we will be
    hiding the scopes being displayed and the approve always button.
    */
    boolean userClaimsConsentOnly = Boolean.parseBoolean(request.getParameter(Constants.USER_CLAIMS_CONSENT_ONLY));

    String USER_ID_KEY_NAME = "userID";
    String CONSENT_ID_KEY_NAME = "consentID";

    //username is set into session during first page load
    if ((String) session.getAttribute("username") == null) {
        session.setAttribute("username", sensitiveDataMap.get("loggedInUser"));
    }

    // userTenantDomain is set to use in the confirmation
    if (session.getAttribute("userTenantDomain") == null){
        session.setAttribute("userTenantDomain", userTenantDomain);
    }

    if (spQueryParams != null && !spQueryParams.trim().isEmpty()) {
        String[] spQueries = spQueryParams.split("&");
        for (String param : spQueries) {
			if (param.contains("request")) {
                requestObject = (param.substring("request=".length())).replaceAll("\\r\\n|\\r|\\n|\\%20", "");
            }
        }
    }
    session.setMaxInactiveInterval(900);
    int timeout = session.getMaxInactiveInterval();

    response.setHeader("Refresh", timeout + "; URL = " + callbackUrl + "?status=Session TimedOut");

    // Insert Client id into session
    List<String> spQueryParamList = Arrays.asList(spQueryParams.split("&"));
    String clientId = "";
    for (String param : spQueryParamList) {
        if (param.startsWith("client_id=")) {
            clientId = param.substring("client_id=".length());
        }
    }

    session.setAttribute("clientId", clientId);

    // Human readable app name, defaults to app name
    String appReadableName = app;

    // Get application attributes for UK
    Map<String, String> applicationAttributes = new HashMap<>();
    Optional<String> applicatioOnBehalfOf = Optional.empty();

    if (CommonConstants.UK_SPEC_NAME.equals(APIDataRetriever.getDeployedSpec())) {

        UKAPIDataRetriever dataRetriever = (UKAPIDataRetriever) APIDataRetriever.getApiDataRetriever();
        applicationAttributes = dataRetriever.getApplicationAttributesMap(app);

        Optional<String> organizationName = Optional.ofNullable(applicationAttributes
                .getOrDefault(UKAPIDataRetriever.APP_ORG_NAME, appReadableName));

        appReadableName = applicationAttributes.getOrDefault(UKAPIDataRetriever.APP_NAME, appReadableName);

        // If organization name is present format readable name with organization name
        if (organizationName.isPresent()){
            appReadableName = String.format("%s by %s", appReadableName, organizationName.get());
        }

        applicatioOnBehalfOf = Optional.ofNullable(applicationAttributes.get(UKAPIDataRetriever.APP_ON_BEHALF_OF));

    }

%>

<html>
<head>
    <jsp:include page="includes/head.jsp"/>
    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>

</head>

<body>

<script type="text/javascript">
    function approved() {
        document.getElementById("approve").disabled = true;
        document.getElementById('consent').value = "approve";
        validateFrm();
    }


    function approvedDefaultClaim() {
        var mandatoryClaimCBs = $(".mandatory-claim");
        var checkedMandatoryClaimCBs = $(".mandatory-claim:checked");
        var scopeApproval = $("input[name='scope-approval']");

        // If scope approval radio button is rendered then we need to validate that it's checked
        if (scopeApproval.length > 0) {
            if (scopeApproval.is(":checked")) {
                var checkScopeConsent = $("input[name='scope-approval']:checked");
                $('#consent').val(checkScopeConsent.val());
            } else {
                $("#modal_scope_validation").modal();
                return;
            }
        } else {
            // Scope radio button was not rendered therefore set the consent to 'approve'
            document.getElementById('consent').value = "approve";
        }

        if (checkedMandatoryClaimCBs.length === mandatoryClaimCBs.length) {
            document.getElementById("profile").submit();
        } else {
            $("#modal_claim_validation").modal();
        }
    }

    function approvedAlwaysDefaultClaim() {
        var mandatoryClaimCBs = $(".mandatory-claim");
        var checkedMandatoryClaimCBs = $(".mandatory-claim:checked");

        if (checkedMandatoryClaimCBs.length === mandatoryClaimCBs.length) {
            document.getElementById('consent').value = "approveAlways";
            document.getElementById("profile").submit();
        } else {
            $("#modal_claim_validation").modal();
        }
    }

    function denyDefaultClaim() {
        document.getElementById('consent').value = "deny";
        document.getElementById("profile").submit();
    }

    function approvedBerlin() {
        document.getElementById('consent').value = "approve";
        validateFrmBerlin();
    }

    function approvedSTET() {
        document.getElementById('consent').value = "approve";
        validateFrmSTET();
    }

    function approvedAlways() {
        document.getElementById('consent').value = "approveAlways";
        validateFrm();
    }

    function deny() {
        var callbackUrl = "<%= callbackUrl %>" + "#consent+denied"
        location.replace(callbackUrl);
    }

    function updateAcc(obj) {
        document.getElementById('account').value = $('#accselect').val();
    }

    function setReAuthAccounts(){
        document.getElementById('account').value = "reAuth";
    }

    function updatePaymentAcc(obj) {
        document.getElementById('paymentAccount').value = obj.value;
    }

    function validateFrmBerlin() {
        if (document.getElementById('type').value === "payments") {
            document.getElementById("oauth2_authz_confirm").submit();
        }
        if (document.getElementById('type').value === "accounts") {
            document.getElementById("oauth2_authz_confirm").submit();
        }
    }

    function validateFrmSTET() {
        if (document.getElementById('type').value === "payments") {
            if (document.getElementById('paymentAccount').value === "null" ||
                document.getElementById('paymentAccount').value === "default") {
                $(".acc-err").show();
                return false;
            } else {
                document.getElementById("oauth2_authz_confirm").submit();
            }
        }
        if (document.getElementById('type').value === "accounts") {
            document.getElementById("oauth2_authz_confirm").submit();
        }
    }

    function reloadPaymentCharges(paymentChargesInfo) {
        $('#payment_charges').html("<center><div class=\"loader\"></div>" +
            "<label class=\"control-label\">Loading payment charges...</label></center>");
        $('#payment_charges').load('payment_charges.jsp', paymentChargesInfo);
    }

    function reloadCrontoImage(loggedInUser, consentId, app) {
        $('#cronto-image').html("<center><div class=\"loader\"></div>" +
            "<label class=\"control-label\">Loading CRONTO image...</label></center>");
        $('#cronto-image').load('oauth2_authz_vasco_cronto.jsp', {
            loggedInUser: loggedInUser,
            consentId: consentId,
            app: app
        });
        document.getElementById("approve").disabled = false;
    }

    function reloadCrontoImageDynamic(loggedInUser, consentId, app, paymentDetails, type) {
        $('#cronto-image').html("<center><div class=\"loader\"></div>" +
            "<label class=\"control-label\">Loading CRONTO image...</label></center>");
        $('#cronto-image').load('oauth2_authz_vasco_cronto.jsp', {
            loggedInUser: loggedInUser,
            consentId: consentId,
            app: app,
            dataset:paymentDetails,
            type:type

        });
        document.getElementById("approve").disabled = false;
    }


    function validateFrm() {
        if (document.getElementById('type').value == "accounts") {

            if (document.getElementById('account').value === "" ||
                document.getElementById('account').value === "default") {
                $(".cronto-err").hide();
                $(".acc-err").show();
                $(".invalid-cronto-err").hide();
                document.getElementById("approve").disabled = false;
                return false;
            } else if (document.getElementById("cronto").value === "") {
                $(".acc-err").hide();
                $(".cronto-err").show();
                $(".invalid-cronto-err").hide();
                document.getElementById("approve").disabled = false;
                return false;
            } else {
                document.getElementById("oauth2_authz_confirm").submit();
            }
        }

        if (document.getElementById('type').value == "payments") {
            if (document.getElementById('paymentAccount').value === "" ||
                document.getElementById('paymentAccount').value === "default") {
                $(".cronto-err").hide();
                $(".acc-err").show();
                $(".invalid-cronto-err").hide();
                document.getElementById("approve").disabled = false;
                return false;
            } else if (document.getElementById("cronto").value === "") {
                $(".acc-err").hide();
                $(".cronto-err").show();
                $(".invalid-cronto-err").hide();
                document.getElementById("approve").disabled = false;
                return false;
            } else if ($("#bankChargesErrorDiv").length==1) {
                $("#bankChargesErrorDiv").html("<p>You can not approve the consent without bank charges!!</p>");
                document.getElementById("approve").disabled = false;
                return false;
            } else {
                document.getElementById("oauth2_authz_confirm").submit();
            }
        }

        if (document.getElementById('type').value == "fundsconfirmations") {
            if (document.getElementById('fundsconfirmation').value === "" || document.getElementById('fundsconfirmation').value === "default") {
                if (document.getElementById("cronto").value === "") {
                    $(".acc-err").hide();
                    $(".cronto-err").show();
                    $(".invalid-cronto-err").hide();
                    document.getElementById("approve").disabled = false;
                    return false;
                } else {
                    document.getElementById("oauth2_authz_confirm").submit();
                }
            }
        }
    }

    function getDataFromServer() {
        $.ajax({
            method: "GET",
            url: "https://localhost:9443/open-banking-berlin/services/accounts/accounts",
            headers: {
                'X-Request-ID': '3469799e-470b-11e8-842f-0ed5f89f731e',
                'Consent-ID': '6a1fa30d-5137-4437-82f2-a3a8950df59e'
            },
            success: function (data) {

                $.each(data["accounts"], function (key, value) {
                    $('#accountselectberlin').append($("<li></li>").attr("name", "iban").text(data["accounts"][key]["iban"]));
                });
            }
        });
    }
</script>


<div class="page-content-wrapper">
    <div class="container-fluid ">
        <div class="container">
            <div class="login-form-wrapper">
                <div class="row">
                    <div class="col-xs-12 col-sm-12 col-md-3 col-lg-3">
                        <div class="brand-container add-margin-bottom-5x">
                            <div class="row">
                                <div class="col-xs-6 col-sm-3 col-md-9 col-lg-9 center-block float-remove-sm
                                float-remove-xs pull-right-md pull-right-lg">
                                    <img src="images/Al_Rayan_Logo-150min.jpg"
                                         class="img-responsive brand-spacer login-logo" alt="WSO2 Open Banking"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <% if (isDefaultConsent) {%>
                    <div class="col-md-12">

                        <!-- content -->
                        <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-5
                        col-centered wr-content wr-login col-centered">
                            <div>
                                <h2 class="wr-title uppercase blue-bg padding-double
                                white boarder-bottom-blue margin-none">
                                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "openid.user.claims")%>
                                </h2>
                            </div>

                            <div class="boarder-all ">
                                <div class="clearfix"></div>
                                <div class="padding-double login-form">
                                    <form action="../oauth2/authorize" method="post" id="profile" name="oauth2_authz"
                                          class="form-horizontal">

                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <div class="alert alert-warning" role="alert">
                                                <p class="margin-bottom-double">
                                                    <strong><%=Encode.forHtml(appReadableName)%>
                                                    </strong>
                                                    </strong>
                                                    <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                            "request.access.profile")%>
                                                </p>
                                            </div>
                                        </div>

                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <% if (userClaimsConsentOnly) {
                                                // If we are getting consent for user claims only we
                                                // don't need to display OIDC
                                                // scopes in the consent page
                                            } else {%>
                                            <%
                                                if (displayScopes && StringUtils.isNotBlank(scopeString)) {
                                                    // Remove "openid" from the scope list to display.
                                                    List<String> openIdScopes = Stream.of(scopeString.split(" "))
                                                            .filter(x -> !StringUtils.equalsIgnoreCase(x, "openid"))
                                                            .collect(Collectors.toList());

                                                    if (CollectionUtils.isNotEmpty(openIdScopes)) {
                                            %>
                                            <h5 class="section-heading-5">
                                                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "requested.scopes")%>
                                            </h5>
                                            <div class="border-gray" style="border-bottom: none;">
                                                <ul class="scopes-list padding">
                                                    <%
                                                        for (String scopeID : openIdScopes) {
                                                    %>
                                                    <li><%=Encode.forHtml(scopeID)%>
                                                    </li>
                                                    <%
                                                        }
                                                    %>
                                                </ul>
                                            </div>
                                            <%
                                                    }
                                                } %>

                                            <div class="border-gray margin-bottom-double">
                                                <div class="padding">
                                                    <div class="radio">
                                                        <label>
                                                            <input type="radio" name="scope-approval" id="approveCb"
                                                                   value="approve">
                                                            Approve Once
                                                        </label>
                                                    </div>
                                                    <div class="radio">
                                                        <label>
                                                            <input type="radio" name="scope-approval"
                                                                   id="approveAlwaysCb" value="approveAlways">
                                                            Approve Always
                                                        </label>
                                                    </div>
                                                </div>
                                            </div>


                                            <%
                                                }
                                            %>
                                        </div>
                                        <!-- Prompting for consent is only needed if we have mandatory or requested
                                        claims without any consent -->
                                        <% if (ArrayUtils.isNotEmpty(mandatoryClaimList) ||
                                                ArrayUtils.isNotEmpty(requestedClaimList)) { %>
                                        <input type="hidden" name="user_claims_consent" id="user_claims_consent"
                                               value="true"/>
                                        <!-- validation -->
                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <h5 class="section-heading-5">
                                                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "requested.attributes")%>
                                            </h5>
                                            <div class="border-gray margin-bottom-double">
                                                <div class="claim-alert" role="alert">
                                                    <p class="margin-bottom-double">
                                                        <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                                "by.selecting.following.attributes")%>
                                                    </p>
                                                </div>
                                                <div class="padding">
                                                    <div class="select-all">
                                                        <div class="checkbox">
                                                            <label>
                                                                <input type="checkbox" name="consent_select_all"
                                                                       id="consent_select_all"/>
                                                                Select All
                                                            </label>
                                                        </div>
                                                    </div>
                                                    <div class="claim-list">
                                                        <% for (String claim : mandatoryClaimList) {
                                                            String[] mandatoryClaimData = claim.split("_", 2);
                                                            if (mandatoryClaimData.length == 2) {
                                                                String claimId = mandatoryClaimData[0];
                                                                String displayName = mandatoryClaimData[1];
                                                        %>
                                                        <div class="checkbox claim-cb">
                                                            <label>
                                                                <input class="mandatory-claim" type="checkbox"
                                                                       name="consent_<%=Encode.forHtmlAttribute(claimId)%>"
                                                                       id="consent_<%=Encode.forHtmlAttribute(claimId)%>"
                                                                       required/>
                                                                <%=Encode.forHtml(displayName)%>
                                                                <span class="required font-medium">*</span>
                                                            </label>
                                                        </div>
                                                        <%
                                                                }
                                                            }
                                                        %>
                                                        <% for (String claim : requestedClaimList) {
                                                            String[] requestedClaimData = claim.split("_", 2);
                                                            if (requestedClaimData.length == 2) {
                                                                String claimId = requestedClaimData[0];
                                                                String displayName = requestedClaimData[1];
                                                        %>
                                                        <div class="checkbox claim-cb">
                                                            <label>
                                                                <input type="checkbox"
                                                                       name="consent_<%=Encode.forHtmlAttribute(claimId)%>"
                                                                       id="consent_<%=Encode.forHtmlAttribute(claimId)%>"/>
                                                                <%=Encode.forHtml(displayName)%>
                                                            </label>
                                                        </div>
                                                        <%
                                                                }
                                                            }
                                                        %>
                                                    </div>
                                                    <div class="text-left padding-top-double">
                                                        <span class="mandatory">
                                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                                    "mandatory.claims.recommendation")%></span>
                                                        <span class="required font-medium">( * )</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <% } %>
                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <div class="alert alert-warning padding-10 margin-bottom-double"
                                                 role="alert">
                                                <div>
                                                    <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                            "privacy.policy.privacy.short.description.approving")%>
                                                    <a href="privacy_policy.do" target="policy-pane">
                                                        <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                                "privacy.policy.general")%>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <table width="100%" class="styledLeft margin-top-double">
                                                <tbody>
                                                <tr>
                                                    <td class="buttonRow" colspan="2">
                                                        <input type="hidden"
                                                               name="<%=Constants.SESSION_DATA_KEY%>"
                                                               value="<%=Encode.forHtmlAttribute(sessionDataKey)%>"/>
                                                        <input type="hidden" name="consent" id="consent" value="deny"/>
                                                        <div style="text-align:left;">
                                                            <input type="button" class="btn  btn-primary" id="approve"
                                                                   name="approve"
                                                                   onclick="approvedDefaultClaim(); return false;"
                                                                   value="<%=AuthenticationEndpointUtil.i18n(resourceBundle,"continue")%>"/>
                                                            <input class="btn" type="reset"
                                                                   onclick="denyDefaultClaim(); return false;"
                                                                   value="<%=AuthenticationEndpointUtil.i18n(resourceBundle,"deny")%>"/>
                                                        </div>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </form>
                                    <div class="clearfix"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="col-xs-12 col-sm-12 col-md-9 col-lg-9 data-container">
                        <div class="alert alert-danger acc-err" style="display:none">Please select an Account.</div>

                        <%
                            if (CommonConstants.UK_SPEC_NAME.equals(APIDataRetriever.getDeployedSpec())) {
                                long unixTimestamp = Instant.now().getEpochSecond();
                        %>
                        <%
                            String intentSubText = "";
                            if (applicatioOnBehalfOf.isPresent()){
                                if(!appReadableName.startsWith(applicatioOnBehalfOf.get())){
                                    intentSubText = "(on behalf of " + applicatioOnBehalfOf.get() + ")";
                                }
                            }
                        %>
                        <%
                            if (requestObject == null) {
                                if (scopeArray != null && scopeArray.length > 0 &&
                                        Arrays.asList(scopeArray).contains("consentmgt")) {
                        %>
                        <form action="../oauth2/authorize"method="post" id="oauth2_authz" name="oauth2_authz"
                              class="form-horizontal auto-submit">
                            <div class="form-group">
                                <div class="col-md-12">
                                    <input type="button" class="btn btn-primary" id="approve" name="approve"
                                           onclick="javascript: approved(); return false;"
                                           value="Continue"/>
                                    <input type="hidden" name="<%=Constants.SESSION_DATA_KEY%>"
                                           value="<%=Encode.forHtmlAttribute(request
                                               .getParameter(Constants.SESSION_DATA_KEY))%>"/>
                                    <input type="hidden" name="consent" id="consent"
                                           value="approve"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                    <div class="well policy-info-message" role="alert margin-top-5x">
                                        <div>
                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                    "privacy.policy.privacy.short.description")%>
                                            <a href="privacy_policy.do" target="policy-pane">
                                                <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                        "privacy.policy.general")%>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                        <%
                        } else {
                        %>
                        <div>
                            <p><strong>No consent requested.</strong></p>
                        </div>
                        <%
                            }
                        } else if (scopeArray != null && scopeArray.length > 0 && Arrays.asList(scopeArray).contains("payments")) {

                            OBReportingDataService OBReportingDataService = AnalyticsUtil.getAnalyticsDataRetrieverService();
                            PaymentRequest paymentRequest = new PaymentRequest();
                            APIDataRetriever apiDataRetriever = APIDataRetriever.getApiDataRetriever();
                            UKAPIDataRetriever ukapiDataRetriever = (UKAPIDataRetriever) apiDataRetriever;
                            ukapiDataRetriever.setUserId(loggedInUser);
                            Map<String, Object> paymentDataSet = apiDataRetriever.getPaymentDataSet(requestObject,app);
                            String paymentInitiationData;
                            String selectedAccount = "";
                            String consentId;
                            boolean isSingle = true;
							String paymentAmount = "";
                            if ("false".equals(paymentDataSet.get(IS_ERROR))) {
                                paymentInitiationData = (String) paymentDataSet.get("paymentInitiationData");
                                paymentDatasetForVasco = Base64Utils.encode(paymentInitiationData.getBytes()).replaceAll("\n","");;
                                consentId = (String) paymentDataSet.get("consent_id");
                                session.setAttribute("spec_version", (String) paymentDataSet.get("spec_version"));
                                paymentChargesRequestInfo = (PaymentChargesRequestInfo) paymentDataSet.get("paymentChargesRequestData");
                            } else {
                                String isError = (String) paymentDataSet.get(IS_ERROR);
                                session.invalidate();
                                response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                return;
                            }

                            if (paymentDataSet.containsKey("MultiAuthType")) {

                                session.setAttribute("MultiAuthType", paymentDataSet.get("MultiAuthType"));
                                session.setAttribute("MultiAuthExpiry", paymentDataSet.get("MultiAuthExpiry"));

                                isSingle = paymentDataSet.get("MultiAuthType").equals("Single");

                                Map<String, Object> authData = MultipleAuthenticationDataRetriever.
                                        getMultipleAuthorizationSession(consentId, loggedInUser);

                                String isError = (String)
                                        authData.get(MultipleAuthenticationDataRetriever.Keys.IS_ERROR);

                                if (!"false".equals(isError)) {
                                    response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                }

                                selectedAccount = (String) authData
                                        .getOrDefault(MultipleAuthenticationDataRetriever.Keys.SELECTED_ACCOUNT, "");
                            }

                            if (UKSpecConfigParser.getInstance().isMAEnabled()) {
                                paymentRequest.setConsentId(consentId);
                                paymentRequest.setUserId(loggedInUser);
                                paymentRequest.setClientId(clientId);
                                paymentRequest.setTimestamp(unixTimestamp);
                                paymentRequest.setAuthorisationStatus("AuthorisationRequired");
                                paymentRequest.setMultiAuth(false);

                                OBReportingDataService.publishPaymentsDataToAnalytics(paymentRequest);
                            }


                        %>
                        <%--<div class="boarder-all data-container">--%>
                        <div class="clearfix"></div>
                        <form action="oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
                              name="oauth2_authz_confirm"
                              class="form-horizontal">
                            <div class="login-form">
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <h3><strong><%=Encode.forHtml(appReadableName)%>
                                        </strong> requests consent to do a payment transaction <%=Encode.forHtml(intentSubText)%>
                                        </h3>
                                        <p>See details below</p>

                                        <%
                                            if (displayScopes && paymentInitiationData != null) {
                                                JSONArray jaPaymentInitiationData = new JSONArray(paymentInitiationData);
                                        %>
                                        <ul class="scope">
                                            <%
                                                for (int i = 0; i < jaPaymentInitiationData.length(); i++) {
                                            %>
                                            <li><%=Encode.forHtml(jaPaymentInitiationData.getString(i))%>
                                            </li>
                                            <%
													if (jaPaymentInitiationData.getString(i).contains("Instructed Amount")){
															String[] amount = jaPaymentInitiationData.getString(i).split("Instructed Amount : ");
															paymentAmount = amount[1];
													}
                                                    if (jaPaymentInitiationData.getString(i).contains("Payment Type")){
                                                            String[] paymentsType = jaPaymentInitiationData.getString(i).split("Payment Type : ");
                                                            paymentType = paymentsType[1];
                                                    }
                                                }
                                            %>
                                        </ul>
                                        <%--Payment Charges--%>
                                        <div id="payment_charges">
                                        </div>
                                        <%
                                            }
                                        %>
                                    </div>
                                </div>

                                <%
                                    Map<String, String> parameters = new HashMap<>();
                                    parameters.put(USER_ID_KEY_NAME, loggedInUser);
                                    parameters.put(CONSENT_ID_KEY_NAME, consentId);
                                    String payableAccounts = DebtorAccountRetriever.getPayableAccounts(parameters,app);
                                    if(payableAccounts== null) {
                                        String isError = "This PSU doesnt have any Payable Acoounts";
                                        session.invalidate();
                                        response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                        return;
                                    }
                                    JSONObject payableAccountsJson = new JSONObject(payableAccounts);
                                    JSONArray payableAccountsArray = payableAccountsJson.getJSONArray("data");

                                    if (paymentDataSet.containsKey("debtor_account")) {
                                        DebtorAccount debtorAccount = (DebtorAccount) paymentDataSet.get("debtor_account");
                                        selectedAccount = debtorAccount.getIdentification();
                                        String schemeName = debtorAccount.getSchemeName();
                                    // Account is valid.

                				    for (int i = 0; i < payableAccountsArray.length(); i++) {
                                        JSONObject object = payableAccountsArray.getJSONObject(i);
                                                                String account_id = object.get("account_id").toString();
                					    System.out.println("-----------account_id----------------"+i);
                						if(account_id.equals(selectedAccount)){
                                            System.out.println("-----------Inside if----------------");
                						    break;
                						}
                                        if((i+1)==payableAccountsArray.length() && !(account_id.equals(selectedAccount))){
                							String isError = "This account has not required permission to proceed";
                                            session.invalidate();
                                            response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                            return;
                						}
                					}
                                %>
                                <hr />
                                <ul class="scope">
                                    <li><%=AuthenticationEndpointUtil.i18n(resourceBundle, "account.id") + ": " + selectedAccount%></li>
                                    <li><%=AuthenticationEndpointUtil.i18n(resourceBundle, "account.scheme.name") + ": " + schemeName%></li>
                                </ul>
                                <input type="hidden" name="accountId" id="accountId" value="<%= paymentDataSet.get("debtorSortCodeAcc")%>">
                                <%
                                } else {
                                %>
                                <div class="form-group">
                                    <!--label for="accselect">Select Account:</label-->
                                        <select <%= StringUtils.isNotBlank(selectedAccount) ? "disabled" : "" %>
                                                class="form-control" id="payaccselect"
                                                data-payment='<%= new ObjectMapper().writeValueAsString(paymentChargesRequestInfo)%>'
                                                data-appName='<%= app %>'
                                                onchange="updatePaymentAcc(this);
                                                reloadPaymentCharges({
                                                        paymentChargesRequestInfo: $(this).attr('data-payment'),
                                                        appName: $(this).attr('data-appName'),
                                                        accountId: this.value,
                                                        paymentType: '<%= paymentType %>',
                                                        consentID: '<%=Encode.forHtml(consentId)%>'
                                                  })">

                                        <option value="default"
                                                <%= !StringUtils.isNotBlank(selectedAccount) ? "selected" : "" %>>
                                            Select Account:
                                        </option>
                                        <%
                                            for (int i = 0; i < payableAccountsArray.length(); i++) {
                                                JSONObject object = payableAccountsArray.getJSONObject(i);
                                                String account_id = object.get("account_id").toString();
                                                String display_name = object.get("display_name").toString();
                                                boolean isSelected = selectedAccount.equals(account_id);
                                                // Hide Multiple authorization accounts if Authentication method is single
                                                if (isSingle && object.getString("authorizationMethod").equals("multiple")) {

                                                    continue;
                                                }

                                        %>
                                        <option <%= isSelected ? "selected" : "" %>
                                                value="<%=account_id%>"><%=display_name%>
                                        </option>
                                        <%
                                            }
                                        %>
                                    </select>
                                </div>
                                <%}%>

                                <div class="form-group">
                                    <div class="col-md-12">
                                        <div class="col-md-6">
                                            <%--Cronto image--%>
                                            <div id="cronto-image">
                                            </div>
                                            <div>
                                                <p> Note: The CRONTO image expires every two minutes. Please reload if required. </p>
                                            </div>
                                                <a href="javascript:reloadCrontoImageDynamic('<%= loggedInUser %>',
                                                $('#id').val(),
                                                $('#app').val(),
                                                '<%= paymentDatasetForVasco%>',
                                                $('#type').val());">
                                                    Reload CRONTO image
                                                </a>
                                        </div>


                                        <div class="col-md-6">
                                            <table width="100%" class="styledLeft">
                                                <tbody>
                                                <tr class="buttonRow">
                                                    <td>
                                                        <%--Cronto image errors --%>
                                                        <div class="alert alert-danger cronto-validate-error" style="display:none">
                                                            Internal sever error on validating CRONTO. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.<br /><br />
                                                            If the error persists, please contact the bank support.
                                                        </div>
                                                        <div class="alert alert-danger cronto-validate-error-unknown" style="display:none">
                                                            Unknown error on validating CRONTO. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.<br /><br />
                                                            If the error persists, please contact the bank support.
                                                        </div>
                                                        <div class="alert alert-danger cronto-err" style="display:none">
                                                            Please enter the VASCO CRONTO code.
                                                        </div>
                                                        <div class="alert alert-danger invalid-cronto-err" style="display:none">
                                                            CRONTO code is incorrect. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.
                                                        </div>
                                                        <label class="control-label">
                                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle, "enter.cronto.code")%>
                                                        </label>
                                                        <input type="text" class="form-control"
                                                               name="cronto"
                                                               id="cronto"
                                                               placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "cronto.code")%>">
                                                    </td>
                                                </tr>
                                                <tr class="buttonRow">
                                                    <td>&nbsp;&nbsp;</td>
                                                </tr>
                                                <tr class="buttonRow">
                                                    <td>
                                                        <input type="button" class="btn btn-primary" id="approve" name="approve"
                                                               onclick="javascript: approved(); return false;"
                                                               value="Approve"/>
                                                        <input class="btn btn-secondary" type="reset" value="Deny"
                                                               onclick="javascript: deny(); return false;"/>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>

                                            <input type="hidden" name="<%=Constants.SESSION_DATA_KEY%>"
                                                   value="<%=Encode.forHtmlAttribute(request
                                               .getParameter(Constants.SESSION_DATA_KEY))%>"/>
                                            <input type="hidden" name="consent" id="consent"
                                                   value="deny"/>
                                            <input type="hidden" name="app" id="app" value="<%=Encode.forHtml(app)%>"/>
                                            <input type="hidden" name="paymentAccount" id="paymentAccount"
                                                   value="<%= selectedAccount %>"/>
											<input type="hidden" name="paymentAmount" id="paymentAmount"
													value="<%= paymentAmount %>"/>
											<input type="hidden" name="callback_url" id="callback_url"
													   value="<%= callbackUrl %>"/>
                                            <input type="hidden" name="id" id="id" value="<%=Encode.forHtml(consentId)%>"/>
                                            <input type="hidden" name="user" id="user"
                                                   value="<%=Encode.forHtml(loggedInUser)%>"/>
                                            <input type="hidden" name="type" id="type" value="payments"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                        <div class="well policy-info-message" role="alert margin-top-5x">
                                            <div>
                                                <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                        "privacy.policy.privacy.short.description")%>
                                                <a href="privacy_policy.do" target="policy-pane">
                                                    <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                            "privacy.policy.general")%>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <%--</div>--%>
                        <%
                        } else if (scopeArray != null && scopeArray.length > 0 &&
                                Arrays.asList(scopeArray).contains("accounts")) {
                            APIDataRetriever apiDataRetriever = APIDataRetriever.getApiDataRetriever();
                            Map<String, Object> accountDataSet = apiDataRetriever.getAccountDataSet(requestObject);
                            OBReportingDataService OBReportingDataService = AnalyticsUtil.getAnalyticsDataRetrieverService();
                            AccountRequest accountRequest = new AccountRequest();
                            String permissions;
                            String dates;
                            String consentId;
                            boolean isReauthorization = false;
                            boolean isReauthAccountUpdateEnabled = false;
                            ArrayList<String> reauthSelectedAccounts;
                            if ("false".equals(accountDataSet.get(IS_ERROR))) {
                                permissions = (String) accountDataSet.get("Permissions");
                                dates = (String) accountDataSet.get("dates");
                                consentId = (String) accountDataSet.get("consent_id");
                                isReauthorization = (Boolean) accountDataSet.get("isReauthorization");
                                isReauthAccountUpdateEnabled = (Boolean) accountDataSet.get("isReauthAccountUpdateEnabled");
                                reauthSelectedAccounts = (ArrayList<String>) accountDataSet.get("reauthSelectedAccount");

                                if (UKSpecConfigParser.getInstance().isMAEnabled()) {
                                    accountRequest.setConsentId(consentId);
                                    accountRequest.setUserId(loggedInUser);
                                    accountRequest.setClientId(clientId);
                                    accountRequest.setReAuth(isReauthorization);
                                    accountRequest.setAuthorisationStatus("AuthorisationRequired");
                                    accountRequest.setTimestamp(unixTimestamp);

                                    OBReportingDataService.publishAccountsDataToAnalytics(accountRequest);
                                }

                            } else {
                                String isError = (String) accountDataSet.get(IS_ERROR);
                                session.invalidate();
                                response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                return;
                            }
                        %>
                        <%--<div class="boarder-all data-container">--%>
                        <div class="clearfix"></div>
                        <form action="oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
                              name="oauth2_authz_confirm"
                              class="form-horizontal">
                            <div class="login-form">
                                <div class="form-group">
                                    <div class="col-md-12">

                                        <h3><strong><%=Encode.forHtml(appReadableName)%>
                                        </strong> requests account details on your account. <%=Encode.forHtml(intentSubText)%>
                                        </h3>
                                        <p>See permission details below</p>

                                        <%
                                            if (displayScopes && permissions != null) {
                                                JSONArray jaPermissions = new JSONArray(permissions);
                                        %>
                                        <ul class="scope">
                                            <%
                                                for (int i = 0; i < jaPermissions.length(); i++) {
                                            %>
                                            <li><%=Encode.forHtml(jaPermissions.getString(i))%>
                                            </li>
                                            <%
                                                }
                                            %>
                                        </ul>
                                        <%
                                            }
                                        %>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <%
                                            if (displayScopes && dates != null) {
                                                JSONArray jaDates = new JSONArray(dates);
                                        %>
                                        <ul class="scope">
                                            <%
                                                for (int i = 0; i < jaDates.length(); i++) {
                                            %>
                                            <li><%=Encode.forHtml(jaDates.getString(i))%>
                                            </li>
                                            <%
                                                }
                                            %>
                                        </ul>
                                        <%
                                            }
                                        %>
                                    </div>
                                </div>

                                <%
                                    if (isReauthorization) {
                                        ArrayList<String> account_display_names = new ArrayList<>();
                                %>
                                <div class="form-group" onload="setReAuthAccounts()">
                                    <div class="col-md-12">
                                        <h3><strong>You are about to re-authorize an intent which had been authorized
                                            before.</strong></h3>
                                        <%
                                            if (!isReauthAccountUpdateEnabled) {
                                        %>
                                        <strong>Note: Account selection during
                                            re-authentication is disabled by your
                                            ASPSP.</strong>
                                        <select style="display:none;" multiple name="accounts[]" class="form-control" id="accselect2">
                                            <%
                                                Map<String, String> parameters = new HashMap<>();
                                                parameters.put(USER_ID_KEY_NAME, loggedInUser);
                                                parameters.put(CONSENT_ID_KEY_NAME, consentId);
                                                String sharableAccounts = DebtorAccountRetriever.getSharableAccounts(parameters,app);
                                                if(sharableAccounts== null) {
                                                    String isError = "This PSU doesnt have any Sharable Accounts";
                                                    session.invalidate();
                                                    response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                                    return;
                                                }
                                                JSONObject sharableAccountsJson = new JSONObject(sharableAccounts);
                                                JSONArray sharableAccountsArray = sharableAccountsJson.getJSONArray("data");
                                                for (String selectedAccount : reauthSelectedAccounts) {
                                                    for (int i = 0; i < sharableAccountsArray.length(); i++) {
                                                        JSONObject accountObject = sharableAccountsArray.getJSONObject(i);
                                                        String account_id = accountObject.get("account_id").toString();
                                                        String display_name = accountObject.get("display_name").toString();
                                                        if(account_id.equals(selectedAccount)) {
                                                            account_display_names.add(display_name);
                                            %>

                                            <option hidden selected value="<%=selectedAccount%>"></option>
                                            <%
                                                            break;
                                                        }
                                                    }
                                                }
                                            %>
                                        </select>
                                        <p style="background-color:#ff1015;">
                                            <%
                                                if (account_display_names.size() == 0) {
                                            %>
                                            <strong>This Consent is not re-authorizable</strong>
                                            <%
                                            } else {
                                            %>
                                        </p>
                                        <ul>
                                            <%
                                                for (String display_name : account_display_names) {
                                            %>
                                            <li>
                                                <%=display_name%>
                                            </li>
                                            <%
                                                }
                                            %>
                                        </ul>
                                        <%
                                                }
                                            }
                                        %>
                                    </div>
                                </div>
                                <%
                                    }
                                    if (!isReauthorization || (isReauthorization && isReauthAccountUpdateEnabled)) {
                                %>
                                <div class="form-group">
                                    <div class="col-md-12">
                                        <select multiple name="accounts[]" class="form-control" id="accselect"
                                                onchange="updateAcc(this)">
                                            <option value="default">Select Account:</option>
                                            <%
                                                Map<String, String> parameters = new HashMap<>();
                                                parameters.put(USER_ID_KEY_NAME, loggedInUser);
                                                parameters.put(CONSENT_ID_KEY_NAME, consentId);
                                                String sharableAccounts = DebtorAccountRetriever.getSharableAccounts(parameters,app);
                                                if(sharableAccounts== null) {
                                                    String isError = "This PSU doesnt have any Sharable Acoounts";
                                                    session.invalidate();
                                                    response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                                    return;
                                                }
                                                JSONObject sharableAccountsJson = new JSONObject(sharableAccounts);
                                                JSONArray sharableAccountsArray = sharableAccountsJson.getJSONArray("data");
                                                for (int i = 0; i < sharableAccountsArray.length(); i++) {
                                                    JSONObject object = sharableAccountsArray.getJSONObject(i);
                                                    String account_id = object.get("account_id").toString();
                                                    String display_name = object.get("display_name").toString();
                                                    String isSelected = reauthSelectedAccounts.contains(account_id) ? "selected" : "";
                                            %>
                                            <option <%=isSelected%> value="<%=account_id%>"><%=display_name%>
                                            </option>
                                            <%
                                                }
                                            %>
                                        </select>
                                    </div>
                                </div>
                                <%
                                    }
                                %>

                                <div class="form-group">
                                    <div class="col-md-12">

                                        <div class="col-md-6">
                                            <%--Cronto image--%>
                                            <div id="cronto-image">
                                            </div>
                                            <div>
                                                <p> Note: The CRONTO image expires every two minutes. Please reload if required. </p>
                                            </div>
                                            <a href="javascript:reloadCrontoImage('<%= loggedInUser %>',
                                                $('#id').val(),
                                                $('#app').val());">
                                                Reload CRONTO image
                                            </a>
                                        </div>
                                        <div class="col-md-6">
                                            <table width="100%" class="styledLeft">
                                                <tbody>
                                                <tr class="buttonRow">
                                                    <td>
                                                        <%--Cronto image errors --%>
                                                        <div class="alert alert-danger cronto-validate-error" style="display:none">
                                                            Internal sever error on validating CRONTO. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.<br /><br />
                                                            If the error persists, please contact the bank support.
                                                        </div>
                                                        <div class="alert alert-danger cronto-validate-error-unknown" style="display:none">
                                                            Unknown error on validating CRONTO. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.<br /><br />
                                                            If the error persists, please contact the bank support.
                                                        </div>
                                                        <div class="alert alert-danger cronto-err" style="display:none">
                                                            Please enter the VASCO CRONTO code.
                                                        </div>
                                                        <div class="alert alert-danger invalid-cronto-err" style="display:none">
                                                            CRONTO code is incorrect. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.
                                                        </div>
                                                        <label class="control-label">
                                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle, "enter.cronto.code")%>
                                                        </label>
                                                        <input type="text" class="form-control"
                                                               name="cronto"
                                                               id="cronto"
                                                               placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "cronto.code")%>">
                                                    </td>
                                                </tr>
                                                <tr class="buttonRow">
                                                    <td>&nbsp;&nbsp;</td>
                                                </tr>
                                                <tr class="buttonRow">
                                                    <td>
                                                        <input type="button" class="btn btn-primary" id="approve" name="approve"
                                                               onclick="javascript: approved(); return false;"
                                                               value="Approve"/>
                                                        <input class="btn btn-secondary" type="reset" value="Deny"
                                                               onclick="javascript: deny(); return false;"/>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways"
                                               value="false"/>
                                        <input type="hidden" name="sessionDataKey"
                                               value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
                                        <input type="hidden" name="isReauthorization"
                                               value="<%=isReauthorization%>"/>
                                        <input type="hidden" name="consent" id="consent"
                                               value="deny"/>
                                        <input type="hidden" name="app" id="app" value="<%=Encode.forHtml(app)%>"/>
                                        <input type="hidden" name="accountsArry[]" id="account" value=""/>
                                        <input type="hidden" name="id" id="id" value="<%=Encode.forHtml(consentId)%>"/>
                                        <input type="hidden" name="user" id="user"
                                               value="<%=Encode.forHtml(loggedInUser)%>"/>
                                        <input type="hidden" name="callback_url" id="callback_url"
                                               value="<%= callbackUrl %>"/>
                                        <input type="hidden" name="type" id="type" value="accounts"/>
                                        </div>

                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                        <div class="well policy-info-message" role="alert margin-top-5x">
                                            <div>
                                                <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                        "privacy.policy.privacy.short.description")%>
                                                <a href="privacy_policy.do" target="policy-pane">
                                                    <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                            "privacy.policy.general")%>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                        <%

                        } else if (scopeArray != null && scopeArray.length > 0 &&
                                Arrays.asList(scopeArray).contains("fundsconfirmations")) {
                            Map<String, Object> fundsConfirmationDataSet =
                                    UKAPIDataRetriever.getApiDataRetriever().getFundsConfirmationDataSet(requestObject);
                            OBReportingDataService OBReportingDataService =
                                    AnalyticsUtil.getAnalyticsDataRetrieverService();
                            String dates;
                            String consentId;
                            boolean isReauthorization = false;
                            if ("false".equals(fundsConfirmationDataSet.get(IS_ERROR))) {
                                dates = (String) fundsConfirmationDataSet.get("dates");
                                consentId = (String) fundsConfirmationDataSet.get("consent_id");
                                isReauthorization = (Boolean) fundsConfirmationDataSet.get("isReauthorization");

                                FundsConfirmationRequest fundsConfirmationRequest = new FundsConfirmationRequest();

                                if (UKSpecConfigParser.getInstance().isMAEnabled()) {
                                    fundsConfirmationRequest.setConsentId(consentId);
                                    fundsConfirmationRequest.setUserId(loggedInUser);
                                    fundsConfirmationRequest.setClientId(clientId);
                                    fundsConfirmationRequest.setAuthorisationStatus("AuthorisationRequired");
                                    fundsConfirmationRequest.setTimestamp(unixTimestamp);
                                    OBReportingDataService.publishFundsConfirmationDataToAnalytics(fundsConfirmationRequest);
                                }

                            } else {
                                String isError = (String) fundsConfirmationDataSet.get(IS_ERROR);
                                session.invalidate();
                                response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                return;
                            }
                            Map<String,Object> exposedData = (Map<String,Object>)fundsConfirmationDataSet.get("exposedData");
                            // Fix defect : logged in user should have the account id mentioned in the request body as DebtorAccount.
                            String debtorAccountIdentification = (String) exposedData.get(IDENTIFICATION);
                            Map<String, String> parameters = new HashMap<>();
                            parameters.put(USER_ID_KEY_NAME, loggedInUser);
                            parameters.put(CONSENT_ID_KEY_NAME, consentId);
                            String payableAccounts = DebtorAccountRetriever.getPayableAccounts(parameters,app);
                            if(payableAccounts== null || debtorAccountIdentification==null || !payableAccounts.contains(debtorAccountIdentification)) {
                                String isError = "Debtor Account doesnt belong to this authorized PSU";
                                session.invalidate();
                                response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                return;
                            }
                        %>
                        <%--<div class="boarder-all data-container">--%>
                        <div class="clearfix"></div>
                        <form action="oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
                              name="oauth2_authz_confirm"
                              class="form-horizontal">
                            <div class="login-form">
                                <div class="form-group">
                                    <div class="col-md-12">

                                        <h3><strong><%=Encode.forHtml(appReadableName)%>
                                        </strong> requests access to confirm the availability of funds in your account. <%=Encode.forHtml(intentSubText)%>
                                        </h3>

                                        <% for (String key : exposedData.keySet()) { %>
                                        <h4><%= key%> : <%=exposedData.get(key)%></h4>
                                        <% } %>

                                    </div>
                                </div>

                                <div class="form-group">
                                    <div class="col-md-12">
                                        <%
                                            if (isReauthorization) {
                                        %>
                                        <strong>Note: You are about to re-authorize an intent
                                            which had been authorized before.<br /></strong>
                                        <%
                                            }
                                        %>

                                        <div class="col-md-6">
                                            <%--Cronto image--%>
                                            <div id="cronto-image">
                                            </div>
                                            <div>
                                                <p> Note: The CRONTO image expires every two minutes. Please reload if required. </p>
                                            </div>
                                            <a href="javascript:reloadCrontoImage('<%= loggedInUser %>',
                                                $('#id').val(),
                                                $('#app').val());">
                                                Reload CRONTO image
                                            </a>
                                        </div>

                                        <div class="col-md-6">
                                            <table width="100%" class="styledLeft">
                                                <tbody>
                                                <tr class="buttonRow">
                                                    <td>
                                                        <%--Cronto image errors --%>
                                                        <div class="alert alert-danger cronto-validate-error" style="display:none">
                                                            Internal sever error on validating CRONTO. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.<br /><br />
                                                            If the error persists, please contact the bank support.
                                                        </div>
                                                        <div class="alert alert-danger cronto-validate-error-unknown" style="display:none">
                                                            Unknown error on validating CRONTO. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.<br /><br />
                                                            If the error persists, please contact the bank support.
                                                        </div>
                                                        <div class="alert alert-danger cronto-err" style="display:none">
                                                            Please enter the VASCO CRONTO code.
                                                        </div>
                                                        <div class="alert alert-danger invalid-cronto-err" style="display:none">
                                                            CRONTO code is incorrect. Please re-scan the CRONTO image and re-enter the CRONTO code.<br />
                                                            The CRONTO image is re-generated.
                                                        </div>
                                                        <label class="control-label">
                                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle, "enter.cronto.code")%>
                                                        </label>
                                                        <input type="text" class="form-control"
                                                               name="cronto"
                                                               id="cronto"
                                                               placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "cronto.code")%>">
                                                    </td>
                                                </tr>
                                                <tr class="buttonRow">
                                                    <td>&nbsp;&nbsp;</td>
                                                </tr>
                                                <tr class="buttonRow">
                                                    <td>
                                                        <input type="button" class="btn btn-primary" id="approve" name="approve"
                                                               onclick="javascript: approved(); return false;"
                                                               value="Approve"/>
                                                        <input class="btn btn-secondary" type="reset" value="Deny"
                                                               onclick="javascript: deny(); return false;"/>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>

                                        <input type="hidden" name="sessionDataKey"
                                               value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
                                        <input type="hidden" name="consent" id="consent"
                                               value="deny"/>
                                        <input type="hidden" name="isReauthorization" id="isReauthorization"
                                               value="<%=isReauthorization%>"/>
                                        <input type="hidden" name="app" id="app" value="<%=Encode.forHtml(app)%>"/>
                                        <input type="hidden" name="fundsconfirmation" id="fundsconfirmation" value=""/>
                                        <input type="hidden" name="id" id="id" value="<%=Encode.forHtml(consentId)%>"/>
                                        <input type="hidden" name="user" id="user"
                                               value="<%=Encode.forHtml(loggedInUser)%>"/>
                                        <input type="hidden" name="callback_url" id="callback_url"
                                                      value="<%= callbackUrl %>"/>
                                        <input type="hidden" name="type" id="type" value="fundsconfirmations"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                        <div class="well policy-info-message" role="alert margin-top-5x">
                                            <div>
                                                <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                        "privacy.policy.privacy.short.description")%>
                                                <a href="privacy_policy.do" target="policy-pane">
                                                    <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                            "privacy.policy.general")%>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <%
                        } else {
                        %>
                        <div>
                            <p><strong>No consent requested....</strong></p>
                        </div>
                        <%
                            }
                        %>
                        <%
                        } else if (CommonConstants.BERLIN_SPEC_NAME.equals(APIDataRetriever.getDeployedSpec())) {
                            for (String param : spQueryParamList) {
                                if (param.startsWith("client_id")) {
                                    clientId = param.split("=")[1];
                                }
                            }
                            String consentId = null;
                            for (String parameter : scopeArray) {
                                if (parameter.startsWith("ais:") || parameter.startsWith("pis:")) {
                                    consentId = parameter;
                                }
                            }

                            String requestdata = clientId + ":" + consentId + ":" + loggedInUser;

                            if (scopeArray != null && scopeArray.length > 0 &&
                                    Arrays.asList(scopeArray).contains("accounts")) {

                                APIDataRetriever apiDataRetrieverAcc = APIDataRetriever.getApiDataRetriever();
                                Map<String, Object> accountDataSet = apiDataRetrieverAcc.getAccountDataSet(requestdata);
                                String permissions;
                                List<AccountReference> transactionIBANlist;
                                List<AccountReference> balanceIBANlist;
                                List<AccountReference> accountsIBANlist;
                                List<String> accountsIBANStringlist = new ArrayList<>();
                                List<String> balanceIBANStringlist = new ArrayList<>();
                                List<String> transactionIBANStringlist = new ArrayList<>();
                                List<String> allIBANStringlist = new ArrayList<>();

                                for (Map.Entry<String, Object> accountEntry : accountDataSet.entrySet()) {
                                    if (accountEntry.getKey().equals("Accounts")) {
                                        accountsIBANlist = (List<AccountReference>) accountEntry.getValue();

                                        for (AccountReference accountReference : accountsIBANlist) {
                                            String iban = accountReference.getIban();
                                            accountsIBANStringlist.add(iban);
                                            allIBANStringlist.add(iban);
                                        }
                                    }
                                }

                                for (Map.Entry<String, Object> transactionentry : accountDataSet.entrySet()) {
                                    if (transactionentry.getKey().equals("Transactions")) {
                                        transactionIBANlist = (List<AccountReference>) transactionentry.getValue();

                                        for (AccountReference accountReference : transactionIBANlist) {
                                            String iban = accountReference.getIban();
                                            transactionIBANStringlist.add(iban);
                                            allIBANStringlist.add(iban);
                                        }
                                    }
                                }

                                for (Map.Entry<String, Object> balancEentry : accountDataSet.entrySet()) {
                                    if (balancEentry.getKey().equals("Balances")) {
                                        balanceIBANlist = (List<AccountReference>) balancEentry.getValue();

                                        for (AccountReference accountReference : balanceIBANlist) {
                                            String iban = accountReference.getIban();
                                            balanceIBANStringlist.add(iban);
                                            allIBANStringlist.add(iban);
                                        }
                                    }
                                }

                                if ("false".equals(accountDataSet.get(IS_ERROR))) {
                                    permissions = accountDataSet.get(PERMISSION).toString();
                                } else {
                                    String isError = (String) accountDataSet.get(IS_ERROR);
                                    session.invalidate();
                                    response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                    return;
                                }
                        %>

                        <div class="clearfix"></div>
                        <form action="oauth2_authz_confirm_berlin.do" method="post" id="oauth2_authz_confirm"
                              name="oauth2_authz_confirm"
                              class="form-horizontal">
                            <div class="login-form">
                                <div class="form-group">
                                    <div class="col-md-12">

                                        <h3><strong><%=Encode.forHtml(appReadableName)%>
                                        </strong> requests account details on your account/s.</h3>
                                        <% if (PermissionEnum.ALL_PSD2.toString().equals(permissions)) { %>

                                        <p><strong> Requested Permissions :  <%=permissions%>
                                        </strong></p>
                                        <div class="form-control" style="color: rgba(45,188,14,0.57)">These permissions
                                            include:
                                        </div>
                                        <li> Read Account Information.</li>
                                        <li> Read Account Balance Information</li>
                                        <li> Read Account Transaction Information</li>
                                        <br>

                                        <script>
                                            getDataFromServer();
                                        </script>

                                        <div class="form-control" style="color: rgba(45,188,14,0.57)">Selected
                                            accounts
                                        </div>
                                        <div class="form-group">
                                            <%--display user accounts--%>
                                            <div id="accountselectberlin">

                                            </div>
                                        </div>

                                        <%
                                        } else if (PermissionEnum.AVAILABLE_ACCOUNTS.toString().equals(permissions)) {
                                        %>
                                        <p><strong> Requested Permissions :  <%=permissions%>
                                        </strong></p>
                                        <div class="form-control" style="color: rgba(45,188,14,0.57)">Required
                                            permissions :
                                        </div>
                                        <li> Read Account Information.</li>
                                        <br>

                                        <script>
                                            getDataFromServer();
                                        </script>

                                        <div class="form-control" style="color: rgba(45,188,14,0.57)">Selected
                                            accounts
                                        </div>
                                        <div class="form-group">
                                            <%--display user accounts--%>
                                            <div id="accountselectberlin">

                                            </div>
                                        </div>

                                        <%
                                        } else if (PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString().equals(permissions)) {
                                        %>
                                        <p><strong> Requested Permissions :  <%=permissions%>
                                        </strong></p>
                                        <div class="form-control" style="color: rgba(45,188,14,0.57)">Required
                                            permissions :
                                        </div>
                                        <li> Read Account Information.</li>
                                        <li> Read Account Balance Information</li>
                                        <br>

                                        <script>
                                            getDataFromServer();
                                        </script>

                                        <div class="form-control" style="color: rgba(45,188,14,0.57)">Selected
                                            accounts
                                        </div>
                                        <div class="form-group">
                                            <%--display user accounts--%>
                                            <div id="accountselectberlin">

                                            </div>
                                        </div>

                                        <%
                                        } else if (PermissionEnum.DEFAULT.toString().equals(permissions)) {
                                            if (accountDataSet.get("Balances") != null &&
                                                    accountDataSet.get("Balances").equals(Collections.emptyList())) {
                                                //show everything here for balance
                                            } else if (accountDataSet.get("Balances") != null) {
                                        %>
                                        <div style="color: rgba(54,249,16,0.57)">Required permissions :</div>
                                        <li> Read Account Information.</li>
                                        <li> Read Account Balance Information</li>
                                        <div style="color: rgba(59,254,18,0.57)">On following accounts</div>
                                        <%
                                            for (Map.Entry<String, Object> entry : accountDataSet.entrySet()) {
                                                if (entry.getKey().equals("Balances")) {
                                                    List<AccountReference> balanceIBANlist2 =
                                                            (List<AccountReference>) entry.getValue();
                                                    for (AccountReference a : balanceIBANlist2) {
                                                        String balanceIBAN = a.getIban();

                                        %>
                                        <li><%=balanceIBAN%>
                                        </li>
                                        <%
                                                        }
                                                    }
                                                }
                                            }
                                            if (accountDataSet.get("Transactions") != null &&
                                                    accountDataSet.get("Transactions").equals(Collections.emptyList())) {
                                                //show everything here for balance
                                            } else if (accountDataSet.get("Transactions") != null) {
                                        %>
                                        <br>
                                        <div style="color: rgba(58,255,15,0.57)">Required permissions :</div>
                                        <li> Read Account Information.</li>
                                        <li> Read Account Transaction Information</li>
                                        <div style="color: rgba(59,255,18,0.57)">On following accounts</div>
                                        <%
                                            for (Map.Entry<String, Object> entry : accountDataSet.entrySet()) {
                                                if (entry.getKey().equals("Transactions")) {
                                                    List<AccountReference> transactionIBANlist2 =
                                                            (List<AccountReference>) entry.getValue();
                                                    for (AccountReference a : transactionIBANlist2) {
                                                        String transactionIBAN = a.getIban();

                                        %>
                                        <li><%=transactionIBAN%>
                                        </li>
                                        <%
                                                        }
                                                    }
                                                }
                                            }

                                            if (accountDataSet.get("Accounts") != null &&
                                                    accountDataSet.get("Accounts").equals(Collections.emptyList())) {
                                                //show everything here for balance
                                            } else if (accountDataSet.get("Accounts") != null) {
                                        %>
                                        <div style="color: rgba(54,249,16,0.57)">Required permissions :</div>
                                        <li> Read Account Information.</li>
                                        <div style="color: rgba(59,254,18,0.57)">On following accounts</div>
                                        <%
                                            for (Map.Entry<String, Object> entry : accountDataSet.entrySet()) {
                                                if (entry.getKey().equals("Accounts")) {
                                                    List<AccountReference> accountsIBANlist2 =
                                                            (List<AccountReference>) entry.getValue();
                                                    for (AccountReference acc : accountsIBANlist2) {
                                                        String accountIBAN = acc.getIban();

                                        %>
                                        <li><%=accountIBAN%>
                                        </li>
                                        <%
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        %>

                                    </div>
                                </div>

                            </div>
                            <div class="form-group">
                                <div class="col-md-12" id="accountConsentDetails">
                                    <input type="button" class="btn btn-primary" id="approveAcc" name="approve"
                                           onclick="javascript: approvedBerlin(); return false;"
                                           value="Approve"/>
                                    <input class="btn btn-secondary" type="reset" value="Deny"
                                           onclick="javascript: deny(); return false;"/>

                                    <input type="hidden" name="<%=Constants.SESSION_DATA_KEY%>"
                                           value="<%=Encode.forHtmlAttribute(request
                                               .getParameter(Constants.SESSION_DATA_KEY))%>"/>
                                    <input type="hidden" name="consent" id="consent" value="deny"/>
                                    <input type="hidden" name="app" id="app" value="<%=Encode.forHtml(app)%>"/>
                                    <input type="hidden" name="id" id="id"
                                           value="<%=Encode.forHtml(consentId.split(":")[1])%>"/>
                                    <input type="hidden" name="tppUniqueId" id="tppUniqueId"
                                           value="<%=Encode.forHtml(clientId)%>"/>
                                    <input type="hidden" name="user" id="user"
                                           value="<%=Encode.forHtml(loggedInUser)%>"/>
                                    <input type="hidden" name="type" id="type" value="accounts"/>
                                    <input type="hidden" name="permission" id="permission" value="<%=permissions%>"/>
                                    <input type="hidden" name="transactionsList" id="transactionsList"
                                           value="<%=transactionIBANStringlist%>">
                                    <input type="hidden" name="balancesList" id="balancesList"
                                           value="<%=balanceIBANStringlist%>">
                                    <input type="hidden" name="accountsList" id="accountsList"
                                           value="<%=accountsIBANStringlist%>">
                                    <input type="hidden" name="callback_url" id="callback_url"
                                           value="<%= callbackUrl %>"/>
                                    <input type="hidden" name="allaccountsList" id="allaccountsList"
                                           value="<%=allIBANStringlist%>">

                                </div>
                            </div>
                        </form>
                    </div>

                    <%
                        }
                        if (scopeArray != null && scopeArray.length > 0 &&
                                Arrays.asList(scopeArray).contains("payments")) {
                            APIDataRetriever apiDataRetriever = APIDataRetriever.getApiDataRetriever();
                            Map<String, Object> paymentDataSet = apiDataRetriever.getPaymentDataSet(requestdata,app);
                            String paymentInitiationData;
                            if ("false".equals(paymentDataSet.get(IS_ERROR))) {
                                paymentInitiationData = (String) paymentDataSet.get("paymentInitiationData");
                            } else {
                                String isError = (String) paymentDataSet.get(IS_ERROR);
                                session.invalidate();
                                response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                return;
                            }
                    %>

                    <div class="clearfix"></div>
                    <form action="oauth2_authz_confirm_berlin.do" method="post" id="oauth2_authz_confirm"
                          name="oauth2_authz_confirm"
                          class="form-horizontal">
                        <div class="login-form">
                            <div class="form-group">
                                <div class="col-md-12">
                                    <h3><strong><%=Encode.forHtml(appReadableName)%>
                                    </strong> requests consent to do a payment transaction</h3>
                                    <p>See details below</p>

                                    <%
                                        if (displayScopes && paymentInitiationData != null) {
                                            JSONArray jaPaymentInitiationData = new JSONArray(paymentInitiationData);
                                    %>
                                    <ul class="scope">
                                        <%
                                            for (int i = 0; i < jaPaymentInitiationData.length(); i++) {
                                                if (!jaPaymentInitiationData.getString(i).equals("linebreak")) {
                                        %>
                                        <li><%=Encode.forHtml(jaPaymentInitiationData.getString(i))%>
                                        </li>
                                        <%
                                        } else {
                                        %>
                                        <br>
                                        <%
                                                }
                                            }
                                        %>
                                    </ul>
                                    <%
                                        }
                                    %>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="col-md-12">
                                    <input type="button" class="btn btn-primary" id="approve"
                                           name="approve"
                                           onclick="javascript: approvedBerlin(); return false;"
                                           value="Approve"/>
                                    <input class="btn btn-secondary" type="reset" value="Deny"
                                           onclick="javascript: deny(); return false;"/>

                                    <input type="hidden"
                                           name="<%=Constants.SESSION_DATA_KEY%>"
                                           value="<%=Encode.forHtmlAttribute(request
                                               .getParameter(Constants.SESSION_DATA_KEY))%>"/>
                                    <input type="hidden" name="consent" id="consent" value="deny"/>
                                    <input type="hidden" name="app" id="app"
                                           value="<%=Encode.forHtml(app)%>"/>
                                    <input type="hidden" name="id" id="id"
                                           value="<%=Encode.forHtml(consentId.split(":")[1])%>"/>
                                    <input type="hidden" name="tppUniqueId" id="tppUniqueId"
                                           value="<%=Encode.forHtml(clientId)%>"/>
                                    <input type="hidden" name="user" id="user"
                                           value="<%=Encode.forHtml(loggedInUser)%>"/>
                                    <input type="hidden" name="callback_url" id="callback_url"
                                           value="<%= callbackUrl %>"/>
                                    <input type="hidden" name="type" id="type" value="payments"/>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                    <div class="well policy-info-message"
                                         role="alert margin-top-5x">
                                        <div>
                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                    "privacy.policy.privacy.short.description")%>
                                            <a href="privacy_policy.do" target="policy-pane">
                                                <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                        "privacy.policy.general")%>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>

                    <%
                        }

                        if (requestObject == null) {
                            if (scopeArray != null && scopeArray.length > 0 &&
                                    Arrays.asList(scopeArray).contains("consentmgt")) {
                    %>
                    <form action="../commonauth" method="post" id="oauth2_authz" name="oauth2_authz"
                          class="form-horizontal auto-submit">
                        <div class="form-group">
                            <div class="col-md-12">
                                <input type="button" class="btn btn-primary" id="approve" name="approve"
                                       onclick="javascript: approved(); return false;"
                                       value="Continue"/>
                                <input type="hidden" name="<%=Constants.SESSION_DATA_KEY%>"
                                       value="<%=Encode.forHtmlAttribute(request
                                               .getParameter(Constants.SESSION_DATA_KEY))%>"/>
                                <input type="hidden" name="consent" id="consent"
                                       value="approve"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                <div class="well policy-info-message" role="alert margin-top-5x">
                                    <div>
                                        <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                "privacy.policy.privacy.short.description")%>
                                        <a href="privacy_policy.do" target="policy-pane">
                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                    "privacy.policy.general")%>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                    <%
                    } else {
                    %>
                    <div>
                        <p><strong>No consent requested.</strong></p>
                    </div>
                    <%
                            }
                        }

                    } else if (CommonConstants.STET_SPEC_NAME.equals(APIDataRetriever.getDeployedSpec())) {
                        String consentId = null;
                        for (String parameter : scopeArray) {
                            if (parameter.startsWith("aisp:") || parameter.startsWith("pisp:")) {
                                consentId = parameter;
                            }
                        }

                        String requestdata = clientId + ":" + consentId + ":" + loggedInUser;

                        if (scopeArray != null && scopeArray.length > 0 &&
                                Arrays.asList(scopeArray).contains("accounts")) {

                            APIDataRetriever apiDataRetrieverAcc = APIDataRetriever.getApiDataRetriever();
                            Map<String, Object> accountDataSet = apiDataRetrieverAcc.getAccountDataSet(requestdata);
                            String permissions;
                            List<AccountReference> transactionIBANlist;
                            List<AccountReference> balanceIBANlist;
                            List<AccountReference> accountsIBANlist;
                            List<String> accountsIBANStringlist = new ArrayList<>();
                            List<String> balanceIBANStringlist = new ArrayList<>();
                            List<String> transactionIBANStringlist = new ArrayList<>();
                            List<String> allIBANStringlist = new ArrayList<>();

                            for (Map.Entry<String, Object> accountEntry : accountDataSet.entrySet()) {
                                if (accountEntry.getKey().equals("Accounts")) {
                                    accountsIBANlist = (List<AccountReference>) accountEntry.getValue();

                                    for (AccountReference accountReference : accountsIBANlist) {
                                        String iban = accountReference.getIban();
                                        accountsIBANStringlist.add(iban);
                                        allIBANStringlist.add(iban);
                                    }
                                }
                            }

                            for (Map.Entry<String, Object> transactionentry : accountDataSet.entrySet()) {
                                if (transactionentry.getKey().equals("Transactions")) {
                                    transactionIBANlist = (List<AccountReference>) transactionentry.getValue();

                                    for (AccountReference accountReference : transactionIBANlist) {
                                        String iban = accountReference.getIban();
                                        transactionIBANStringlist.add(iban);
                                        allIBANStringlist.add(iban);
                                    }
                                }
                            }

                            for (Map.Entry<String, Object> balanceEntry :
                                    accountDataSet.entrySet()) {
                                if (balanceEntry.getKey().equals("Balances")) {
                                    balanceIBANlist = (List<AccountReference>)
                                            balanceEntry.getValue();

                                    for (AccountReference accountReference : balanceIBANlist) {
                                        String iban = accountReference.getIban();
                                        balanceIBANStringlist.add(iban);
                                        allIBANStringlist.add(iban);
                                    }
                                }
                            }

                            if ("false".equals(accountDataSet.get(IS_ERROR))) {
                                permissions = accountDataSet.get(PERMISSION).toString();
                            } else {
                                String isError = (String) accountDataSet.get(IS_ERROR);
                                session.invalidate();
                                response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                return;
                            }
                    %>
                    <div class="clearfix"></div>
                    <form action="oauth2_authz_confirm_stet.do" method="post"
                          id="oauth2_authz_confirm" name="oauth2_authz_confirm"
                          class="form-horizontal">
                        <div class="login-form">
                            <div class="form-group">
                                <div class="col-md-12">

                                    <h3><strong><%=Encode.forHtml(appReadableName)%>
                                    </strong> requests account details on your account/s.</h3>
                                    <% if (PermissionEnum.ALL_PSD2.toString().equals(permissions)) { %>

                                    <p><strong> Requested Permissions :  <%=permissions%>
                                    </strong></p>
                                    <div class="form-control" style="color: rgba(45,188,14,0.57)">
                                        These permissions include:
                                    </div>
                                    <li> Read Account Information.</li>
                                    <li> Read Account Balance Information</li>
                                    <li> Read Account Transaction Information</li>
                                    <br>

                                    <script>
                                        getDataFromServer();
                                    </script>

                                    <div class="form-control" style="color: rgba(45,188,14,0.57)">
                                        Selected accounts
                                    </div>
                                    <div class="form-group">
                                        <%--display user accounts--%>
                                        <div id="accountselectberlin">

                                        </div>
                                    </div>

                                    <%
                                    } else if (PermissionEnum.AVAILABLE_ACCOUNTS.toString().equals(permissions)) {
                                    %>
                                    <p><strong> Requested Permissions :  <%=permissions%>
                                    </strong></p>
                                    <div class="form-control" style="color: rgba(45,188,14,0.57)">
                                        Required permissions :
                                    </div>
                                    <li> Read Account Information.</li>
                                    <br>

                                    <script>
                                        getDataFromServer();
                                    </script>

                                    <div class="form-control" style="color: rgba(45,188,14,0.57)">
                                        Selected accounts
                                    </div>
                                    <div class="form-group">
                                        <%--display user accounts--%>
                                        <div id="accountselectberlin">

                                        </div>
                                    </div>

                                    <%
                                    } else if (PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString().equals(permissions)) {
                                    %>
                                    <p><strong> Requested Permissions :  <%=permissions%>
                                    </strong></p>
                                    <div class="form-control" style="color: rgba(45,188,14,0.57)">
                                        Required permissions :
                                    </div>
                                    <li> Read Account Information.</li>
                                    <li> Read Account Balance Information</li>
                                    <br>

                                    <script>
                                        getDataFromServer();
                                    </script>

                                    <div class="form-control" style="color: rgba(45,188,14,0.57)">
                                        Selected accounts
                                    </div>
                                    <div class="form-group">
                                        <%--display user accounts--%>
                                        <div id="accountselectberlin">

                                        </div>
                                    </div>

                                    <%
                                    } else if (PermissionEnum.DEFAULT.toString().equals(permissions)) {
                                        if (accountDataSet.get("Balances") != null &&
                                                accountDataSet.get("Balances").equals(Collections.emptyList())) {
                                            //show everything here for balance
                                        } else if (accountDataSet.get("Balances") != null) {
                                    %>
                                    <div style="color: rgba(54,249,16,0.57)">Required permissions
                                        :
                                    </div>
                                    <li> Read Account Information.</li>
                                    <li> Read Account Balance Information</li>
                                    <div style="color: rgba(59,254,18,0.57)">On following accounts
                                    </div>
                                    <%
                                        for (Map.Entry<String, Object> entry : accountDataSet.entrySet()) {
                                            if (entry.getKey().equals("Balances")) {
                                                List<AccountReference> balanceIBANlist2 =
                                                        (List<AccountReference>) entry.getValue();
                                                for (AccountReference a : balanceIBANlist2) {
                                                    String balanceIBAN = a.getIban();

                                    %>
                                    <li><%=balanceIBAN%>
                                    </li>
                                    <%
                                                    }
                                                }
                                            }
                                        }
                                        if (accountDataSet.get("Transactions") != null &&
                                                accountDataSet.get("Transactions").equals(Collections.emptyList())) {
                                            //show everything here for balance
                                        } else if (accountDataSet.get("Transactions") != null) {
                                    %>
                                    <br>
                                    <div style="color: rgba(58,255,15,0.57)">Required permissions
                                        :
                                    </div>
                                    <li> Read Account Information.</li>
                                    <li> Read Account Transaction Information</li>
                                    <div style="color: rgba(59,255,18,0.57)">On following accounts
                                    </div>
                                    <%
                                        for (Map.Entry<String, Object> entry : accountDataSet.entrySet()) {
                                            if (entry.getKey().equals("Transactions")) {
                                                List<AccountReference> transactionIBANlist2 =
                                                        (List<AccountReference>) entry.getValue();
                                                for (AccountReference a : transactionIBANlist2) {
                                                    String transactionIBAN = a.getIban();

                                    %>
                                    <li><%=transactionIBAN%>
                                    </li>
                                    <%
                                                    }
                                                }
                                            }
                                        }

                                        if (accountDataSet.get("Accounts") != null &&
                                                accountDataSet.get("Accounts").equals(Collections.emptyList())) {
                                            //show everything here for balance
                                        } else if (accountDataSet.get("Accounts") != null) {
                                    %>
                                    <div style="color: rgba(54,249,16,0.57)">Required permissions
                                        :
                                    </div>
                                    <li> Read Account Information.</li>
                                    <div style="color: rgba(59,254,18,0.57)">On following accounts
                                    </div>
                                    <%
                                        for (Map.Entry<String, Object> entry : accountDataSet.entrySet()) {
                                            if (entry.getKey().equals("Accounts")) {
                                                List<AccountReference> accountsIBANlist2 =
                                                        (List<AccountReference>) entry.getValue();
                                                for (AccountReference acc : accountsIBANlist2) {
                                                    String accountIBAN = acc.getIban();

                                    %>
                                    <li><%=accountIBAN%>
                                    </li>
                                    <%
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    %>

                                </div>
                            </div>

                        </div>
                        <div class="form-group">
                            <div class="col-md-12" id="accountConsentDetails">
                                <input type="button" class="btn btn-primary" id="approveAcc"
                                       name="approve"
                                       onclick="javascript: approvedBerlin(); return false;"
                                       value="Approve"/>
                                <input class="btn btn-secondary" type="reset" value="Deny"
                                       onclick="javascript: deny(); return false;"/>

                                <input type="hidden" name="<%=Constants.SESSION_DATA_KEY%>"
                                       value="<%=Encode.forHtmlAttribute(request
                                               .getParameter(Constants.SESSION_DATA_KEY))%>"/>
                                <input type="hidden" name="consent" id="consent" value="deny"/>
                                <input type="hidden" name="app" id="app"
                                       value="<%=Encode.forHtml(app)%>"/>
                                <input type="hidden" name="id" id="id"
                                       value="<%=Encode.forHtml(consentId.split(":")[1])%>"/>
                                <input type="hidden" name="tppUniqueId" id="tppUniqueId"
                                       value="<%=Encode.forHtml(clientId)%>"/>
                                <input type="hidden" name="user" id="user"
                                       value="<%=Encode.forHtml(loggedInUser)%>"/>
                                <input type="hidden" name="type" id="type" value="accounts"/>
                                <input type="hidden" name="permission" id="permission"
                                       value="<%=permissions%>"/>
                                <input type="hidden" name="transactionsList" id="transactionsList"
                                       value="<%=transactionIBANStringlist%>">
                                <input type="hidden" name="balancesList" id="balancesList"
                                       value="<%=balanceIBANStringlist%>">
                                <input type="hidden" name="accountsList" id="accountsList"
                                       value="<%=accountsIBANStringlist%>">
                                <input type="hidden" name="callback_url" id="callback_url"
                                       value="<%= callbackUrl %>"/>
                                <input type="hidden" name="allaccountsList" id="allaccountsList"
                                       value="<%=allIBANStringlist%>">

                            </div>
                        </div>
                    </form>
                </div>

                <%
                    }
                    if (scopeArray != null && scopeArray.length > 0 && Arrays.asList(scopeArray).contains("payments")) {
                        APIDataRetriever apiDataRetriever = APIDataRetriever.getApiDataRetriever();
                        Map<String, Object> paymentDataSet = apiDataRetriever.getPaymentDataSet(requestdata,app);
                        String paymentInitiationData;
                        if ("false".equals(paymentDataSet.get(IS_ERROR))) {
                            paymentInitiationData = (String) paymentDataSet.get("paymentInitiationData");
                        } else {
                            String isError = (String) paymentDataSet.get(IS_ERROR);
                            session.invalidate();
                            response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                            return;
                        }
                %>

                <div class="clearfix"></div>
                <form action="oauth2_authz_confirm_stet.do" method="post"
                      id="oauth2_authz_confirm"
                      name="oauth2_authz_confirm"
                      class="form-horizontal">
                    <div class="login-form">
                        <div class="form-group">
                            <div class="col-md-12">
                                <h3><strong><%=Encode.forHtml(appReadableName)%>
                                </strong> requests consent to do a payment transaction</h3>
                                <p>See details below</p>

                                <%
                                    String debtorAccount = null;
                                    if (displayScopes && paymentInitiationData != null) {
                                        JSONArray jaPaymentInitiationData = new JSONArray(paymentInitiationData);
                                %>
                                <ul class="scope">
                                    <%
                                        for (int i = 0; i < jaPaymentInitiationData.length(); i++) {
                                            if (debtorAccount == null &&
                                                    jaPaymentInitiationData.getString(i).contains("Debtor iban") &&
                                                    !(jaPaymentInitiationData.getString(i).split(" : ")[1].equals("null"))) {
                                                debtorAccount = jaPaymentInitiationData.getString(i).split(" : ")[1];
                                            }
                                            if (!jaPaymentInitiationData.getString(i).equals("linebreak")) {
                                                if (!(jaPaymentInitiationData.getString(i).split(" : ")[1].equals("null"))) {
                                    %>

                                    <li><%=Encode.forHtml(jaPaymentInitiationData.getString(i))%>
                                    </li>
                                    <%
                                        }
                                    } else {
                                    %>
                                    <br>
                                    <%
                                            }
                                        }
                                    %>
                                </ul>
                                <%
                                    }
                                %>
                            </div>
                        </div>
                        <%
                            if (debtorAccount == null) {
                        %>
                        <div class="form-group">
                            <!--label for="accselect">Select Account:</label-->
                            <select class="form-control" id="payaccselect"
                                    onchange="updatePaymentAcc(this)">
                                <option value="default" selected>Select Account:</option>
                                <%
                                    Map<String, String> parameters = new HashMap<>();
                                    parameters.put(USER_ID_KEY_NAME, loggedInUser);
                                    parameters.put(CONSENT_ID_KEY_NAME, consentId);
                                    String payableAccounts = DebtorAccountRetriever.getPayableAccounts(parameters,app);
                                    if(payableAccounts== null) {
                                        String isError = "This PSU doesnt have any Payable Acoounts";
                                        session.invalidate();
                                        response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
                                        return;
                                    }
                                    JSONObject payableAccountsJson = new JSONObject(payableAccounts);
                                    JSONArray payableAccountsArray = payableAccountsJson.getJSONArray("data");
                                    for (int i = 0; i < payableAccountsArray.length(); i++) {
                                        JSONObject object = payableAccountsArray.getJSONObject(i);
                                        String account_id = object.get("account_id").toString();
                                        String display_name = object.get("display_name").toString();

                                %>
                                <option value="<%=account_id%>"><%=display_name%>
                                </option>
                                <%
                                    }
                                %>
                            </select>
                        </div>
                        <%}%>
                        <div class="form-group">
                            <div class="col-md-12">
                                <input type="button" class="btn btn-primary" id="approve"
                                       name="approve"
                                       onclick="javascript: approvedSTET(); return false;"
                                       value="Approve"/>
                                <input class="btn btn-secondary" type="reset" value="Deny"
                                       onclick="javascript: deny(); return false;"/>

                                <input type="hidden" name="<%=Constants.SESSION_DATA_KEY%>"
                                       value="<%=Encode.forHtmlAttribute(request
                                               .getParameter(Constants.SESSION_DATA_KEY))%>"/>
                                <input type="hidden" name="consent" id="consent" value="deny"/>
                                <input type="hidden" name="app" id="app"
                                       value="<%=Encode.forHtml(app)%>"/>
                                <input type="hidden" name="id" id="id"
                                       value="<%=Encode.forHtml(consentId.split(":")[1])%>"/>
                                <input type="hidden" name="clientId" id="clientId"
                                       value="<%=Encode.forHtml(clientId)%>"/>
                                <input type="hidden" name="user" id="user"
                                       value="<%=Encode.forHtml(loggedInUser)%>"/>
                                <input type="hidden" name="callback_url" id="callback_url"
                                       value="<%= callbackUrl %>"/>
                                <input type="hidden" name="type" id="type" value="payments"/>
                                <input type="hidden" name="paymentAccount" id="paymentAccount"
                                       value="<%=Encode.forHtml(debtorAccount)%>"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                <div class="well policy-info-message" role="alert margin-top-5x">
                                    <div>
                                        <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                "privacy.policy.privacy.short.description")%>
                                        <a href="privacy_policy.do" target="policy-pane">
                                            <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                                                    "privacy.policy.general")%>
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>

                <%
                            }

                        }
                    }
                %>
            </div>
        </div>
    </div>
</div>
</div>
</div>

<div id="modal_claim_validation" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
    <div class="modal-dialog modal-md" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "mandatory.claims")%>
                </h4>
            </div>
            <div class="modal-body">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "mandatory.claims.warning.msg.1")%>
                <span class="mandatory-msg"><%=AuthenticationEndpointUtil.i18n(resourceBundle,
                        "mandatory.claims.warning.msg.2")%></span>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "mandatory.claims.warning.msg.3")%>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary"
                        data-dismiss="modal"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "ok")%>
                </button>
            </div>
        </div>
    </div>
</div>

<div id="modal_scope_validation" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
    <div class="modal-dialog modal-md" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "requested.scopes")%>
                </h4>
            </div>
            <div class="modal-body">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "please.select.approve.always")%>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary"
                        data-dismiss="modal"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "ok")%>
                </button>
            </div>
        </div>
    </div>
</div>


<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
<jsp:include page="includes/footer.jsp"/>
<script>
    $(document).ready(function () {
        if ($('.auto-submit').length) {
            $('.auto-submit').submit();
        }

        if($("#type").val()==="payments") {
            reloadCrontoImageDynamic("<%= loggedInUser %>", $("#id").val(), $("#app").val(), "<%= paymentDatasetForVasco%>", $("#type").val());
            updatePaymentAcc(document.getElementById("accountId"));
            reloadPaymentCharges({
                paymentChargesRequestInfo:
                    '<%= new ObjectMapper().writeValueAsString(paymentChargesRequestInfo)%>',
                appName: '<%= app %>',
                accountId: document.getElementById("accountId").value,
                paymentType:  '<%= paymentType %>',
                consentID: $("#id").val()
            });
        }
        else
        {
            reloadCrontoImage("<%= loggedInUser %>", $("#id").val(), $("#app").val());
        }

        $("#consent_select_all").click(function () {
            if (this.checked) {
                $('.checkbox input:checkbox').each(function () {
                    $(this).prop("checked", true);
                });
            } else {
                $('.checkbox :checkbox').each(function () {
                    $(this).prop("checked", false);
                });
            }
        });
        $(".checkbox input").click(function () {
            var claimCheckedCheckboxes = $(".claim-cb input:checked").length;
            var claimCheckboxes = $(".claim-cb input").length;
            if (claimCheckedCheckboxes !== claimCheckboxes) {
                $("#consent_select_all").prop("checked", false);
            } else {
                $("#consent_select_all").prop("checked", true);
            }
        });
        $("div[onload]").trigger("onload");
    });
</script>
</body>
</html>
