package com.alrayan.wso2.webapp.managementutility;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanConstants;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.StringDecryptionException;
import com.alrayan.wso2.common.utils.KeyStoreUtils;
import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import com.alrayan.wso2.user.core.util.UserManagementUtil;
import com.alrayan.wso2.webapp.managementutility.bean.AccountDisableStatus;
import com.alrayan.wso2.webapp.managementutility.bean.AccountLockStatus;
import com.alrayan.wso2.webapp.managementutility.bean.PINValidationRequest;
import com.alrayan.wso2.webapp.managementutility.bean.PINValidationResponse;
import com.alrayan.wso2.webapp.managementutility.bean.ResponseBean;
import com.alrayan.wso2.webapp.managementutility.bean.UserStatus;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    /**
     * Returns whether the PIN is valid against the given PIN code positions.
     *
     * @param pin      PIN code
     * @param username Salesforce ID of the user
     * @return a response containing whether the PIN code is valid or not
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{username}/pin")
    public Response validatePIN(PINValidationRequest pin, @PathParam("username") String username) {
        try {
            int tenantID = alRayanUserStoreManager.getTenantId();

            // Check user existence.
            if (!UserManagementUtil.isUserExist(username)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode()))
                        .build();
            }

            String userPIN = alRayanUserStoreManager.getUserPINForUserName(username, tenantID);
            PrivateKey privateKey = KeyStoreUtils
                    .getPrivateKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());
            String decryptedPIN = KeyStoreUtils.decryptFromPrivateKey(privateKey, Base64.decode(userPIN));

            // Get PIN code positions.
            String[] pinCodePositions = pin.getPinCodePositions().split(";");
            String pinCode = pin.getpIN();
            int count = 0;

            if (pinCodePositions.length != pinCode.length()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.INVALID_PIN_VALIDATION_REQUEST.getErrorMessageWithCode()))
                        .build();
            }

            // Validate PIN code against PIN code positions.
            for (String pinCodePosition : pinCodePositions) {
                char pinDigitToValidate = pinCode.charAt(count);
                count += 1;
                int pinCodePositionInt = Integer.parseInt(pinCodePosition);
                if (pinCodePositionInt < 0) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ResponseBean()
                                    .setMessage(AlRayanError.INVALID_PIN_CODE_POSITION.getErrorMessageWithCode()))
                            .build();
                }
                if (pinCodePositionInt > decryptedPIN.length()) {
                    return Response.status(Response.Status.OK)
                            .entity(new PINValidationResponse().setPinValid(false))
                            .build();
                }
                if (!(decryptedPIN.charAt(pinCodePositionInt - 1) == pinDigitToValidate)) {
                    return Response.status(Response.Status.OK)
                            .entity(new PINValidationResponse().setPinValid(false))
                            .build();
                }
            }
            return Response.status(Response.Status.OK)
                    .entity(new PINValidationResponse().setPinValid(true))
                    .build();
        } catch (UserStoreException e) {
            log.error(AlRayanError.INTERNAL_SERVER_ERROR_PIN_VALIDATION.getErrorMessageWithCode(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.INTERNAL_SERVER_ERROR_PIN_VALIDATION.getErrorMessageWithCode()))
                    .build();
        } catch (StringDecryptionException | CertificateException | NoSuchAlgorithmException |
                UnrecoverableKeyException | KeyStoreException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.PIN_DECRYPTION_ERROR.getErrorMessageWithCode()))
                    .build();
        }
    }

    /**
     * Updates the user account lock status.
     *
     * @param username          salesforce ID of the user
     * @param accountLockStatus account lock status to update
     * @return response containing the result of updating the lock status
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{username}/claim/accountLock")
    public Response accountLock(@PathParam("username") String username, AccountLockStatus accountLockStatus) {
        try {
            // Prepare claim.
            if (accountLockStatus.getLocked() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.BAD_REQUEST_ACCOUNT_LOCK_STATUS_NOT_GIVEN
                                        .getErrorMessageWithCode()))
                        .build();
            }
            Map<String, String> claimValueMap = new HashMap<>();
            claimValueMap.put(AlRayanConstants.CLAIM_URL_ACCOUNT_LOCKED, accountLockStatus.getLocked());

            // Check user existence.
            if (!UserManagementUtil.isUserExist(username)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode()))
                        .build();
            }

            // Modify claim.
            alRayanUserStoreManager.setUserClaimValues(username, claimValueMap,
                    AlRayanConstants.CLAIM_PROFILE);
            return Response.status(Response.Status.NO_CONTENT)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.BAD_REQUEST_ACCOUNT_LOCK_STATUS_INVALID.getErrorMessageWithCode()))
                    .build();
        } catch (UserStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(
                                    AlRayanError.INTERNAL_SERVER_ERROR_ACCOUNT_LOCK_MODIFY.getErrorMessageWithCode()))
                    .build();
        }
    }

    /**
     * Updates the user account disable status.
     *
     * @param username             salesforce ID of the user
     * @param accountDisableStatus account disable status to update
     * @return response containing the result of updating the disable status
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{username}/claim/accountDisabled")
    public Response accountDisable(@PathParam("username") String username, AccountDisableStatus accountDisableStatus) {
        try {
            // Prepare claim.
            if (accountDisableStatus.getDisabled() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.BAD_REQUEST_ACCOUNT_DISABLE_STATUS_NOT_GIVEN
                                        .getErrorMessageWithCode()))
                        .build();
            }
            Map<String, String> claimValueMap = new HashMap<>();
            claimValueMap.put(AlRayanConstants.CLAIM_URL_ACCOUNT_DISABLED, accountDisableStatus.getDisabled());

            // Check user existence.
            if (!UserManagementUtil.isUserExist(username)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode()))
                        .build();
            }

            // Modify claim.
            alRayanUserStoreManager.setUserClaimValues(username, claimValueMap,
                    AlRayanConstants.CLAIM_PROFILE);
            return Response.status(Response.Status.NO_CONTENT)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.BAD_REQUEST_ACCOUNT_DISABLE_STATUS_INVALID
                                    .getErrorMessageWithCode()))
                    .build();
        } catch (UserStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.INTERNAL_SERVER_ERROR_ACCOUNT_DISABLE_MODIFY
                                    .getErrorMessageWithCode()))
                    .build();
        }
    }

    /**
     * Returns the user status for the given user.
     *
     * @param username username of the user
     * @return user active and lock status
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{username}/claim/status")
    public Response userStatus(@PathParam("username") String username) {
        try {
            // Check user existence.
            if (!UserManagementUtil.isUserExist(username)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode()))
                        .build();
            }

            String isUserActive = alRayanUserStoreManager.getUserClaimValues(username,
                    new String[]{AlRayanConstants.CLAIM_URL_ACCOUNT_DISABLED}, AlRayanConstants.CLAIM_PROFILE)
                    .getOrDefault(AlRayanConstants.CLAIM_URL_ACCOUNT_DISABLED, "false");

            String isUserLocked = alRayanUserStoreManager.getUserClaimValues(username,
                    new String[]{AlRayanConstants.CLAIM_URL_ACCOUNT_LOCKED}, AlRayanConstants.CLAIM_PROFILE)
                    .getOrDefault(AlRayanConstants.CLAIM_URL_ACCOUNT_LOCKED, "false");

            UserStatus userStatus = new UserStatus.UserStatusBuilder()
                    .setSalesforceId(username)
                    .setDisabled(isUserActive)
                    .setLocked(isUserLocked)
                    .build();
            return Response.status(Response.Status.OK)
                    .entity(userStatus)
                    .build();
        } catch (UserStoreException e) {
            String message = "Error on getting user status claim values - " + e.getMessage();
            log.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.ERROR_ON_OBTAINING_USER_CLAIM_VALUES.getErrorMessageWithCode()))
                    .build();
        }
    }
}
