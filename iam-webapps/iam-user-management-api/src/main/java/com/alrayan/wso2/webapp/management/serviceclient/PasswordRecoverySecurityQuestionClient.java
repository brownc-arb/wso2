/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alrayan.wso2.webapp.management.serviceclient;

import com.alrayan.wso2.webapp.management.IdentityManagementEndpointConstants;
import com.alrayan.wso2.webapp.management.IdentityManagementEndpointUtil;
import com.alrayan.wso2.webapp.management.IdentityManagementServiceUtil;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.wso2.carbon.identity.mgt.beans.User;
import com.alrayan.wso2.webapp.management.serviceclient.beans.UserPassword;
import com.alrayan.wso2.webapp.management.serviceclient.beans.VerifyAllAnswerRequest;
import com.alrayan.wso2.webapp.management.serviceclient.beans.VerifyAnswerRequest;
import com.alrayan.wso2.webapp.management.serviceclient.client.proxy.api.PasswordRecoverySecurityQuestion;

import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * REST client for password reset with security question
 */
public class PasswordRecoverySecurityQuestionClient {

    StringBuilder builder = new StringBuilder();
    String url = IdentityManagementServiceUtil.getInstance().getServiceContextURL()
            .replace(IdentityManagementEndpointConstants.UserInfoRecovery.SERVICE_CONTEXT_URL_DOMAIN,
                    IdentityManagementEndpointConstants.UserInfoRecovery.REST_API_URL_DOMAIN);

    public Response initiateUserChallengeQuestion(User user) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestion(user);
        return response;
    }

    public Response verifyUserChallengeAnswer(VerifyAnswerRequest verifyAnswerRequest, Map<String, String> headers) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = IdentityManagementEndpointUtil.create(url,
                PasswordRecoverySecurityQuestion.class,
                IdentityManagementServiceUtil.getInstance().getJSONProvider(), null, headers);
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswer(verifyAnswerRequest);
        return response;
    }

    public Response updatePassword(UserPassword userPassword) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.updatePassword(userPassword);
        return response;
    }

    public Response initiateUserChallengeQuestionAtOnce(User user) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestionAtOnce(user);
        return response;
    }

    public Response verifyUserChallengeAnswerAtOnce(VerifyAllAnswerRequest verifyAllAnswerRequest, Map<String, String> headers) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = IdentityManagementEndpointUtil.create(url,
                PasswordRecoverySecurityQuestion.class,
                IdentityManagementServiceUtil.getInstance().getJSONProvider(), null, headers);
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswerAtOnce(verifyAllAnswerRequest);
        return response;
    }



}
