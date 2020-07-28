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

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.owasp.encoder.Encode" %>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">

    <%
        String stat = request.getParameter("status");
        String statusMessage = request.getParameter("statusMsg");
        String errorCode = request.getParameter("errorCode");

        if (stat == null || statusMessage == null) {
            stat = "Authentication Error !";
            statusMessage = "Something went wrong during the authentication process. Please try signing in again.";


            if("17003".equals(errorCode)) {
                statusMessage = " You are temporarily blocked from performing this operation. Please contact the bank staff to unblock your account";
            }
        }
        session.invalidate();
    %>

    <html>
    <head>
        <jsp:include page="includes/head.jsp" />
    </head>

    <body>
        
    <div class="page-content-wrapper">
        <div class="container-fluid ">
            <div class="container">
                <div class="login-form-wrapper">
                    <div class="row" style="background-color: white">
                        <div class="col-xs-12 col-sm-12 col-md-3 col-lg-3">
                            <div class="brand-container add-margin-bottom-5x">
                                <div class="row">
                                    <div class="col-xs-6 col-sm-3 col-md-9 col-lg-9 center-block float-remove-sm float-remove-xs pull-right-md pull-right-lg">
                                        <img src="images/Al_Rayan_Logo-150min.jpg" class="img-responsive brand-spacer login-logo" alt="Al Rayan Bank"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-9 col-lg-9 login">
                            <div class="boarder-all data-container error">
                                <div class="clearfix"></div>
                                <form action="../commonauth" method="post" id="oauth2_authz" name="oauth2_authz" class="form-horizontal">
                                    <div class="login-form">
                                        <h3><%=Encode.forHtmlContent(stat)%></h3>
                                        <p class="padding-bottom-double">
                                            <%=Encode.forHtmlContent(statusMessage)%>
                                        </p>
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

    <script type="text/javascript" src="js/u2f-api.js"></script>

    </body>
    </html>


</fmt:bundle>

