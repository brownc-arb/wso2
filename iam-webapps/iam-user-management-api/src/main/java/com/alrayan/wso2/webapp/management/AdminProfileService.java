package com.alrayan.wso2.webapp.management;


import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import com.alrayan.wso2.user.core.util.UserManagementUtil;
import com.alrayan.wso2.webapp.management.client.ApiException;
import com.alrayan.wso2.webapp.management.client.api.NotificationApi;
import com.alrayan.wso2.webapp.management.client.api.RecoverCredentialsApi;
import com.alrayan.wso2.webapp.management.client.model.Error;
import com.alrayan.wso2.webapp.management.client.bean.ErrorResponse;
import com.alrayan.wso2.webapp.management.client.bean.UserResponse;
import com.alrayan.wso2.webapp.management.client.bean.UserResponseWithCode;
import com.alrayan.wso2.webapp.management.client.model.ResetPasswordRequest;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminProfileService {

    private AlRayanUserStoreManager alRayanUserStoreManager;
    private static Log log = LogFactory.getLog(AdminProfileService.class);

    /**
     * Creates an instance of {@link AdminProfileService} rest service.
     *
     * @throws UserStoreException thrown when error on getting the user store manager
     */
    public AdminProfileService() throws UserStoreException {
        this.alRayanUserStoreManager = UserManagementUtil.getAlRayanUserManagerService();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{salesforceID}/getuser")
    public Response getUserWithSalesforce(@PathParam("salesforceID") String salesforceID) {

        String activeusername = null;
        UserResponse userResponse = null;
        ErrorResponse errorResponse = null;

        try {
            RecoverCredentialsApi recoverCredentialsApi = new RecoverCredentialsApi();
            activeusername = recoverCredentialsApi.getUsernameForSalesforceId(salesforceID);
        } catch (Exception e) {
            log.error(e);
        }

        if(!StringUtils.isEmpty(activeusername)) {
            userResponse = new UserResponse.UserResponseBuilder()
                    .setSalesforceId(salesforceID)
                    .setActiveUsername(activeusername)
                    .build();
        } else {
            errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .setErrorCode("18003")
                    .setErrorDescription("User not found")
                    .build();
        }

        return !StringUtils.isEmpty(activeusername)  ? Response.status(Response.Status.OK)
                .entity(userResponse)
                .build():Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{salesforceID}/getuser/code")
    public Response getResetCodeWithSFID(@PathParam("salesforceID") String salesforceID) {

        String activeusername = null;
        String resetcode = null;
        UserResponseWithCode userResponseWithCode = null;
        ErrorResponse errorResponse = null;

        try {
            RecoverCredentialsApi recoverCredentialsApi = new RecoverCredentialsApi();
            activeusername = recoverCredentialsApi.getUsernameForSalesforceId(salesforceID);
            resetcode = recoverCredentialsApi.getRecoverPasswordConfirmationKey(salesforceID);
        } catch (Exception e) {
            log.error(e);
        }

        if(!StringUtils.isEmpty(activeusername)) {
            userResponseWithCode = new UserResponseWithCode.UserResponseWithCodeBuilder()
                    .setSalesforceId(salesforceID)
                    .setActiveUsername(activeusername)
                    .setResetCode(resetcode)
                    .build();
        } else {
            errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .setErrorCode("18003")
                    .setErrorDescription("User not found")
                    .build();
        }

        return !StringUtils.isEmpty(activeusername) ? Response.status(Response.Status.OK)
                .entity(userResponseWithCode)
                .build():Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("internal/setpass")
    public Response setUserPassword(@FormParam("password") String password,
                                    @FormParam("code") String confirmationKey) {


        String passwordValidationRegex ="^[\\S]{1,30}$";
        String passwordErrorMessage ="Password pattern policy violated.";
        UserStoreConfigAdminService userStoreConfigAdminService = new UserStoreConfigAdminService();
        String successMessage = "{\n" +
                "\t\"response\":\"success\"\n" +
                "\t\n" +
                "}";

        try {
            UserStoreDTO[] userStoreDTOS = userStoreConfigAdminService.getSecondaryRealmConfigurations();


            for (UserStoreDTO userStoreDTO : userStoreDTOS) {
                if (!StringUtils.isEmpty(userStoreDTO.getClassName()) && userStoreDTO.getClassName().contains("AlRayanUserStoreManager"))

                    for (PropertyDTO propertyDTO : userStoreDTO.getProperties()) {

                        if ("PasswordJavaRegEx".equals(propertyDTO.getName())) {
                            passwordValidationRegex = propertyDTO.getValue();
                        }

                        if ("PasswordJavaRegExViolationErrorMsg".equals(propertyDTO.getName())) {
                            passwordErrorMessage = propertyDTO.getValue();
                        }
                    }
            }
        }
        catch (IdentityUserStoreMgtException e){
            log.error("Exception occured while accessing the userstores" + e);
        }

        if (StringUtils.isNotBlank(password)) {
            NotificationApi notificationApi = new NotificationApi();
            ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
            List<Property> properties = new ArrayList<Property>();

            Property tenantProperty = new Property();

            properties.add(tenantProperty);

            resetPasswordRequest.setKey(confirmationKey);
            resetPasswordRequest.setPassword(password);

            try {
                notificationApi.setPasswordPost(resetPasswordRequest);
            } catch (ApiException e) {

                Error error = new Gson().fromJson(e.getMessage(), Error.class);

                if (error != null) {

                    ErrorResponse errorResponse = null;

                    errorResponse = "18001".equals(error.getCode()) ? new ErrorResponse.ErrorResponseBuilder()
                            .setErrorCode(error.getCode())
                            .setErrorDescription(error.getDescription())
                            .build():new ErrorResponse.ErrorResponseBuilder()
                            .setErrorCode(error.getCode())
                            .setErrorDescription(passwordErrorMessage)
                            .build();

                    return "18001".equals(error.getCode()) ? Response.status(Response.Status.FORBIDDEN)
                            .entity(errorResponse)
                            .build():Response.status(Response.Status.BAD_REQUEST)
                            .entity(errorResponse)
                            .build();
                }
            }
            }

        return Response.status(Response.Status.OK)
                .entity(successMessage)
                .build();
    }
}
