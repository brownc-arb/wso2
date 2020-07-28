<!doctype html>
<%--
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Arrays" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityCoreConstants" %>

<%!
    private static final String FIDO_AUTHENTICATOR = "FIDOAuthenticator";
    private static final String IWA_AUTHENTICATOR = "IWAAuthenticator";
    private static final String IS_SAAS_APP = "isSaaSApp";
    private static final String BASIC_AUTHENTICATOR = "BasicAuthenticator";
    private static final String AL_RAYAN_BASIC_AUTHENTICATOR = "AlRayanBasicAuthenticator";
    private static final String OPEN_ID_AUTHENTICATOR = "OpenIDAuthenticator";
%><fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">

    <%
        String BUNDLE = "org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        request.getSession().invalidate();
        String queryString = request.getQueryString();
        Map<String, String> idpAuthenticatorMapping = null;
        if (request.getAttribute(Constants.IDP_AUTHENTICATOR_MAP) != null) {
            idpAuthenticatorMapping = (Map<String, String>) request.getAttribute(Constants.IDP_AUTHENTICATOR_MAP);
        }

        String errorMessage = "Authentication Failed! Please Retry";
        String errorCode = "";
        if(request.getParameter(Constants.ERROR_CODE)!=null){
            errorCode = request.getParameter(Constants.ERROR_CODE) ;
        }
        String loginFailed = "false";

        if (Boolean.parseBoolean(request.getParameter(Constants.AUTH_FAILURE))) {
            loginFailed = "true";
            if (request.getParameter(Constants.AUTH_FAILURE_MSG) != null) {
                errorMessage = resourceBundle.getString(request.getParameter(Constants.AUTH_FAILURE_MSG));
            }
        }
    %>
    <%

        boolean hasLocalLoginOptions = false;
        List<String> localAuthenticatorNames = new ArrayList<String>();

        if (idpAuthenticatorMapping != null && idpAuthenticatorMapping.get(Constants.RESIDENT_IDP_RESERVED_NAME) != null) {
            String authList = idpAuthenticatorMapping.get(Constants.RESIDENT_IDP_RESERVED_NAME);
            if (authList != null) {
                localAuthenticatorNames = Arrays.asList(authList.split(","));
            }
        }


    %>
    <%
        boolean reCaptchaEnabled = false;
        if (request.getParameter("reCaptcha") != null && "TRUE".equalsIgnoreCase(request.getParameter("reCaptcha"))) {
            reCaptchaEnabled = true;
        }
    %>
    <html>
    <head>
        <jsp:include page="includes/head.jsp" />
        <%
            if (reCaptchaEnabled) {
        %>
        <script src='<%=
        (Encode.forJavaScriptSource(request.getParameter("reCaptchaAPI")))%>'></script>
        <%
            }
        %>
    </head>
	<!-- header -->
	<header class="header header-default">
		<div class="container-fluid">
		   <div class="pull-left brand float-remove-xs text-center-xs brand-container">
			  <img src="images/Al_Rayan_Logo-150min.jpg" class="logo" alt="Alrayan Open Banking"/>
			  <h2 class="text-center-sm text-center-xs text-center-md text-right">Al Rayan bank user portal</h2>
		   </div>
		</div>
	</header>

    <body>
        
    <div class="page-content-wrapper">
        <div class="container-fluid ">
            <div class="container">
                <div class="login-form-wrapper">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 login">
                            <div class="boarder-all data-container">
                                <form action="../commonauth" method="post" id="oauth2_authz" name="oauth2_authz" class="form-horizontal">
                                    <h3>Sign in using Al Rayan Bank account</h3>
                                    <div class="login-form">
                                        <%
                                            if (localAuthenticatorNames.size() > 0) {

                                                if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains(OPEN_ID_AUTHENTICATOR)) {
                                                    hasLocalLoginOptions = true;
                                        %>

                                        <%@ include file="openid.jsp" %>

                                        <%
                                        } else if (localAuthenticatorNames.size() > 0
                                                   && (localAuthenticatorNames.contains(BASIC_AUTHENTICATOR) ||
                                                       localAuthenticatorNames.contains(
                                                               AL_RAYAN_BASIC_AUTHENTICATOR))) {
                                            hasLocalLoginOptions = true;
                                        %>

                                        <%
                                            if (TenantDataManager.isTenantListEnabled() && Boolean.parseBoolean(request.getParameter(IS_SAAS_APP))) {
                                        %>

                                        <%@ include file="tenantauth.jsp" %>

                                        <script>
                                            //set the selected tenant domain in dropdown from the cookie value
                                            window.onload = selectTenantFromCookie;
                                        </script>
                                        <%
                                        } else {
                                        %>
                                        <%@ include file="basicauth.jsp" %>
                                        <%
                                                    }
                                                }
                                            }
                                        %>

                                        <%if (idpAuthenticatorMapping != null &&
                                                idpAuthenticatorMapping.get(Constants.RESIDENT_IDP_RESERVED_NAME) != null) { %>

                                        <%} %>
                                        <%
                                            if ((hasLocalLoginOptions && localAuthenticatorNames.size() > 1) || (!hasLocalLoginOptions)
                                                    || (hasLocalLoginOptions && idpAuthenticatorMapping != null && idpAuthenticatorMapping.size() > 1)) {
                                        %>
                                        <div class="form-group">
                                            <% if (hasLocalLoginOptions) { %>
                                            <label class="font-large">Other login options:</label>
                                            <%} %>
                                        </div>
                                        <div class="form-group">
                                            <%
                                                int iconId = 0;
                                                if (idpAuthenticatorMapping != null) {
                                                for (Map.Entry<String, String> idpEntry : idpAuthenticatorMapping.entrySet()) {
                                                    iconId++;
                                                    if (!idpEntry.getKey().equals(Constants.RESIDENT_IDP_RESERVED_NAME)) {
                                                        String idpName = idpEntry.getKey();
                                                        boolean isHubIdp = false;
                                                        if (idpName.endsWith(".hub")) {
                                                            isHubIdp = true;
                                                            idpName = idpName.substring(0, idpName.length() - 4);
                                                        }
                                            %>
                                            <% if (isHubIdp) { %>
                                            <div>
                                                <a href="#" data-toggle="popover" data-placement="bottom"
                                                   title="Sign in with <%=Encode.forHtmlAttribute(idpName)%>" id="popover" id="icon-<%=iconId%>">
                                                    <img class="idp-image" src="images/login-icon.png"
                                                         title="Sign in with <%=Encode.forHtmlAttribute(idpName)%>"/>

                                                    <div id="popover-head" class="hide">
                                                        <label class="font-large">Sign in with <%=Encode.forHtmlContent(idpName)%></label>
                                                    </div>
                                                    <div id="popover-content" class="hide">
                                                        <form class="form-inline">
                                                            <div class="form-group">
                                                                <input id="domainName" class="form-control" type="text"
                                                                       placeholder="Domain Name">
                                                            </div>
                                                            <input type="button" class="btn btn-primary go-btn"
                                                                   onClick="javascript: myFunction('<%=idpName%>','<%=idpEntry.getValue()%>','domainName')"
                                                                   value="Go"/>
                                                        </form>

                                                    </div>
                                                </a>
                                                <label for="icon-<%=iconId%>"><%=Encode.forHtmlContent(idpName)%></label>
                                            </div>
                                            <%} else { %>
                                            <div>
                                                <a onclick="javascript: handleNoDomain('<%=Encode.forJavaScriptAttribute(Encode.
                                                forUriComponent(idpName))%>',
                                                        '<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(idpEntry.getValue()))%>')"
                                                   href="#" id="icon-<%=iconId%>">
                                                    <img class="idp-image" src="images/login-icon.png" data-toggle="tooltip"
                                                         data-placement="top" title="Sign in with <%=Encode.forHtmlAttribute(idpName)%>"/>
                                                </a>
                                                <label for="icon-<%=iconId%>"><%=Encode.forHtmlContent(idpName)%></label>
                                            </div>
                                            <%} %>
                                            <%
                                            } else if (localAuthenticatorNames.size() > 0) {
                                                if (localAuthenticatorNames.contains(IWA_AUTHENTICATOR)) {
                                            %>
                                            <div>
                                                <a onclick="javascript: handleNoDomain('<%=Encode.forJavaScriptAttribute(Encode.
                                                forUriComponent(idpEntry.getKey()))%>',
                                                        'IWAAuthenticator')" class="main-link" style="cursor:pointer" id="icon-<%=iconId%>">
                                                    <img class="idp-image" src="images/login-icon.png" data-toggle="tooltip"
                                                         data-placement="top" title="Sign in with IWA"/>
                                                </a>
                                                <label for="icon-<%=iconId%>">IWA</label>
                                            </div>
                                            <%
                                                }
                                                if (localAuthenticatorNames.contains(FIDO_AUTHENTICATOR)) {
                                            %>
                                            <div>
                                                <a onclick="javascript: handleNoDomain('<%=Encode.forJavaScriptAttribute(Encode.
                                                forUriComponent(idpEntry.getKey()))%>',
                                                        'FIDOAuthenticator')" class="main-link" style="cursor:pointer" id="icon-<%=iconId%>">
                                                    <img class="idp-image" src="images/login-icon.png" data-toggle="tooltip"
                                                         data-placement="top" title="Sign in with FIDO"/>
                                                </a>
                                                <label for="icon-<%=iconId%>">FIDO</label>
                                            </div>
                                            <%
                                                        }
                                                    }

                                                }
                                                }%>

                                        </div>


                                        <% } %>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="includes/footer.jsp" />

    <script>
        $(document).ready(function () {
            $('.main-link').click(function () {
                $('.main-link').next().hide();
                $(this).next().toggle('fast');
                var w = $(document).width();
                var h = $(document).height();
                $('.overlay').css("width", w + "px").css("height", h + "px").show();
            });
            $('[data-toggle="popover"]').popover();
            $('.overlay').click(function () {
                $(this).hide();
                $('.main-link').next().hide();
            });

            <%
            if(reCaptchaEnabled) {
            %>
            var error_msg = $("#error-msg");
            $("#loginForm").submit(function (e) {
                var resp = $("[name='g-recaptcha-response']")[0].value;
                if (resp.trim() == '') {
                    error_msg.text("Please select reCaptcha.");
                    error_msg.show();
                    $("html, body").animate({scrollTop: error_msg.offset().top}, 'slow');
                    return false;
                }
                return true;
            });
            <%
            }
            %>
        });
        function myFunction(key, value, name) {
            var object = document.getElementById(name);
            var domain = object.value;


            if (domain != "") {
                document.location = "../commonauth?idp=" + key + "&authenticator=" + value +
                        "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>&domain=" +
                        domain;
            } else {
                document.location = "../commonauth?idp=" + key + "&authenticator=" + value +
                        "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>";
            }
        }

        function handleNoDomain(key, value) {
            document.location = "../commonauth?idp=" + key + "&authenticator=" + value +
                    "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>";
        }

        $('#popover').popover({
            html: true,
            title: function () {
                return $("#popover-head").html();
            },
            content: function () {
                return $("#popover-content").html();
            }
        });

    </script>

    </body>
    </html>


</fmt:bundle>

