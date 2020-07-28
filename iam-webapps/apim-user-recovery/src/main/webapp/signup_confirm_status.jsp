<%@ page import="com.alrayan.wso2.common.AlRayanConfiguration" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Al Rayan Credential Management</title>
    <link rel="shortcut icon" href="assets/images/favicon.jpg">
    <link type="text/css" rel="stylesheet"
          href="assets/libs/bootstrap_3.3.5/css/bootstrap.css"/>
    <link type="text/css" rel="stylesheet"
          href="assets/libs/font-wso2_1.0.2/css/font-wso2.css"/>
    <link type="text/css" rel="stylesheet"
          href="assets/libs/font-awesome/css/font-awesome.css"/>
    <link type="text/css" rel="stylesheet"
          href="assets/libs/bootstrap-select/css/bootstrap-select.css"/>
    <link type="text/css" rel="stylesheet"
          href="assets/libs/bootstrap-rating/bootstrap-rating.css"/>
    <link type="text/css" rel="stylesheet" href="assets/css/customize-template.css"/>
</head>
<body>

<%
    String serverURL =  AlRayanConfiguration.SERVERHOST_WITH_PROXYPORT.getValue();
%>
<c:choose>
    <c:when test="${status.verified}">
        <div class="container">
            <div id="infoModel" class="modal fade" role="dialog">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;
                            </button>
                            <h4 class="modal-title">Information</h4>
                        </div>
                        <div class="modal-body">
                            <p>
                                Thanks for verifying your account. You Will be able to access your account in couple of minutes
                            </p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <script src="assets/libs/jquery_1.11.0/jquery-1.11.3.min.js"></script>
        <script src="assets/libs/bootstrap_3.3.5/js/bootstrap.js"></script>
        <script src="assets/libs/bootstrap-rating/bootstrap-rating.js"></script>
        <script type="application/javascript">
          $(document).ready(function () {
            var infoModel = $("#infoModel");
            infoModel.modal("show");
            infoModel.on('hidden.bs.modal', function () {
              location.href = "<%= serverURL %>/store/site/pages/login.jag";
            })
          });
        </script>
    </c:when>
    <c:otherwise>
        <div class="container">
            <div id="infoModel" class="modal fade" role="dialog">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;
                            </button>
                            <h4 class="modal-title">Information</h4>
                        </div>
                        <div class="modal-body">
                            <p>
                                Account confirmation failed. Please contact the system
                                administrator.
                            </p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <script src="assets/libs/jquery_1.11.0/jquery-1.11.3.min.js"></script>
        <script src="assets/libs/bootstrap_3.3.5/js/bootstrap.js"></script>
        <script src="assets/libs/bootstrap-rating/bootstrap-rating.js"></script>
        <script type="application/javascript">
          $(document).ready(function () {
            var infoModel = $("#infoModel");
            infoModel.modal("show");
            infoModel.on('hidden.bs.modal', function () {
              if (document.referrer !== '') {
                location.href = history.back();
              } else {
                location.href = "<%= serverURL %>/store/site/pages/login.jag";
              }
            })
          });
        </script>
    </c:otherwise>
</c:choose>
</body>
</html>
