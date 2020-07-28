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
<%@ page import="com.alrayan.wso2.vasco.authentication.AuthUserChallengeCommand" %>
<%@ page import="com.alrayan.wso2.vasco.authentication.AuthUserSigGenCommand" %>
<%@ page import="org.wso2.carbon.user.core.util.UserCoreUtil" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="java.util.Map" %>
<%
    String jsonString = request.getReader().readLine();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> map = mapper.readValue(jsonString, Map.class);
    String vascoChallengeKey = map.get("vascoChallengeKey");
    String cronto = map.get("cronto");
    String app = map.get("app");
    String username = map.get("username");
    String type = map.get("type");

    if(StringUtils.isNotEmpty(vascoChallengeKey)) {
        vascoChallengeKey = vascoChallengeKey.replace("/", "");
    }

    boolean isAuthUserSuccess = false;

    try {

//        if(!"payments".equals(type)) {
//            AuthUserChallengeCommand authUserChallengeCommand =
//                    new AuthUserChallengeCommand(UserCoreUtil.removeDomainFromName(username),
//                            cronto, vascoChallengeKey, app);
//            isAuthUserSuccess = authUserChallengeCommand.execute();
//        }
//        else {
//            AuthUserSigGenCommand authUserSigGenCommand =
//                    new AuthUserSigGenCommand(UserCoreUtil.removeDomainFromName(username),
//                            cronto, vascoChallengeKey, app);
//            isAuthUserSuccess = authUserSigGenCommand.execute();
//        }
        AuthUserSigGenCommand authUserSigGenCommand = new AuthUserSigGenCommand(UserCoreUtil.removeDomainFromName(username),
                cronto, vascoChallengeKey, app);
        isAuthUserSuccess = authUserSigGenCommand.execute();

        if (!isAuthUserSuccess) {
            String statusMsg = AlRayanError.VASCO_CRONTO_CODE_VALIDATION_FAILED
                    .getErrorMessageWithCode();
            response.sendError(401, statusMsg);
            return;
        }
        response.setStatus(204);
    } catch (VASCOException e) {
        String statusMsg = AlRayanError.ERROR_ON_VASCO_AUTH_USER_REQUEST.getErrorMessageWithCode();
        response.sendError(500, statusMsg);
        return;
    }
%>