<!doctype html>
<%--
  ~ Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    String code = request.getParameter("code");
	 	String error_description = request.getParameter("error_description");
%>
<html>
<head>
    <jsp:include page="includes/head.jsp" />
		<style>
			  .token-field { margin-left: 10px; }
		</style>
</head>

<body>

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
                                    <%
                                        if (code != null) {
                                    %>
                                        <h5>Copy and use the below authorization code in your token request.</h5>
                                        <h3 style="margin-top: 30px;">
																					<div class="form-inline">
																						<label>Authorization Code :</label>
																						<span class="token-field input-group input-group-lg">
																							<span id="token" class="input-group-addon" value="<%=Encode.forHtml(code)%>"><%=Encode.forHtml(code)%></span>
																							<div class="input-group-btn">
																									<span class="btn btn-primary btn-lg copy-button" data-clipboard-target="#token" id="copy-button" data-toggle="tooltip" title="Copy to Clipboard">
																											<i class="fw fw-copy" alt="Copy to clipboard"></i>
																									</span>		      
																							</div>
																						</span>
																					</div>
																				</h3>		
                                    <%
                                    } else {
                                    %>
                                        <h5>No authorization code received.</h5>
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
                
<script src="libs/clipboard-1.7.1/clipboard.min.js"></script>
<script>
	
	var copyButton = '#copy-button';
	new Clipboard(copyButton);

  $(copyButton).tooltip();


  $(copyButton).bind('click', function() {
     	$(copyButton).trigger('copied', ['Copied!']);
  });

  $(copyButton).bind('copied', function(event, message) {
    	$(this).attr('title', message)
        	.tooltip('fixTitle')
        	.tooltip('show')
        	.attr('title', "Copy to Clipboard")
        	.tooltip('fixTitle');
  });

</script>
</body>
</html>


