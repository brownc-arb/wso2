<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="includes/header.jsp"></jsp:include>
<h4>
    Enter captcha text shown below
</h4>
<br/>
<form method="POST" action="${pageContext.request.contextPath}/confirmReg" id="registerConfirmForm"
      class="form-horizontal">
    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
            <div class="input-group input-wrap">
                <label>
                    Captcha <br/>
                    <img src="${captchaImageUrl}"
                         alt='If you can not see the captcha " +
                        "image please refresh the page or click the link again.'/>
                </label>
            </div>
        </div>
    </div>
    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
            <div class="input-group input-wrap">
                <label>
                    Enter captcha text
                    <input type="text" name="captchaAnswer" id="captchaAnswer" class="form-control"
                           required placeholder="Captcha text"
                           value="${captcha.userAnswer}"/>
                </label>
            </div>
        </div>
    </div>
    <input type="hidden" name="captchaSecretKey" value="${captcha.secretKey}"/>
    <input type="hidden" name="captchaImagePath" value="${captcha.imagePath}"/>
    <div class="form-group">
        <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
            <div class="input-group input-wrap">
                <input type="submit" value="Submit" class="btn btn-primary add-margin-right-2x"/>
            </div>
        </div>
    </div>
</form>
<jsp:include page="includes/footer.jsp"></jsp:include>
