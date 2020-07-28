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

<%
    String errorCode = request.getParameter("oauthErrorCode");
    String errorMsg = request.getParameter("oauthErrorMsg");
%>

<html>
<head>
    <jsp:include page="includes/head.jsp" />
</head>

<body>

    <script type="text/javascript">
        function approved() {
            document.getElementById('consent').value = "approve";
            document.getElementById("oauth2_authz").submit();
        }
        function approvedAlways() {
            document.getElementById('consent').value = "approveAlways";
            document.getElementById("oauth2_authz").submit();
        }
        function deny() {
            document.getElementById('consent').value = "deny";
            document.getElementById("oauth2_authz").submit();
        }
    </script>
	<header class="header header-default">
		<div class="container-fluid">
		   <div class="pull-left brand float-remove-xs text-center-xs brand-container">
			  <img src="images/Al_Rayan_Logo-150min.jpg" class="logo" alt="Al Rayan Open Banking"/>
			  <h2 class="text-center-sm text-center-xs text-center-md text-right">Al Rayan bank user portal</h2>
		   </div>
		</div>
	</header>
    <div class="page-content-wrapper">
        <div class="container-fluid ">
            <div class="container">
                <div class="login-form-wrapper">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 login">
                            <div class="boarder-all data-container error">
                                <div class="clearfix"></div>
                                <form action="../commonauth" method="post" id="oauth2_authz" name="oauth2_authz" class="form-horizontal">
                                    <div class="login-form">
                                        <h3>Invalid Request</h3>
                                        <%
                                            if (errorCode != null && errorMsg != null) {
                                        %>
                                            <p class="padding-bottom-double">
                                                <%=Encode.forHtmlContent(errorCode)%>
                                            </p>
                                            <p class="padding-bottom-double">
                                                <%=Encode.forHtmlContent(errorMsg)%>
                                            </p>
                                        <%
                                        } else {
                                        %>
                                            <p class="padding-bottom-double">
                                                <fmt:message key='oauth.processing.error.msg'/>
                                            </p>
                                        <%
                                            }
                                        %>
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
</body>
</html>


