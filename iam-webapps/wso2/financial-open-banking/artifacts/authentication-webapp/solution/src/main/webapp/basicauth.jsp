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

<%@ page import="org.apache.cxf.jaxrs.client.JAXRSClientFactory" %>
<%@ page import="org.apache.cxf.jaxrs.provider.json.JSONProvider" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.client.SelfUserRegistrationResource" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.AuthenticationEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.bean.ResendCodeRequestDTO" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.bean.UserDTO" %>
<%@ page import="com.alrayan.wso2.common.AlRayanConfiguration" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>



<%
    String isRegistrationFlow = request.getParameter("is_registration_flow");
    String resendUsername = request.getParameter("resend_username");
    if (StringUtils.isNotBlank(resendUsername)) {

        String url = config.getServletContext().getInitParameter(Constants.ACCOUNT_RECOVERY_REST_ENDPOINT_URL);

        ResendCodeRequestDTO selfRegistrationRequest = new ResendCodeRequestDTO();
        UserDTO userDTO = AuthenticationEndpointUtil.getUser(resendUsername);
        selfRegistrationRequest.setUser(userDTO);
        url = url.replace("tenant-domain", userDTO.getTenantDomain());

        List<JSONProvider> providers = new ArrayList<JSONProvider>();
        JSONProvider jsonProvider = new JSONProvider();
        jsonProvider.setDropRootElement(true);
        jsonProvider.setIgnoreNamespaces(true);
        jsonProvider.setValidateOutput(true);
        jsonProvider.setSupportUnwrapped(true);
        providers.add(jsonProvider);

        SelfUserRegistrationResource selfUserRegistrationResource = JAXRSClientFactory
                .create(url, SelfUserRegistrationResource.class, providers);
        Response selfRegistrationResponse = selfUserRegistrationResource.regenerateCode(selfRegistrationRequest);
        if (selfRegistrationResponse != null &&  selfRegistrationResponse.getStatus() == HttpStatus.SC_CREATED) {
%>
<div class="alert alert-info"><%= Encode.forHtml(resourceBundle.getString(Constants.ACCOUNT_RESEND_SUCCESS_RESOURCE)) %>
</div>
<%
} else {
%>
<div class="alert alert-danger"><%= Encode.forHtml(resourceBundle.getString(Constants.ACCOUNT_RESEND_FAIL_RESOURCE))  %>
</div>
<%
        }
    }
%>


<%
    String type = request.getParameter("type");
    if ("samlsso".equals(type)) {
%>
<form action="/samlsso" method="post" id="loginForm">
    <input id="tocommonauth" name="tocommonauth" type="hidden" value="true">
<%
    } else if ("oauth2".equals(type)){
%>
    <form action="/oauth2/authorize" method="post" id="loginForm">
        <input id="tocommonauth" name="tocommonauth" type="hidden" value="true">

<%
    } else {
%>

<form action="../commonauth" method="post" id="loginForm">

    <%
        }
    %>

    <% if (Boolean.parseBoolean(loginFailed)) { %>
    <div class="alert alert-danger" id="error-msg"><%= Encode.forHtml(errorMessage) %>
    </div>
    <%}else if((Boolean.TRUE.toString()).equals(request.getParameter("authz_failure"))){%>
    <div class="alert alert-danger" id="error-msg">You are not authorized to login</div>
    <%}%>

    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
            <input id="username" name="username" type="text" class="form-control" tabindex="0"
                   placeholder="Username">
        </div>
    </div>
    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
            <input id="password" name="password" type="password" class="form-control"
                   placeholder="Password" autocomplete="off">
        </div>
    </div>
    <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
            (request.getParameter("sessionDataKey"))%>'/>
    <%
        if (reCaptchaEnabled) {
    %>
    <div class="form-group">
        <div class="g-recaptcha"
             data-sitekey="<%=Encode.forHtmlContent(request.getParameter("reCaptchaKey"))%>">
        </div>
    </div>
    <%
        }
    %>

    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
            <div class="form-actions">
                <button
                        class="btn btn-primary uppercase"
                        type="submit">Sign in
                </button>
            </div>
        </div>
    </div>

    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
           <div class="well policy-info-message" role="alert margin-top-5x">
              <div>
                  <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.cookies.short.description")%>
                  <a href="https://www.alrayanbank.co.uk/cookies/" target="policy-pane">
                      <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.cookies")%>
                  </a>
                  <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.for.more.details")%>
                  <br><br>
                  <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.privacy.short.description")%>
                  <a href="https://www.alrayanbank.co.uk/useful-info-tools/legal/privacy/" target="policy-pane">
                      <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.general")%>
                  </a>
              </div>
           </div>
        </div>
    </div>

    <%

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String uri = (String) request.getAttribute("javax.servlet.forward.request_uri");
        String prmstr = (String) request.getAttribute("javax.servlet.forward.query_string");
        String urlWithoutEncoding = scheme + "://" +serverName + ":" + serverPort + uri + "?" + prmstr;
        String urlEncodedURL = URLEncoder.encode(urlWithoutEncoding, "UTF-8");

        if (request.getParameter("relyingParty").equals("wso2.my.dashboard")) {
            String identityMgtEndpointContext =
                    application.getInitParameter("IdentityManagementEndpointContextURL");
            if (StringUtils.isBlank(identityMgtEndpointContext)) {
                identityMgtEndpointContext = IdentityUtil.getServerURL("/accountrecoveryendpoint", true, true);
            }

            URL url = null;
            HttpURLConnection httpURLConnection = null;

            url = new URL(identityMgtEndpointContext + "/recoverpassword.do?callback="+Encode.forHtmlAttribute
                    (urlEncodedURL ));
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("HEAD");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
    %>
        <div class="form-group">
            <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                <a id="passwordRecoverLink" href="<%=url%>">Forgot Password </a>
            </div>
        </div>
    <%
    }

    url = new URL(identityMgtEndpointContext + "/recoverusername.do?callback="+Encode.forHtmlAttribute
            (urlEncodedURL ));
    httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("HEAD");
    httpURLConnection.connect();
    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
    %>
        <div class="form-group">
            <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                <a id="usernameRecoverLink" href="<%=url%>">Forgot Username </a>
            </div>
        </div>
    <%
    }




    url = new URL(identityMgtEndpointContext + "/register.do?callback="+Encode.forHtmlAttribute
            (urlEncodedURL ));
    httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("HEAD");
    httpURLConnection.connect();
    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
    %>
        <div class="form-group">
            <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                <label class="control-label">Don't have an account?</label>
                <a id="registerLink" href="<%=url%>">Register Now</a>
            </div>
        </div>
    <%
            }
    %>

    <%
        } else {
    %>

    <!-- Recover credentials link -->
    <%
        if((Boolean.FALSE.toString()).equals(isRegistrationFlow) || isRegistrationFlow == null){
    %>

    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
            <a id="passwordRecoverLink"
               href="<%= AlRayanConfiguration.RESET_CREDENTIAL_ENDPOINT.getValue() %>"
               target="_blank">
                Recover Username/password (Personal)
            </a>
			<br/>
			<!-- this is a quick fix for the release of Digital -->
			<a id="passwordRecoverLinkBB"
               href="<%= AlRayanConfiguration.RESET_CREDENTIAL_ENDPOINT_BB.getValue() %>"
               target="_blank">
                Recover Username/password (Business)
            </a>
        </div>
    </div>

    <%
        }
    %>
    <!-- End of recover credentials link -->

    <%
        }
    %>


    <% if (Boolean.parseBoolean(loginFailed) && errorCode.equals(IdentityCoreConstants.USER_ACCOUNT_NOT_CONFIRMED_ERROR_CODE) && request.getParameter("resend_username") == null) { %>
        <div class="form-group">
            <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                <label class="control-label">Not received confirmation email ?</label>
                <a id="registerLink" href="login.do?resend_username=<%=Encode.forHtml(request.
                getParameter("failedUsername"))%>&<%=AuthenticationEndpointUtil.
                cleanErrorMessages(Encode.forJava(request.getQueryString()))%>">Re-Send</a>
            </div>
        </div>
    <%}%>
</form>
