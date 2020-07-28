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

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointUtil" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointConstants" %>
<%@ page import="com.alrayan.wso2.common.AlRayanConfiguration" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.UserStoreConfigAdminService" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO" %>

<jsp:directive.include file="localize.jsp"/>

<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));
    String callback = (String) request.getAttribute("callback");
    String confirmationKey = (String) request.getAttribute("confirmationKey");
    String tenantDomain = (String) request.getAttribute(IdentityManagementEndpointConstants.TENANT_DOMAIN);
    if (tenantDomain == null) {
        tenantDomain = (String) session.getAttribute(IdentityManagementEndpointConstants.TENANT_DOMAIN);
    }
    boolean isDBBasedRecoveryOld = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("isDBBasedRecoveryOld"));

    String serverURL =  AlRayanConfiguration.SERVERHOST_WITH_PROXYPORT.getValue();
    String response_type = request.getParameter("response_type");
    String scope = request.getParameter("scope");
    String openidnonse = request.getParameter("openidnonse");
    String client_id = request.getParameter("client_id");
    if(StringUtils.isEmpty(response_type)) {
        response_type = "code";
    }
    if(StringUtils.isEmpty(scope)) {
        scope = "openid";
    }
    if(StringUtils.isEmpty(openidnonse)){
        openidnonse = "arn";
    }
%>

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


<%
    String passwordValidationRegex ="^[\\S]{1,30}$";
    String passwordErrorMessage = "Username pattern policy violated";
    String passwordPatternViolatedMsg = "Password pattern policy violated";
    UserStoreConfigAdminService userStoreConfigAdminService = new UserStoreConfigAdminService();
    UserStoreDTO[] userStoreDTOS = userStoreConfigAdminService.getSecondaryRealmConfigurations();

    for(UserStoreDTO userStoreDTO:userStoreDTOS) {

        if(!StringUtils.isEmpty(userStoreDTO.getClassName()) && userStoreDTO.getClassName().contains("AlRayanUserStoreManager"))

            for(PropertyDTO propertyDTO:userStoreDTO.getProperties()) {

                if("PasswordJavaRegEx".equals(propertyDTO.getName())) {
                    passwordValidationRegex = propertyDTO.getValue();
                }
                if("PasswordJavaRegExViolationErrorMsg".equals(propertyDTO.getName())) {
                    passwordErrorMessage = propertyDTO.getValue();
                }
            }
    }

%>

<!-- page content -->
<div class="container-fluid body-wrapper">

    <div class="row">
        <!-- content -->
        <div class="col-xs-12 col-sm-10 col-md-8 col-lg-5 col-centered wr-login">
            <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Reset.Password")%>
            </h2>

            <div class="clearfix"></div>
            <div class="boarder-all ">

                <% if (error) { %>
                <div class="alert alert-danger" id="server-error-msg">
                    <%=IdentityManagementEndpointUtil.i18nBase64(recoveryResourceBundle, errorMsg)%>
                </div>
                <% } %>
                <div class="alert alert-danger" id="error-msg" hidden="hidden"></div>

                <div class="padding-double">
                    <form method="post" action="completeoldpasswordreset.do" id="passwordResetForm">

                        <% if (isDBBasedRecoveryOld) {
                            String digitalBankingUsername = (String) request.getAttribute("digitalBankingUsername");
                        %>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                            <input type="hidden" name="digitalBankingUsername" value="<%=Encode.forHtmlAttribute(digitalBankingUsername) %>"/>
                        </div>
                        <% } %>

                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                            <label class="control-label">
                                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Enter.old.password")%>
                            </label>
                            <input id="old-password" name="old-password" type="password"
                                   class="form-control" required>
                        </div>

                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                            <label class="control-label">
                                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Enter.new.password")%>
                            </label>
                            <input id="reset-password" name="reset-password" type="password"
                                   class="form-control" required pattern="<%=passwordValidationRegex%>">
                            <label>
                                <div style="color: darkred; font-size: small">
                                    <%=passwordErrorMessage%>
                                </div>
                            </label>

                        </div>



                        <%
                            if (callback != null) {
                        %>
                        <div>
                            <input type="hidden" name="callback" value="<%=Encode.forHtmlAttribute(callback) %>"/>
                        </div>
                        <%
                            }
                        %>

                        <%
                            if (confirmationKey != null) {
                        %>
                        <div>
                            <input type="hidden" name="confirmationKey" value="<%=Encode.forHtmlAttribute(confirmationKey) %>"/>
                        </div>
                        <%
                            }
                        %>

                        <%
                            if (tenantDomain != null) {
                        %>
                        <div>
                            <input type="hidden" name="tenantdomain" value="<%=Encode.forHtmlAttribute(tenantDomain) %>"/>
                        </div>
                        <%
                            }
                        %>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                            <label class="control-label">
                                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Confirm.password")%>
                            </label>
                            <input id="reset-password2" name="reset-password2" type="password" class="form-control"
                                   data-match="reset-password" required="" pattern="<%=passwordValidationRegex%>">

                            <label>
                                <div style="color: darkred; font-size: small">
                                    <%=passwordErrorMessage%>
                                </div>
                            </label>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                            <input type="hidden" id="passwordValidationRegex" value="<%=passwordValidationRegex%>" >
                        </div>


                        <div class="form-actions">
                            <button id="submit"
                                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                    type="submit"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Submit")%>
                            </button>
                        </div>
                        <div class="clearfix"></div>
                    </form>
                </div>
            </div>
        </div>
        <!-- /content/body -->

    </div>
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

<script type="text/javascript">
    $(document).ready(function () {

        $("#passwordResetForm").submit(function (e) {

            $("#server-error-msg").remove();
            var password = $("#reset-password").val();
            var password2 = $("#reset-password2").val();
            var pattern = $("#passwordValidationRegex").val();
            var error_msg = $("#error-msg");

            if (!password || 0 === password.length) {
                error_msg.text("<%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                "Password.cannot.be.empty")%>");
                error_msg.show();
                $("html, body").animate({scrollTop: error_msg.offset().top}, 'slow');
                return false;
            }

            if (password != password2) {
                error_msg.text("<%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                 "Passwords.did.not.match.please.try.again")%>");
                error_msg.show();
                $("html, body").animate({scrollTop: error_msg.offset().top}, 'slow');
                return false;
            }


            if(!password.match(pattern)){
                error_msg.text("<%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
            passwordErrorMessage.toString())%>");
                error_msg.show();
                return false;
            }

            return true;
        });
    });

</script>
</body>
</html>
