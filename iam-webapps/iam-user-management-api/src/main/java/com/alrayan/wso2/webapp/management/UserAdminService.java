package com.alrayan.wso2.webapp.management;

/*
    developed by ARB
 */

import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import com.alrayan.wso2.user.core.util.UserManagementUtil;
import com.alrayan.wso2.webapp.management.client.ApiException;
import com.alrayan.wso2.webapp.management.client.api.NotificationApi;
import com.alrayan.wso2.webapp.management.client.api.RecoverCredentialsApi;
import com.alrayan.wso2.webapp.management.client.bean.ValueResponse;
import com.alrayan.wso2.webapp.management.client.model.Error;
import com.alrayan.wso2.webapp.management.client.bean.ErrorResponse;
import com.alrayan.wso2.webapp.management.client.bean.UserResponse;
import com.alrayan.wso2.webapp.management.client.bean.UserResponseWithCode;
import com.alrayan.wso2.webapp.management.client.model.ResetPasswordRequest;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.identity.user.store.configuration.UserStoreConfigAdminService;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.alrayan.wso2.webapp.management.client.model.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * REST API to manage user admin functions.
 *
 * @since 1.0.0
 */
@Path("/admin2")
@Produces(MediaType.APPLICATION_JSON)

public class UserAdminService {
    private AlRayanUserStoreManager alRayanUserStoreManager;
    private static Log log = LogFactory.getLog(UserAdminService.class);

    /**
     * Creates an instance of {@link UserAdminService} rest service.
     *
     * @throws UserStoreException thrown when error on getting the user store manager
     */
    public UserAdminService() throws UserStoreException {
        this.alRayanUserStoreManager = UserManagementUtil.getAlRayanUserManagerService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{username}/recover")
    public Response recoverPasswordConfirmationKey(@PathParam("username") String username) {

        ValueResponse valueResponse = null;
        ErrorResponse errorResponse = null;
        RecoverCredentialsApi recoverCredentialsApi = new RecoverCredentialsApi();

        String confirmationKey = "";

        try {
            confirmationKey = recoverCredentialsApi.getRecoverPasswordConfirmationKey(username);
        } catch (IdentityRecoveryException e) {
            e.printStackTrace();
        }

        if(!StringUtils.isEmpty(confirmationKey)) {
            valueResponse = new ValueResponse();
            valueResponse.setStrResponse(confirmationKey);

        } else {
            errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .setErrorCode("18003")
                    .setErrorDescription("User not found")
                    .build();
        }

        return !StringUtils.isEmpty(confirmationKey)  ? Response.status(Response.Status.OK)
                .entity(valueResponse)
                .build():Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
