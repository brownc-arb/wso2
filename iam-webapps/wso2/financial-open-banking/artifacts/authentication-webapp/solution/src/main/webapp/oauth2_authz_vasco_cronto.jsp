<%--
 ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 ~
 ~ This software is the property of WSO2 Inc. and its suppliers, if any.
 ~ Dissemination of any information or reproduction of any material contained
 ~ herein is strictly forbidden, unless permitted by WSO2 in accordance with
 ~ the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 ~ language governing the permissions and limitations under this license,
 ~ please see the license as well as any agreement you’ve entered into with
 ~ WSO2 governing the purchase of this software and any associated services.
--%>

<%@ page import="com.alrayan.wso2.common.AlRayanError" %>
<%@ page import="com.alrayan.wso2.vasco.VASCOException" %>
<%@ page import="com.alrayan.wso2.vasco.authentication.ImageGeneratorCommand" %>
<%@ page import="com.alrayan.wso2.vasco.authentication.SecureChallengeCommand" %>
<%@ page import="com.alrayan.wso2.vasco.authentication.SignGenCommand" %>
<%@ page import="com.alrayan.wso2.vasco.model.SecureChallengeResult" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.awt.image.BufferedImage" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.IOException" %>
<%@ page import="javax.imageio.ImageIO" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ include file="localize.jsp" %>
<%!
    /**
     * Returns the VASCO challenge key.
     *
     * @param loggedInUser logged in user
     * @param consentId consent ID
     * @param app application name
     * @return VASCO challenge key result
     * @throws VASCOException thrown when error on CRONTO image generation
     */
    private SecureChallengeResult getVASCOChallengeKey(String loggedInUser, String consentId, String app,String dataset
    ,String type)
            throws VASCOException {

//        if(StringUtils.isEmpty(dataset) || StringUtils.isEmpty(type)) {
//            SecureChallengeCommand secureChallengeCommand =
//                    new SecureChallengeCommand(loggedInUser, consentId, app);
//            return secureChallengeCommand.execute();
//        }

        SignGenCommand signGenCommand =
                new SignGenCommand(loggedInUser, consentId, app, dataset, type);
        return signGenCommand.execute();
    }

    /**
     * Generates and returns the CRONTO image base64 binary.
     *
     * @param result secure challenge result
     * @return CRONTO image base64 binary
     * @throws VASCOException thrown when error on CRONTO image generation
     * @throws IOException thrown when error on CRONTO image generation
     */
    private String generateCrontoImage(SecureChallengeResult result)
            throws VASCOException, IOException {
        ImageGeneratorCommand imageGeneratorCommand =
                new ImageGeneratorCommand(result.getRequestMessage());
        BufferedImage bufferedImage = imageGeneratorCommand.execute();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        byte[] imageInByteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return javax.xml.bind.DatatypeConverter.printBase64Binary(imageInByteArray);
    }
%>
<%
String loggedInUser = request.getParameter("loggedInUser");
String consentId = request.getParameter("consentId");
String app = request.getParameter("app");
String dataset = request.getParameter("dataset");
String type = request.getParameter("type");

// Generate VASCO CRONTO code.
try {
    SecureChallengeResult result = getVASCOChallengeKey(loggedInUser, consentId, app, dataset, type);
    String challengeKey = result.getChallengeKey();
    String base64Binary = generateCrontoImage(result);
%>
<img src="data:image/jpg;base64, <%= base64Binary %>"
     alt="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "cronto.code")%>"/>
<input type="hidden" name="vascoChallengeKey" id="vascoChallengeKey"
<%--ALRAYANSUB-195---%>
       value="<%= Encode.forHtmlAttribute(challengeKey) %>"/>
<%
} catch (VASCOException e) {
%>
<div class="alert alert-danger cronto-validate-error">
    Error occurred on generating the VASCO CRONTO image.<br /><br />
    Please contact the bank support service if the error persists.
</div>
<%
}
%>
