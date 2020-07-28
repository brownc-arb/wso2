<!doctype html>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <jsp:include page="includes/head.jsp" />
</head>

<body>

<script type="text/javascript">
    function approved() {
        document.getElementById('consent').value = "approve";
        document.getElementById("oidc_logout_consent_form").submit();
    }

    function deny() {
        document.getElementById('consent').value = "deny";
        document.getElementById("oidc_logout_consent_form").submit();
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
                            <form action="../oidc/logout" method="post" id="oidc_logout_consent_form" name="oidc_logout_consent_form" class="form-horizontal login-form">
                                <div class="form-group">
                                    <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                        <h3>Do you want to Logout?</h3>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                        <div class="form-actions">
                                            <input type="button" class="btn btn-primary" id="approve" name="approve"
                                               onclick="javascript: approved(); return false;"
                                               value="Yes"/>
                                            <input class="btn btn-secondary" type="reset" value="No"
                                               onclick="javascript: deny(); return false;"/>
                                            <input type="hidden" name="consent" id="consent" value="deny"/>
                                        </div>
                                    </div>
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

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
</body>
</html>

