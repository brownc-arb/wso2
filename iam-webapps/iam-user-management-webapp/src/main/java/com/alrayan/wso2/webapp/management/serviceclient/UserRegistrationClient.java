package com.alrayan.wso2.webapp.management.serviceclient;

import com.alrayan.wso2.webapp.management.IdentityManagementEndpointConstants;
import com.alrayan.wso2.webapp.management.IdentityManagementEndpointUtil;
import com.alrayan.wso2.webapp.management.IdentityManagementServiceUtil;
import com.alrayan.wso2.webapp.management.serviceclient.beans.SelfRegistrationRequest;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import com.alrayan.wso2.webapp.management.serviceclient.beans.ConfirmSelfRegistrationRequest;
import com.alrayan.wso2.webapp.management.serviceclient.client.proxy.api.NotificationUsernameRecoveryResource;
import com.alrayan.wso2.webapp.management.serviceclient.client.proxy.api.SelfUserRegistrationResource;

import java.util.Map;
import javax.ws.rs.core.Response;

public class UserRegistrationClient {
    StringBuilder builder = new StringBuilder();
    String url = IdentityManagementServiceUtil.getInstance().getServiceContextURL()
            .replace(IdentityManagementEndpointConstants.UserInfoRecovery.SERVICE_CONTEXT_URL_DOMAIN,
                     IdentityManagementEndpointConstants.UserInfoRecovery.REST_API_URL_DOMAIN);

    public Response getAllClaims(String tenantDomain) {
        NotificationUsernameRecoveryResource notificationUsernameRecoveryResource = JAXRSClientFactory
                .create(url, NotificationUsernameRecoveryResource.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response responseObj = notificationUsernameRecoveryResource.getAllLocalSupportedClaims();
        return responseObj;
    }
    public Response registerUser(SelfRegistrationRequest registrationRequest, Map<String, String> headers) {
        SelfUserRegistrationResource selfUserRegistrationResource = IdentityManagementEndpointUtil
                .create(url, SelfUserRegistrationResource.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider(), null, headers);
        Response responseObj = selfUserRegistrationResource.registerUser(registrationRequest);
        return responseObj;
    }

    public Response confirmUser(ConfirmSelfRegistrationRequest confirmSelfRegistrationRequest) {
        SelfUserRegistrationResource selfUserRegistrationResource = JAXRSClientFactory
                .create(url, SelfUserRegistrationResource.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response responseObj = selfUserRegistrationResource.confirmCode(confirmSelfRegistrationRequest);
        return responseObj;
    }
}
