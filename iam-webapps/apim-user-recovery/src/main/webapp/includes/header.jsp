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
    <script type="text/javascript" src="assets/libs/jquery_1.11.0/jquery-1.11.3.min.js"></script>
    <script type="text/javascript" src="assets/libs/bootstrap-rating/bootstrap-rating.js"></script>
</head>
<body class="sticky-footer">
<header class="header header-default">
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs brand-container">
            <a href="<%= request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() %>/store/site/pages/index.jag"
               title="Al Rayan Open Banking">
                <img src="assets/images/Al_Rayan_Logo-150min.jpg"
                     class="logo" alt="Al Rayan Open Banking">
            </a>
            <h2 class="text-center-sm text-center-xs text-center-md text-right">
                Credential management
            </h2>
        </div>
    </div>
</header>
<div class="body-container">
    <br/>
    <br/>
    <br/>
    <div id="body-content">
        <section class="page container">
            <div class="row">
                <div class="login-form-wrapper">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <div class="data-container">
