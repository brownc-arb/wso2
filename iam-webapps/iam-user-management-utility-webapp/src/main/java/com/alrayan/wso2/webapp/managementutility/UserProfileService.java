package com.alrayan.wso2.webapp.managementutility;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.AccessTokenValidationFailedException;
import com.alrayan.wso2.common.exception.PINValidationFailedException;
import com.alrayan.wso2.common.exception.StringDecryptionException;
import com.alrayan.wso2.common.exception.UserNameNotFoundException;
import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import com.alrayan.wso2.user.core.util.UserManagementUtil;
import com.alrayan.wso2.webapp.managementutility.bean.PINChangeRequest;
import com.alrayan.wso2.webapp.managementutility.bean.ResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.xml.encryption.P;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import uk.co.alrayan.PinUtils;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST API to manage user functions.
 *
 * @since 1.0.0
 */
@Path("user")
public class UserProfileService {

    private AlRayanUserStoreManager alRayanUserStoreManager;
    private static Log log = LogFactory.getLog(UserProfileService.class);
    private static TokenMgtDAO tokenMgtDAO;

    // Initialise token management service.
    static {
        tokenMgtDAO = new TokenMgtDAO();
    }

    /**
     * Creates an instance of {@link UserProfileService} rest service.
     *
     * @throws UserStoreException thrown when error on getting the user store manager
     */
    public UserProfileService() throws UserStoreException {
        this.alRayanUserStoreManager = UserManagementUtil.getAlRayanUserManagerService();
    }

    /**
     * Validates the old PIN and if successful, updates the PIN with the new PIN.
     * <p>
     * Make sure you add the following configuration to {@code [WSO2-IAM]/repository/conf/identity/identity.xml}
     * <p>
     * {@code <Resource context="(.*)/usermanagement/v1/user/(.*)" http-method="all" secured="true"><Permissions>/permission/admin/alrayan/digitalBankUser</Permissions></Resource>}
     * <p>
     * This permission setting is mandatory for the proper invocation of this method.
     *
     * @param pinChangeRequest PIN change request
     * @return response containing change PIN result
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{username}/pin")
    public Response changePIN(PINChangeRequest pinChangeRequest, @Context HttpHeaders headers,
                              @PathParam("username") String username) {

        log.info(">> changePIN for:"+ username + " requested via API");

        try {
            // Validate user
            String authorizationHeader = headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
            if (StringUtils.isEmpty(authorizationHeader) ||
                !(authorizationHeader.toUpperCase().startsWith("BEARER "))) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.AUTHORIZATION_HEADER_MISSING.getErrorMessageWithCode()))
                        .build();
            }

            String[] encodedCredentialComponents = authorizationHeader.split("(?i)BEARER");
            String accessTokenInRequest = StringUtils
                    .trim(encodedCredentialComponents[encodedCredentialComponents.length - 1]);
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            AuthenticatedUser authenticatedUser = UserManagementUtil.buildAuthenticatedUser(username,
                    AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue(), tenantDomain, false);
            long count = tokenMgtDAO.getAccessTokensForUser(authenticatedUser).stream()
                    .filter(accessToken -> accessToken.equals(accessTokenInRequest))
                    .count();
            if (count == 0L) {
                throw new AccessTokenValidationFailedException(
                        AlRayanError.ACCESS_TOKEN_VALIDATION_FAILED.getErrorMessageWithCode());
            }

            username = UserCoreUtil.removeDomainFromName(username);
            String currentPINFromRequest = pinChangeRequest.getCurrentPIN();
            String newPINFromRequest = pinChangeRequest.getNewPIN();

            int retVal = PinUtils.checkPin(newPINFromRequest);
            if (retVal < 0) {
                log.info(">> checkPin failed with code:"+ retVal + " for user :" + username);
                throw new PINValidationFailedException("New Pin code not complex enough");
            }
            alRayanUserStoreManager.changePIN(username, currentPINFromRequest, newPINFromRequest);

            // Change PIN code is a success.
            return Response.status(Response.Status.NO_CONTENT)
                    .build();
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error(AlRayanError.PIN_VALIDATION_INTERNAL_SERVER_ERROR.getErrorMessageWithCode(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.PIN_VALIDATION_INTERNAL_SERVER_ERROR.getErrorMessageWithCode() +
                                        " - " + e.getMessage()))
                    .build();
        } catch (UserNameNotFoundException | AccessTokenValidationFailedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ResponseBean()
                            .setMessage(
                                    AlRayanError.CHANGE_PIN_FAILED_INCORRECT_CREDENTIALS.getErrorMessageWithCode()))
                    .build();
        } catch (StringDecryptionException | IOException | CertificateException | NoSuchAlgorithmException |
                UnrecoverableKeyException | KeyStoreException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.PIN_DECRYPTION_ERROR.getErrorMessageWithCode()))
                    .build();
        } catch (IdentityOAuth2Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.ACCESS_TOKEN_VALIDATION_INTERNAL_SERVER_ERROR
                                    .getErrorMessageWithCode()))
                    .build();
        } catch (PINValidationFailedException e) {
                 Response response = Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.PINCODE_NOT_COMPLEX_ENOUGH
                                        .getErrorMessageWithCode()))
                 .build();
                 log.info("<< changePIN for:"+ username + "returning with response:" + response );
                 return response;
        }
    }

    /**
     * Validates the old PIN and if successful, updates the PIN with the new PIN.
     * <p>
     * Make sure you add the following configuration to {@code [WSO2-IAM]/repository/conf/identity/identity.xml}
     * <p>
     * {@code <Resource context="(.*)/usermanagement/v1/user/(.*)" http-method="all" secured="true"><Permissions>/permission/admin/alrayan/digitalBankUser</Permissions></Resource>}
     * <p>
     * This permission setting is mandatory for the proper invocation of this method.
     *
     * @return response containing change PIN result
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{username}/changepin")
    public Response changePIN(@FormParam("newPin") String newPin, @Context HttpHeaders headers,
                              @PathParam("username") String username) {

        log.info(">> changePIN (via FORM) for:"+ username);
        try {
            // Validate user
            String authorizationHeader = headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
            if (StringUtils.isEmpty(authorizationHeader) ||
                    !(authorizationHeader.toUpperCase().startsWith("BEARER "))) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseBean()
                                .setMessage(AlRayanError.AUTHORIZATION_HEADER_MISSING.getErrorMessageWithCode()))
                        .build();
            }

            String[] encodedCredentialComponents = authorizationHeader.split("(?i)BEARER");
            String accessTokenInRequest = StringUtils
                    .trim(encodedCredentialComponents[encodedCredentialComponents.length - 1]);
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            AuthenticatedUser authenticatedUser = UserManagementUtil.buildAuthenticatedUser(username,
                    AlRayanConfiguration.AL_RAYAN_USERSTORE_PSU.getValue(), tenantDomain, false);
            long count = tokenMgtDAO.getAccessTokensForUser(authenticatedUser).stream()
                    .filter(accessToken -> accessToken.equals(accessTokenInRequest))
                    .count();
            if (count == 0L) {
                throw new AccessTokenValidationFailedException(
                        AlRayanError.ACCESS_TOKEN_VALIDATION_FAILED.getErrorMessageWithCode());
            }

            username = UserCoreUtil.removeDomainFromName(username);

            int retVal = PinUtils.checkPin(newPin);
            if (retVal < 0) {
                log.info(">> changePIN (via FORM) for:"+ username + " PIN code not complex enough");
                throw new PINValidationFailedException("New Pin code not complex enough");
                
            }

            alRayanUserStoreManager.changePIN(username,newPin);

            // Change PIN code is a success.
            log.info(">> changePIN (via FORM) for:"+ username + " is a success");

            return Response.status(Response.Status.NO_CONTENT)
                    .build();
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            log.error(AlRayanError.PIN_VALIDATION_INTERNAL_SERVER_ERROR.getErrorMessageWithCode(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.PIN_VALIDATION_INTERNAL_SERVER_ERROR.getErrorMessageWithCode() +
                                    " - " + e.getMessage()))
                    .build();
        } catch (AccessTokenValidationFailedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ResponseBean()
                            .setMessage(
                                    AlRayanError.CHANGE_PIN_FAILED_INCORRECT_CREDENTIALS.getErrorMessageWithCode()))
                    .build();
        } catch (IdentityOAuth2Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseBean()
                            .setMessage(AlRayanError.ACCESS_TOKEN_VALIDATION_INTERNAL_SERVER_ERROR
                                    .getErrorMessageWithCode()))
                    .build();
        }  catch (PINValidationFailedException e) {
                Response response = Response.status(Response.Status.NOT_ACCEPTABLE)
                .entity(new ResponseBean()
                        .setMessage(AlRayanError.PINCODE_NOT_COMPLEX_ENOUGH
                                .getErrorMessageWithCode()))
                .build();
         
                log.info("<< changePIN for:"+ username + "returning with response:" + response.getStatus());
                return response;
        }
    }


}
