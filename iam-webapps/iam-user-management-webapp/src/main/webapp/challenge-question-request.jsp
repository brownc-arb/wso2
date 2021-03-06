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

<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointConstants" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementEndpointUtil" %>
<%@ page import="com.alrayan.wso2.webapp.management.IdentityManagementServiceUtil" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.ApiException" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.api.SecurityQuestionApi" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.Error" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.Question" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.InitiateAllQuestionResponse" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.User" %>
<%@ page import="com.alrayan.wso2.webapp.management.client.model.RetryError" %>
<jsp:directive.include file="localize.jsp"/>

<%
    String username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));
    RetryError errorResponse = (RetryError) request.getAttribute("errorResponse");
    List<Question> challengeQuestions = null;

    if (errorResponse != null) {
        username = (String) session.getAttribute("username");
    }
    if (StringUtils.isNotBlank(username)) {
        if (Boolean.parseBoolean(application.getInitParameter(
                IdentityManagementEndpointConstants.ConfigConstants.PROCESS_ALL_SECURITY_QUESTIONS))) {
            User user = IdentityManagementServiceUtil.getInstance().getUser(username);

            try {
                SecurityQuestionApi securityQuestionApi = new SecurityQuestionApi();
                InitiateAllQuestionResponse initiateAllQuestionResponse = securityQuestionApi.securityQuestionsGet(user.getUsername(),
                        user.getRealm(), user.getTenantDomain());
                IdentityManagementEndpointUtil.addReCaptchaHeaders(request, securityQuestionApi.getApiClient().getResponseHeaders());
                session.setAttribute("initiateAllQuestionResponse", initiateAllQuestionResponse);

                challengeQuestions = initiateAllQuestionResponse.getQuestions();
            } catch (ApiException e) {
                if (e.getCode() == 204) {

                    //No questions found
                    request.setAttribute("error", true);
                    request.setAttribute("errorMsg",
                            IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                    "No.security.questions.found.to.recover.password.contact.system.administrator"));
                    request.setAttribute("errorCode", "18017");
                    request.getRequestDispatcher("error.jsp").forward(request, response);
                    return;

                }
                IdentityManagementEndpointUtil.addReCaptchaHeaders(request, e.getResponseHeaders());
                Error error = new Gson().fromJson(e.getMessage(), Error.class);
                request.setAttribute("error", true);
                if (error != null) {
                    request.setAttribute("errorMsg", error.getDescription());
                    request.setAttribute("errorCode", error.getCode());
                }
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }

        } else {
            request.getRequestDispatcher("challenge-question-process.jsp?username=" + username).forward(request,
                    response);
        }
    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Username.missing"));
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    boolean reCaptchaEnabled = false;
    if (request.getAttribute("reCaptcha") != null && "TRUE".equalsIgnoreCase((String) request.getAttribute("reCaptcha"))) {
        reCaptchaEnabled = true;
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

        <%
            if (reCaptchaEnabled) {
        %>
        <script src='<%=(request.getAttribute("reCaptchaAPI"))%>'></script>
        <%
            }
        %>
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

        <div class="row">
            <!-- content -->
            <div class="col-xs-12 col-sm-10 col-md-8 col-lg-5 col-centered wr-login">
                <%
                    if (errorResponse != null) {
                %>
                <div class="alert alert-danger" id="server-error-msg">
                    <%=IdentityManagementEndpointUtil.i18nBase64(recoveryResourceBundle, errorResponse.getDescription())%>
                </div>
                <%
                    }
                %>
                <div class="clearfix"></div>
                <div class="boarder-all data-container">

                    <div class="padding-double">
                        <form method="post" action="processsecurityquestions.do" id="securityQuestionForm">
                            <%
                                int count = 0;
                                if (challengeQuestions != null) {
                                    for (Question challengeQuestion : challengeQuestions) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label class="control-label"><%=Encode.forHtml(challengeQuestion.getQuestion())%>
                                </label>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input name="<%=Encode.forHtmlAttribute(challengeQuestion.getQuestionSetId())%>"
                                       type="text"
                                       class="form-control"
                                       tabindex="0" autocomplete="off" required/>
                            </div>
                            <%
                                    }
                                }
                            %>
                            <%
                                if (reCaptchaEnabled) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <div class="g-recaptcha"
                                     data-sitekey="<%=Encode.forHtmlContent((String)request.getAttribute("reCaptchaKey"))%>">
                                </div>
                            </div>
                            <%
                                }
                            %>
                            <div class="form-actions">
                                <button id="answerSubmit"
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
    </body>
    </html>
