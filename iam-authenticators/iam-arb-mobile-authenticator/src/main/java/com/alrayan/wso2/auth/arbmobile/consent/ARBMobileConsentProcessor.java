package com.alrayan.wso2.auth.arbmobile.consent;

import com.alrayan.wso2.auth.arbmobile.util.ARBMobileConstants;
import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.crypto.ARBCryptoHandler;
import com.alrayan.wso2.common.crypto.ARBSignatureUtils;
import com.alrayan.wso2.common.exception.ARBCryptoException;
import com.alrayan.wso2.common.jwt.ARBJWTTokenService;
import com.alrayan.wso2.common.utils.KeyStoreUtils;
import com.wso2.finance.open.banking.common.exception.ConsentMgtException;
import com.wso2.finance.open.banking.uk.consent.mgt.model.AccountSetupResponse;
import com.wso2.finance.open.banking.uk.consent.mgt.model.AdvancedAccountConsent;
import com.wso2.finance.open.banking.uk.consent.mgt.model.AdvancedPaymentConsent;
import com.wso2.finance.open.banking.uk.consent.mgt.model.DebtorAccount;
import com.wso2.finance.open.banking.uk.consent.mgt.service.AccountsConsentMgtService;
import com.wso2.finance.open.banking.uk.consent.mgt.service.PaymentsConsentMgtService;
import com.wso2.finance.open.banking.uk.consent.mgt.util.PermissionsEnum;

import org.apache.axiom.om.util.Base64;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

/**
 * Consent details are processed in ARBMobileConsentProcessor.
 *
 * @since 1.0.0
 */
public class ARBMobileConsentProcessor implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ARBMobileConsentProcessor.class);
    private static final long serialVersionUID = -1759341308244553658L;
    private static final String CLIENT_ID = "client_id";
    private static final String SCOPE = "scope";
    private static final String ACCOUNTS_SCOPE = "accounts";
    private static final String PAYMENTS_SCOPE = "payments";
    private static final String CLAIMS = "claims";
    private static final String USERINFO = "userinfo";
    private static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    private static final String VALUE = "value";

    ARBCryptoHandler cryptoHandler = new ARBCryptoHandler();
    ARBJWTTokenService arbjwtTokenService = new ARBJWTTokenService();
    ARBSignatureUtils arbSignatureUtils = new ARBSignatureUtils();

    /**
     * Returns the consent Details for the particular TPP.
     *
     * @param consentId  consent ID
     * @param clientId client ID
     * @return map which contains the consent details such as permissions and transaction allowed time limit
     */
    public Map<String, Object> getConsentDetails(String consentId, String clientId) {
        Map<String, Object> accountDataSet = new HashMap<>();
        try {
            AccountsConsentMgtService accountsConsentMgtService = (AccountsConsentMgtService) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getOSGiService(
                            AccountsConsentMgtService.class, null);
            AccountSetupResponse accountSetupResponse = accountsConsentMgtService
                    .getAccountConsents(consentId, clientId);

            JSONArray permissionArray = new JSONArray();
            if (StringUtils.isEmpty(consentId)) {
                accountDataSet.put(ARBMobileConstants.IS_ERROR, "Account_id is not been found");
            }

            //If the account response is received and it does't have the error response such as "BAD_REQUEST"
            // or "FORBIDDEN".
            if (accountSetupResponse != null && !accountSetupResponse.isError()) {
                if (!ARBMobileConstants.AWAITING_AUTHORISATION.equals(accountSetupResponse.
                        getAccountResponseData().getStatus().toString())) {
                    accountDataSet.put(ARBMobileConstants.IS_ERROR, "Consent is not in AwaitingAuthorisation state");
                    return accountDataSet;
                }

                //Getting the permission list
                List<PermissionsEnum> permissionsEnumList = accountSetupResponse
                        .getAccountResponseData().getPermissions();
                for (PermissionsEnum permission : permissionsEnumList) {
                    permissionArray.add(permission.toString());
                }

                if (permissionArray != null && permissionArray.size() < 1) {
                    accountDataSet.put(ARBMobileConstants.IS_ERROR, "No account permissions found");
                    return accountDataSet;
                }

                if (permissionArray != null) {
                    accountDataSet.put(ARBMobileConstants.PERMISSIONS, permissionArray.toString());
                }

                JSONArray datesArray = new JSONArray();
                String expireTime = accountSetupResponse.getAccountResponseData().getExpirationDateTime();
                String transactionFromTime = accountSetupResponse.getAccountResponseData().getTransactionFromDateTime();
                String transactionToTime = accountSetupResponse.getAccountResponseData().getTransactionToDateTime();

                if (expireTime != null) {
                    datesArray.add(ARBMobileConstants.EXPIRATION_DATE_TIME + " : " + expireTime);
                }

                if (transactionFromTime != null) {
                    datesArray.add(ARBMobileConstants.TRANSACTION_FROM_DATE_TIME + " : " + transactionFromTime);
                }

                if (transactionToTime != null) {
                    datesArray.add(ARBMobileConstants.TRANSACTION_TO_DATE_TIME + " : " + transactionToTime);
                }

                accountDataSet.put(ARBMobileConstants.DATES, datesArray.toString());
            }

        } catch (ConsentMgtException e) {
            log.error(AlRayanError.ERROR_RETRIEVING_CONSENT_DETAILS.getMessage());
            accountDataSet.put(ARBMobileConstants.IS_ERROR, AlRayanError.ERROR_RETRIEVING_CONSENT_DETAILS.getMessage());
        }
        return accountDataSet;
    }


    /**
     * Returns the consent Details for the particular TPP.
     *
     * @param consent  consent details
     * @param scope requested scope, this implies account or payment
     * @return this returns a JWT message which will be sent to the mobile client
     */
    public String generateSecureMessage(org.json.JSONArray consent, String scope, AuthenticationContext context
            , SecretKey secretKey) throws AuthenticationFailedException {

        X509Certificate x509Certificate = null;

        JSONObject jsonObject = new JSONObject();

        try {
            x509Certificate = (X509Certificate) IdentityApplicationManagementUtil
                    .decodeCertificate(context.getExternalIdP().getIdentityProvider().getCertificate());

        } catch (CertificateException e) {
            log.error("Error retrieving the application certificate ", e);
            throw new AuthenticationFailedException("Error retrieving the application certificate");
        }

        try {
            PublicKey publicKey = x509Certificate.getPublicKey();

            byte[] consentCipherText = cryptoHandler.encryptUsingSymmetricKey(secretKey, consent.toString());
            byte[] symmetricCipher = cryptoHandler.encryptSymmetricKey(secretKey, publicKey);

            jsonObject.put("issuer", AlRayanConfiguration.OPEN_BANKING_JWT_ISSUER.getValue());
            jsonObject.put("consentvalue", Base64.encode(consentCipherText));
            jsonObject.put("scope", Base64.encode(scope.getBytes()));
            jsonObject.put("nonse", Base64.encode(symmetricCipher));

        }  catch (ARBCryptoException e) {
            log.error("Error while encrypting the consent : " + e);
            throw new AuthenticationFailedException("Error while encrypting the consent");
        }

        try {
            return generateConsentJWT(jsonObject.toString());
        } catch (UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | KeyStoreException |
                IOException e) {
            log.error("Error while generating the consent : " + e);
            throw new AuthenticationFailedException("Error while generating the consent");
        }
    }


    public SecretKey getSymmetricKeyForConsentEncryption() {

        SecretKey secretKey = null;

        try {
            secretKey = cryptoHandler.getSymmetricKey();
        } catch (ARBCryptoException e) {
            log.error("Error while encrypting the consent : " + e);
        }

        return secretKey;
    }

    /**
     * Returns the consent Details for the particular TPP.
     *
     * @param consentDetails consent details that application received back.
     * @param consentId  consent ID
     * @param clientId client ID
     * @return if the method returns true means the consent details are not been modified, this is to confirm the data
     * integrity
     */

    public boolean validateConsentData(String consentDetails, String clientId, String consentId,
                                          SecretKey secretKey)
            throws AuthenticationFailedException {

        JSONParser parser = new JSONParser();
        JSONObject json;
        try {
            String consentJWTbody = new String(Base64Utils.decode(arbjwtTokenService.getJWTBody(consentDetails)));
            json = (JSONObject) parser.parse(consentJWTbody);

            String consentStringfromJson = (String) json.get("consentvalue");
            byte[] consentCipher = Base64.decode(consentStringfromJson);

            byte[] consentSignature = cryptoHandler.generateHashMessage(consentCipher);

            org.json.JSONArray consentValues = new org.json.JSONArray().put(getConsentDetails(consentId, clientId));

            byte[] consentSignatureOriginal = cryptoHandler.generateHashMessage(cryptoHandler.
                    encryptUsingSymmetricKey(secretKey, consentValues.toString()));

            return Arrays.equals(consentSignatureOriginal, consentSignature);

        } catch (ParseException e) {
            log.error(AlRayanError.ERROR_CONSENT_PARSING.getMessage(), e);
            throw new AuthenticationFailedException(AlRayanError.ERROR_CONSENT_PARSING.getMessage());
        } catch (ARBCryptoException e) {
            throw new AuthenticationFailedException("Exception occured while doing the consent validation");
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Returns true if the signature validation passes.
     *
     * @param consentDetails consent details that application received back.
     * @return map which contains the signature of the consent is verified
     */
    public boolean verifyconsentSignature(AuthenticationContext context, String consentDetails)
            throws AuthenticationFailedException {
        try {
             return  arbSignatureUtils.verifySignature(context, consentDetails);
        } catch (Exception e) {
            throw new AuthenticationFailedException("Exception occured during the signature validation");
        }
    }


    /**
     * Updating the consent details in the DB.
     *
     * @param consentDetails consent details that application received back.
     * @param consentId  consent ID
     * @param username  authenticated user name
     */
    public void updateConsentDetails(String consentDetails, String consentId, String username) {

        JSONParser parser = new JSONParser();
        JSONObject json;
        try {
            String consentJWTbody = new String(Base64Utils.decode(arbjwtTokenService.getJWTBody(consentDetails)));
            json = (JSONObject) parser.parse(consentJWTbody);

            //retrieve scope, accounts and the consent provided for approval
            String scope = new String(Base64.decode((String) json.get("scope")));
            String account = new String(Base64.decode((String) json.get("account")));
            String approvalOfConsent = new String(Base64.decode((String) json.get("approval")));

            updateConsent(consentId, approvalOfConsent, scope, username, account);

        } catch (ParseException e) {

        } catch (AuthenticationFailedException e) {
        } catch (ARBCryptoException e) {
        } catch (Exception e) {
        }
    }

    /**
     * Updating the consent details in the DB.
     *
     *@param consentID  consent ID
     *@param approvalOfConsent if PSU approved the consent
     * @param username  authenticated user name
     * @param account  authenticated user name
     */
    public void updateConsent(String consentID, String approvalOfConsent, String scope, String username,
                              String account) {

        String approval = "Rejected";
        if ("approve".equals(approvalOfConsent) && "accounts".equals(scope)) {
            approval = "Authorised";
        }
        if ("approve".equals(approvalOfConsent) && "payments".equals(scope)) {
            approval = "AcceptedCustomerProfile";
        }

        if ("accounts".equals(scope)) {
            List<String> accounts = Arrays.asList(account.split(" "));
            AdvancedAccountConsent aac = new AdvancedAccountConsent();
            aac.setAccountConsentID(consentID);
            aac.setApproval(approval);
            aac.setCollectionMethod("test");
            aac.setAccountIds(accounts);
            aac.setUserId(username);

            AccountsConsentMgtService accountsConsentMgtService = (AccountsConsentMgtService) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getOSGiService(AccountsConsentMgtService.class, null);
            try {
                accountsConsentMgtService.addAccountConsent(aac);
            } catch (ConsentMgtException e) {
                return;
            }

        } else if ("payments".equals(scope)) {
            AdvancedPaymentConsent apc = new AdvancedPaymentConsent();
            apc.setPaymentId(consentID);
            apc.setApproval(approval);
            apc.setCollectionMethod("test");
            apc.setUserId(username);

            DebtorAccount da = new DebtorAccount();
            da.setSchemeName("SortCodeAccountNumber");
            da.setIdentification(account);
            da.setName("");
            da.setSecondaryIdentification("");
            apc.setDebtorAccount(da);

            PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);
            try {
                paymentsConsentMgtService.addUserConsent(apc);
            } catch (ConsentMgtException e) {
                return;
            }
        }
    }

    protected String generateConsentJWT(String consent) throws UnrecoverableKeyException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException {
        // If JWT signing is enabled, execute the following flow.
        if ("true".equals(AlRayanConfiguration.CONSENT_JWT_SIGNING_ENABLED.getValue())) {
            PrivateKey privateKey = KeyStoreUtils
                    .getPrivateKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());

            return arbjwtTokenService.createJWT(consent, privateKey);
        }
        return arbjwtTokenService.createJWT(consent);
    }

    public String getConsentId(String requestObject, String clientIdFromParam) {

        JSONParser parser = new JSONParser();
        String consentId = null;
        String clientId = null;
        String payload;

        try {
            payload = arbjwtTokenService.getJWTBody(requestObject);

            JSONObject jsonObject = (JSONObject) parser.parse
                    (new String(java.util.Base64.getDecoder().decode(payload)));
            // extract client id
            if (jsonObject.containsKey(CLIENT_ID)) {
                clientId = (String) jsonObject.get(CLIENT_ID);
            }
            // extract consent id
            if (jsonObject.containsKey(CLAIMS)) {
                JSONObject claims = (JSONObject) jsonObject.get(CLAIMS);
                if (claims.containsKey(USERINFO)) {
                    JSONObject userInfo = (JSONObject) claims.get(USERINFO);
                    if (userInfo.containsKey(OPENBANKING_INTENT_ID)) {
                        JSONObject intentObject = (JSONObject) userInfo.get(OPENBANKING_INTENT_ID);
                        if (intentObject.containsKey(VALUE)) {
                            consentId = (String) intentObject.get(VALUE);
                        }
                    }
                }
            }

            if (clientId != null && !clientId.equals(clientIdFromParam)) {
                log.info("Client Id from the request not matches with the client id from the request object");
                throw new AuthenticationFailedException("Client id parameters has been tampered");
            }
            if (StringUtils.isEmpty(consentId)) {
                log.info("Consent details for the client are not found");
                throw new AuthenticationFailedException("Consent details for the client are not found");
            }

        } catch (Exception e) {
        }

        return consentId;
    }

    public String getConsentScope(String requestObject) {

        JSONParser parser = new JSONParser();
        String scope = null;
        String clientId = null;
        String payload;

        try {
            payload = arbjwtTokenService.getJWTBody(requestObject);
            JSONObject jsonObject = (JSONObject) parser.parse
                    (new String(java.util.Base64.getDecoder().decode(payload)));

            // extract client id
            if (jsonObject.containsKey(CLIENT_ID)) {
                clientId = (String) jsonObject.get(CLIENT_ID);
            }
            // extract consent id
            if ((clientId != null) && jsonObject.containsKey(SCOPE)) {
                String scopeValue = (String) jsonObject.get(SCOPE);
                if (scopeValue != null) {
                    List<String> scopes = Arrays.asList(scopeValue.split(" "));
                    if (scopes.contains(ACCOUNTS_SCOPE)) {
                        scope = ACCOUNTS_SCOPE;
                    }
                    if (scopes.contains(PAYMENTS_SCOPE)) {
                        scope = PAYMENTS_SCOPE;
                    }
                }
            }

        } catch (Exception e) {
        }

        return scope;
    }
}
