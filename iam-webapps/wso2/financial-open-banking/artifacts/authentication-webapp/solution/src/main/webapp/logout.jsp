<!doctype html>
<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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
    
<div class="page-content-wrapper">
    <div class="container-fluid ">
        <div class="container">
            <div class="login-form-wrapper">
                <div class="row" style="background: white">
                    <div class="col-xs-12 col-sm-12 col-md-3 col-lg-3">
                        <div class="brand-container add-margin-bottom-5x">
                            <div class="row">
                                <div class="col-xs-6 col-sm-3 col-md-9 col-lg-9 center-block float-remove-sm float-remove-xs pull-right-md pull-right-lg">
                                    <img src="images/Al_Rayan_Logo-150min.jpg" class="img-responsive brand-spacer login-logo" alt="WSO2 Open Banking"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-12 col-sm-12 col-md-9 col-lg-9 login">
                        <div class="boarder-all data-container">
                            <div class="clearfix"></div>
                            <form action="../commonauth" method="post" id="oauth2_authz" name="oauth2_authz" class="form-horizontal">
                                <div class="login-form">
                                    <h3>You have successfully logged out.</h3>
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



