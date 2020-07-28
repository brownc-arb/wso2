/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.identity.application.authentication.endpoint.client;

import com.wso2.finance.open.banking.common.config.CommonConfigParser;
import com.wso2.finance.open.banking.common.config.uk.UKSpecConfigParser;
import com.wso2.finance.open.banking.common.exception.ConsentMgtException;
import com.wso2.finance.open.banking.common.exception.OpenBankingException;
import com.wso2.finance.open.banking.multiple.authorization.mgmt.model.MultipleAuthorizationData;
import com.wso2.finance.open.banking.multiple.authorization.mgmt.util.MultipleAuthorizationStatusEnum;
import com.wso2.finance.open.banking.uk.consent.mgt.model.AccountSetupResponse;
import com.wso2.finance.open.banking.uk.consent.mgt.model.Authorisation;
import com.wso2.finance.open.banking.uk.consent.mgt.model.DebtorAccount;
import com.wso2.finance.open.banking.uk.consent.mgt.model.FundsConfirmationSetupResponse;
import com.wso2.finance.open.banking.uk.consent.mgt.model.Initiation;
import com.wso2.finance.open.banking.uk.consent.mgt.model.PaymentFileResponse;
import com.wso2.finance.open.banking.uk.consent.mgt.model.PaymentSetUpResponse;
import com.wso2.finance.open.banking.uk.consent.mgt.service.AccountsConsentMgtService;
import com.wso2.finance.open.banking.uk.consent.mgt.service.FundsConfirmationConsentMgtService;
import com.wso2.finance.open.banking.uk.consent.mgt.service.PaymentsConsentMgtService;
import com.wso2.finance.open.banking.uk.consent.mgt.util.PermissionsEnum;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.endpoint.client.model.PaymentChargesRequestInfo;
import org.wso2.carbon.identity.application.authentication.endpoint.client.model.PaymentChargesRequestInfoBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Call Consent Management APIs and retrieve required data to populate consent pages.
 */
public class UKAPIDataRetriever extends APIDataRetriever {

    private static Logger logger = LoggerFactory.getLogger(UKAPIDataRetriever.class);
    private final static String CLIENT_ID = "client_id";
    private final static String CONSENT_ID = "consent_id";
    private final static String CLAIMS = "claims";
    private final static String[] CLAIM_FIELDS = new String[]{"userinfo", "id_token"};
    private final static String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    private final static String VALUE = "value";
    private final static String IS_ERROR = "isError";
    private final static String ACCEPTED_TECHNICAL_VALIDATION = "AcceptedTechnicalValidation";
    private final static String INSTRUCTED_AMOUNT = "Instructed Amount";
    private final static String INSTRUCTED_CURRENCY = "Instructed Currency";
    private final static String PERMISSIONS = "Permissions";
    private final static String AWAITING_AUTHORISATION = "AwaitingAuthorisation";
    private final static String AUTHORIZED = "Authorised";
    private final static String PAYMENT_INITIATION_DATA = "paymentInitiationData";
    private final static String EXPIRATION_DATE_TIME = "Expiration Date Time";
    private final static String TRANSACTION_FROM_DATE_TIME = "Transaction From Date Time";
    private final static String TRANSACTION_TO_DATE_TIME = "Transaction To Date Time";
    private final static String DATES = "dates";
    private final static int NUMBER_OF_PARTS_IN_JWS = 3;
    private final static String INSTRUCTION_IDENTIFICATION = "Instruction Identification";
    private final static String END_TO_END_IDENTIFICATION = "End to End Identification";
    private final static String REQUESTED_EXECUTION_DATE_TIME = "Requested Execution Date Time";
    private final static String FREQUENCY = "Frequency";
    private final static String FIRST_PAYMENT_DATE_TIME = "First Payment Date Time";
    private final static String CURRENCY_OF_TRANSFER = "Currency of Transfer";
    private final static String FILE_TYPE = "File Type";
    private final static String FILE_HASH = "File Hash";
    private final static String PAYMENT_TYPE = "Payment Type";
    private final static String MULTI_AUTH_TYPE = "MultiAuthType";
    private final static String MULTI_AUTH_EXPIRY = "MultiAuthExpiry";
    private final static String DEFAULT_MULTIPLE_AUTHORIZATION_TYPE = "Any";
    private final static String EXPOSED_DATA = "exposedData";
    private final static String IDENTIFICATION = "Identification";
    private final static String FUNDS_CONF_EXPIRATION_DATE_TIME = "Expiration Date Time";
    private final static String SCHEME_NAME = "Scheme Name";
    private final static String ACCOUNT_NAME = "Account Name";
    private final static String OPEN_ENDED_AUTHORIZATION = "Open Ended Authorization Requested";
    private final static String SPEC_VERSION = "spec_version";
    private final static String IS_REAUTHORIZATION = "isReauthorization";
    private final static String IS_REAUTH_ACCOUNT_UPDATE_ENABLED = "isReauthAccountUpdateEnabled";
    private final static String REAUTH_SELECTED_ACCOUNT = "reauthSelectedAccount";
    private final static String DEBTOR_ACCOUNT = "debtor_account";
    private final static String CREDITOR_ACCOUNT_ID = "Creditor Account ID";
    private final static String CREDITOR_ACCOUNT_NAME = "Creditor Account Name";
    private final static String CREDITOR_ACCOUNT_SCHEME_NAME = "Creditor Account Scheme Name";
    private final static String REFERENCE = "Reference";
    private final static String NO_OF_PAYEES = "No. Of Payees";
    private final static String AMOUNT = "Amount";
    private final static String PAYMENT_METHOD = "Payment Method";
    private final static String PAYMENT_CHARGES_REQUEST_DATA = "paymentChargesRequestData";

    /*
        Service Provider Metadata property keys
    */
    public static final String APP_NAME_PREFIX = "software_client_name";
    public static final String APP_ORG_NAME_PREFIX = "org_name";
    public static final String APP_ON_BEHALF_OF_PREFIX = "software_on_behalf_of_org";

    /*
        Application Property Attribute Keys.
    */
    public static final String APP_NAME = "app_name";
    public static final String APP_ORG_NAME = "org_name";
    public static final String APP_ON_BEHALF_OF = "software_on_behalf_of_org";


    private String consentId = null;
    private String clientId = null;
    private String userId = null;

    public UKAPIDataRetriever() {

    }

    private Map<String, Object> validateRequestObjectAndExtractDataRequired(String requestObject,
                                                                            Map<String, Object> dataSet) {

        try {

            // validate request object and get the payload
            String requestObjectPayload;
            String[] jwtTokenValues = requestObject.split("\\.");
            if (jwtTokenValues.length == NUMBER_OF_PARTS_IN_JWS) {
                requestObjectPayload = new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]), "UTF-8");

            } else {
                logger.error("request object is not signed JWT");
                dataSet.put(IS_ERROR, "request object is not signed JWT");
                return dataSet;
            }

            // get consent id from the request object
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(requestObjectPayload);

            if (jsonObject.containsKey(CLAIMS)) {
                JSONObject claims = (JSONObject) jsonObject.get(CLAIMS);
                for (String claim : CLAIM_FIELDS) {
                    if (claims.containsKey(claim)) {
                        JSONObject idToken = (JSONObject) claims.get(claim);
                        if (idToken.containsKey(OPENBANKING_INTENT_ID)) {
                            JSONObject intentObject = (JSONObject) idToken.get(OPENBANKING_INTENT_ID);
                            if (intentObject.containsKey(VALUE)) {
                                consentId = (String) intentObject.get(VALUE);
                                break;
                            }
                        }
                    }
                }
            }

            if (consentId == null) {
                logger.error("intent_id not found in request object");
                dataSet.put(IS_ERROR, "intent_id not found in request object");
                return dataSet;
            }
            dataSet.put(CONSENT_ID, consentId);

            // get client id from the request object
            if (jsonObject.containsKey(CLIENT_ID)) {
                clientId = (String) jsonObject.get(CLIENT_ID);
            } else {
                logger.error("client_id not found in request object");
                dataSet.put(IS_ERROR, "client_id not found in request object");
                return dataSet;
            }

            if (clientId == null) {
                logger.error("client_id not found in request object");
                dataSet.put(IS_ERROR, "client_id not found in request object");
                return dataSet;
            }
            dataSet.put(CLIENT_ID, clientId);

        } catch (UnsupportedEncodingException | ParseException e) {
            logger.error("Error while validating and extracting data from the request object : ", e);
            dataSet.put(IS_ERROR, "Error while validating and extracting data from the request object ");
        }

        return dataSet;
    }

    /**
     * Get the data set required for approval in the consent page for AISP flow.
     *
     * @param requestObject request object
     * @return account data to show in consent page
     */
    public Map<String, Object> getAccountDataSet(String requestObject) {

        Map<String, Object> accountDataSet = new HashMap<>();
        accountDataSet.put(IS_ERROR, "false");
        accountDataSet.put(IS_REAUTHORIZATION, false);
        accountDataSet.put(IS_REAUTH_ACCOUNT_UPDATE_ENABLED, false);
        accountDataSet.put(REAUTH_SELECTED_ACCOUNT, new ArrayList<>());

        try {
            // validate request object and extract data required
            accountDataSet = validateRequestObjectAndExtractDataRequired(requestObject, accountDataSet);
            if (!"false".equals(accountDataSet.get(IS_ERROR))) {
                return accountDataSet;
            }

            AccountsConsentMgtService accountsConsentMgtService = (AccountsConsentMgtService) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getOSGiService(
                            AccountsConsentMgtService.class, null);
            AccountSetupResponse accountSetupResponse = accountsConsentMgtService
                    .getAccountConsents(consentId, clientId);

            JSONArray permissionArray = new JSONArray();
            JSONArray datesArray = new JSONArray();

            if (accountSetupResponse != null && !accountSetupResponse.isError()) {
                if (accountSetupResponse.getAccountResponseData() == null) {
                    accountDataSet.put(IS_ERROR, "Consent data not found.");
                    return accountDataSet;
                }

                if (accountSetupResponse.getAccountResponseData().getExpirationDateTime() != null) {
                    String expireTime = accountSetupResponse.getAccountResponseData().getExpirationDateTime();
                    boolean isExpired = validateExpiryDateTime(expireTime);

                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Consent expiry validation result for %s is %b", consentId, isExpired));
                    }

                    if (isExpired) {
                        accountDataSet.put(IS_ERROR, "Consent has expired: " + expireTime);
                        return accountDataSet;
                    }

                    expireTime = accountSetupResponse.getAccountResponseData().getExpirationDateTime();

                    if (expireTime != null) {
                        datesArray.add(EXPIRATION_DATE_TIME + " : " + expireTime);
                    }

                }

                String status = accountSetupResponse.getAccountResponseData().getStatus().toString();
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Status %s retrieved for consent %s", status, consentId));
                }
                if (!AWAITING_AUTHORISATION.equals(status) && !AUTHORIZED.equals(status)) {
                    String errorMessage = "Consent is not in AwaitingAuthorisation or Authorized state";
                    logger.error(errorMessage);
                    accountDataSet.put(IS_ERROR, errorMessage);
                    return accountDataSet;
                }

                if (AUTHORIZED.equals(status)) {
                    accountDataSet.put(IS_REAUTHORIZATION, true);
                    accountDataSet.put(IS_REAUTH_ACCOUNT_UPDATE_ENABLED,
                            CommonConfigParser.getInstance().isConsentReAuthAccountUpdateEnabled());
                    accountDataSet.put(REAUTH_SELECTED_ACCOUNT, accountSetupResponse.getAccountIds());
                }

                List<PermissionsEnum> permissionsEnumList = accountSetupResponse
                        .getAccountResponseData().getPermissions();
                for (PermissionsEnum permission : permissionsEnumList) {
                    permissionArray.add(permission.toString());
                }

                if (permissionArray.size() < 1) {
                    logger.error("No account permissions found");
                    accountDataSet.put(IS_ERROR, "No account permissions found");
                    return accountDataSet;
                }
                accountDataSet.put(PERMISSIONS, permissionArray.toString());

                String transactionFromTime = accountSetupResponse.getAccountResponseData().getTransactionFromDateTime();
                String transactionToTime = accountSetupResponse.getAccountResponseData().getTransactionToDateTime();

                if (transactionFromTime != null) {
                    datesArray.add(TRANSACTION_FROM_DATE_TIME + " : " + transactionFromTime);
                }

                if (transactionToTime != null) {
                    datesArray.add(TRANSACTION_TO_DATE_TIME + " : " + transactionToTime);
                }

                accountDataSet.put(DATES, datesArray.toString());
            }

        } catch (ConsentMgtException e) {
            logger.error("Error while retrieving account consent data : ", e);
            accountDataSet.put(IS_ERROR, "Error while retrieving account consent data");
        }

        return accountDataSet;

    }

    @Override
    public Map<String, Object> getAccountDataSet(String requestObject, String appname) {
        return getPaymentDataSet(requestObject);
    }

    public Map<String, Object> getFundsConfirmationDataSet(String requestObject) {

        Map<String, Object> fundsConfirmationSet = new HashMap<>();
        Map<String, Object> exposedData = new HashMap<>();
        fundsConfirmationSet.put(IS_ERROR, "false");
        fundsConfirmationSet.put(IS_REAUTHORIZATION, false);

        try {
            // validate request object and extract data required
            fundsConfirmationSet = validateRequestObjectAndExtractDataRequired(requestObject, fundsConfirmationSet);
            if (!"false".equals(fundsConfirmationSet.get(IS_ERROR))) {
                return fundsConfirmationSet;
            }
            FundsConfirmationConsentMgtService consentMgtService =
                    (FundsConfirmationConsentMgtService) PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().getOSGiService(FundsConfirmationConsentMgtService.class, null);

            FundsConfirmationSetupResponse confirmationSetupResponse = consentMgtService
                    .getConsentByConsentId(consentId, clientId);

            if (confirmationSetupResponse != null && !confirmationSetupResponse.isError()) {
                if (confirmationSetupResponse.getData() == null) {
                    fundsConfirmationSet.put(IS_ERROR, "Consent data not found.");
                    return fundsConfirmationSet;
                }
                if (confirmationSetupResponse.getData().getExpirationDateTime() != null) {

                    String expireTime = confirmationSetupResponse.getData().getExpirationDateTime();
                    boolean isExpired = validateExpiryDateTime(expireTime);

                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Consent expiry validation result for %s is %b", consentId, isExpired));
                    }

                    if (isExpired) {
                        fundsConfirmationSet.put(IS_ERROR, "Consent has expired: " + expireTime);
                        return fundsConfirmationSet;
                    }
                    exposedData.put(FUNDS_CONF_EXPIRATION_DATE_TIME, expireTime);
                    String status = confirmationSetupResponse.getData().getStatus().toString();
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Status %s retrieved for consent %s", status, consentId));
                    }
                    if (!AWAITING_AUTHORISATION.equals(status) && !AUTHORIZED.equals(status)) {
                        String errorMessage = "Consent is not in AwaitingAuthorisation or Approved state";
                        logger.error(errorMessage);
                        fundsConfirmationSet.put(IS_ERROR, errorMessage);
                        return fundsConfirmationSet;
                    }

                    if (AUTHORIZED.equals(status)) {
                        fundsConfirmationSet.put(IS_REAUTHORIZATION, true);
                    }
                } else {
                    exposedData.put(FUNDS_CONF_EXPIRATION_DATE_TIME, OPEN_ENDED_AUTHORIZATION);
                }
                DebtorAccount debtorAccount = confirmationSetupResponse.getData().getDebtorAccount();
                exposedData.put(IDENTIFICATION, debtorAccount.getIdentification());
                exposedData.put(SCHEME_NAME, debtorAccount.getSchemeName());
                if (debtorAccount.getName() != null) {
                    exposedData.put(ACCOUNT_NAME, debtorAccount.getName());
                }
                fundsConfirmationSet.put(EXPOSED_DATA, exposedData);
            }
        } catch (ConsentMgtException e) {
            logger.error("Error while retrieving account consent data : ", e);
            fundsConfirmationSet.put(IS_ERROR, "Error while retrieving account consent data");
        }
        return fundsConfirmationSet;
    }


    public Map<String, Object> getPaymentDataSet(String requestObject) {

        return getPaymentDataSet(requestObject,"");
    }


    /**
     * Get the data set required for approval in the consent page for PISP flow.
     *
     * @param requestObject request object
     * @return payment data to show in consent page
     */
    public Map<String, Object> getPaymentDataSet(String requestObject,String appName) {

        Map<String, Object> paymentDataSet = new HashMap<>();
        paymentDataSet.put(IS_ERROR, "false");

        try {
            // validate request object and extract data required
            paymentDataSet = validateRequestObjectAndExtractDataRequired(requestObject, paymentDataSet);
            if (!"false".equals(paymentDataSet.get(IS_ERROR))) {
                return paymentDataSet;
            }

            PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);

            PaymentSetUpResponse paymentSetUpResponse = paymentsConsentMgtService
                    .getPaymentConsentRequest(consentId, clientId);

            JSONArray paymentInitiationData = new JSONArray();
            PaymentChargesRequestInfo paymentChargesRequestInfo = null;
            if (paymentSetUpResponse != null && !paymentSetUpResponse.isError()) {
                String responseStatus = paymentSetUpResponse.getData().getStatus().toString();
                String specVersion = paymentSetUpResponse.getCreatedSpecVersion();
                paymentDataSet.put(SPEC_VERSION, specVersion);

                String multiAuthStatus = "";
                if ("UK300".equals(specVersion)) {
                    MultipleAuthorizationData multiAuthData = MultipleAuthenticationDataRetriever
                            .getMultipleAuthorizationObject(consentId);
                    multiAuthStatus = multiAuthData != null ? multiAuthData.getStatus() : "";
                }

                if (!MultipleAuthorizationStatusEnum.AWAITING_FURTHER_AUTHORISATION.toString().equals(multiAuthStatus)) {

                    if (!ACCEPTED_TECHNICAL_VALIDATION.equals(responseStatus) && "UK110".equals(specVersion)) {
                        paymentDataSet.put(IS_ERROR, "Consent is not in AcceptedTechnicalValidation state");
                        return paymentDataSet;
                    } else if (!AWAITING_AUTHORISATION.equals(responseStatus) && "UK300".equals(specVersion)) {
                        paymentDataSet.put(IS_ERROR, "Consent is not in AwaitingAuthorisation state");
                        return paymentDataSet;
                    }
                } else if (MultipleAuthorizationStatusEnum.REJECTED.toString().equals(multiAuthStatus)) {

                    paymentDataSet.put(IS_ERROR, "Multiple Authorization was rejected by another party");
                    return paymentDataSet;
                }

                boolean multipleAuthorizationApplicable = false;

                if (paymentSetUpResponse.getData().getInitiation().getInstructionIdentification() != null &&
                        paymentSetUpResponse.getData().getInitiation().getEndToEndIdentification() != null &&
                        paymentSetUpResponse.getData().getInitiation().getRequestedExecutionDateTime() == null) {
                    //For Payments

                    if (paymentSetUpResponse.getData().getInitiation().getCurrencyOfTransfer() != null) {
                        //For International Payments

                        paymentInitiationData.add(PAYMENT_TYPE + " : International Payments");
                        paymentInitiationData.add(CURRENCY_OF_TRANSFER + " : " + paymentSetUpResponse.getData().getInitiation()
                                .getCurrencyOfTransfer());

                    } else {

                        paymentInitiationData.add(PAYMENT_TYPE + " : Domestic Payments");
                    }

                    paymentInitiationData.add(INSTRUCTION_IDENTIFICATION + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getInstructionIdentification());
                    paymentInitiationData.add(END_TO_END_IDENTIFICATION + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getEndToEndIdentification());
                    paymentInitiationData.add(INSTRUCTED_AMOUNT + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getAmount());
                    paymentInitiationData.add(INSTRUCTED_CURRENCY + " :" + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getCurrency());

                    multipleAuthorizationApplicable = true;

                } else if (paymentSetUpResponse.getData().getInitiation().getInstructionIdentification() != null &&
                        paymentSetUpResponse.getData().getInitiation().getCurrencyOfTransfer() != null &&
                        paymentSetUpResponse.getData().getInitiation().getRequestedExecutionDateTime() != null) {
                    //For International Schedule Payments

                    paymentInitiationData.add(PAYMENT_TYPE + " : International Scheduled Payments");
                    paymentInitiationData.add(PERMISSIONS + " : " + paymentSetUpResponse.getData().getPermission());
                    paymentInitiationData.add(INSTRUCTION_IDENTIFICATION + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getInstructionIdentification());
                    paymentInitiationData.add(CURRENCY_OF_TRANSFER + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getCurrencyOfTransfer());
                    paymentInitiationData.add(REQUESTED_EXECUTION_DATE_TIME + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getRequestedExecutionDateTime());
                    paymentInitiationData.add(INSTRUCTED_AMOUNT + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getAmount());
                    paymentInitiationData.add(INSTRUCTED_CURRENCY + " :" + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getCurrency());

                } else if (paymentSetUpResponse.getData().getInitiation().getRequestedExecutionDateTime() != null
                        && paymentSetUpResponse.getData().getInitiation().getFileHash() == null) {
                    //For Domestic Scheduled Payments

                    paymentInitiationData.add(PAYMENT_TYPE + " : Domestic Scheduled Payments");
                    paymentInitiationData.add(PERMISSIONS + " : " + paymentSetUpResponse.getData().getPermission());
                    paymentInitiationData.add(INSTRUCTION_IDENTIFICATION + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getInstructionIdentification());
                    paymentInitiationData.add(REQUESTED_EXECUTION_DATE_TIME + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getRequestedExecutionDateTime());
                    paymentInitiationData.add(INSTRUCTED_AMOUNT + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getAmount());
                    paymentInitiationData.add(INSTRUCTED_CURRENCY + " :" + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getCurrency());

                    multipleAuthorizationApplicable = true;

                } else if (paymentSetUpResponse.getData().getInitiation().getFrequency() != null &&
                        paymentSetUpResponse.getData().getInitiation().getFirstPaymentDateTime() != null &&
                        paymentSetUpResponse.getData().getInitiation().getCurrencyOfTransfer() == null) {
                    //For Domestic Standing Orders

                    paymentInitiationData.add(PAYMENT_TYPE + " : Domestic Standing Orders");
                    paymentInitiationData.add(PERMISSIONS + " : " + paymentSetUpResponse.getData().getPermission());
                    paymentInitiationData.add(FREQUENCY + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getFrequency());
                    paymentInitiationData.add(FIRST_PAYMENT_DATE_TIME + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getFirstPaymentDateTime());

                    multipleAuthorizationApplicable = true;

                } else if (paymentSetUpResponse.getData().getInitiation().getFrequency() != null &&
                        paymentSetUpResponse.getData().getInitiation().getCurrencyOfTransfer() != null) {
                    //For International Standing Orders

                    paymentInitiationData.add(PAYMENT_TYPE + " : International Standing Orders");
                    paymentInitiationData.add(PERMISSIONS + " : " + paymentSetUpResponse.getData().getPermission());
                    paymentInitiationData.add(FREQUENCY + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getFrequency());
                    paymentInitiationData.add(CURRENCY_OF_TRANSFER + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getCurrencyOfTransfer());
                    paymentInitiationData.add(INSTRUCTED_AMOUNT + " : " + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getAmount());
                    paymentInitiationData.add(INSTRUCTED_CURRENCY + " :" + paymentSetUpResponse.getData().getInitiation()
                            .getInstructedAmount().getCurrency());

                    multipleAuthorizationApplicable = true;

                } else if (paymentSetUpResponse.getData().getInitiation().getFileHash() != null &&
                        paymentSetUpResponse.getData().getInitiation().getFileType() != null) {
                    //For File Payments
                    populateFilePaymentInfo(paymentSetUpResponse, paymentInitiationData);

                } else {
                    paymentInitiationData.add("Error : No payment initiation data found");
                }

                // Multiple Authorization Data Extraction
                if (multipleAuthorizationApplicable && "UK300".equals(paymentSetUpResponse.getCreatedSpecVersion())) {

                    //TODO: Move this logic to Consent Mgt
                    //Create Default Expiry Date
                    int expiryDays = Integer.parseInt(CommonConfigParser.getInstance()
                            .getMultipleAuthorizationExpiryTime());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.DATE, expiryDays);

                    String multipleAuthorizationType = DEFAULT_MULTIPLE_AUTHORIZATION_TYPE;
                    Authorisation authorizationObject = paymentSetUpResponse.getData().getAuthorisation();
                    //Set Authorization attributes if exists
                    if (paymentSetUpResponse.getData().getAuthorisation() != null) {
                        multipleAuthorizationType = authorizationObject.getAuthorisationType();
                        Date parsedExpiryDate;
                        //Parse and validate date format
                        try {
                            String completionDateTime = authorizationObject.getCompletionDateTime();
                            //The completionDateTime should be a future date
                            if (validateExpiryDateTime(completionDateTime)) {
                                paymentDataSet.put(IS_ERROR, "Authorization Completion Date Time exceeded");
                                return paymentDataSet;
                            } else {
                                OffsetDateTime parsedOffsetDateTime = OffsetDateTime.parse(completionDateTime);
                                parsedExpiryDate = Date.from(parsedOffsetDateTime.toInstant());
                            }
                        } catch (ConsentMgtException e) {
                            paymentDataSet.put(IS_ERROR, "Invalid Authorization Date format In Consent");
                            return paymentDataSet;
                        }
                        calendar = Calendar.getInstance();
                        calendar.setTime(parsedExpiryDate);
                    }
                    String multipleAuthorizationExpiryDate = Long.toString(calendar.getTimeInMillis());

                    paymentDataSet.put(MULTI_AUTH_TYPE, multipleAuthorizationType);
                    paymentDataSet.put(MULTI_AUTH_EXPIRY, multipleAuthorizationExpiryDate);
                }

                // Payment charges request data.
                String payerAccountIdentification = null;
                String payerReference = null;

                DebtorAccount debtorAccount = paymentSetUpResponse.getData().getInitiation().getDebtorAccount();

                if (debtorAccount != null) {
                    JSONParser parser = new JSONParser();
                    //check whether the accounts send by the tpp are allowed for payments
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("userID", userId);
                    parameters.put("consentID", consentId);

                    String payableAccounts = "";
                    if(!StringUtils.isEmpty(appName)) {
                        payableAccounts = DebtorAccountRetriever.getPayableAccounts(parameters,appName);
                    }
                    else {
                        logger.error("App name is not been passed with the request");
                    }

                    JSONObject payableAccountsJson = (JSONObject) parser.parse(payableAccounts);
                    JSONArray payableAccountsArray = (JSONArray) payableAccountsJson.get("data");
                    Iterator payableAccountIterator = payableAccountsArray.iterator();
                    boolean hasPermission = false;
                    boolean checkDebtorAccIdentification = UKSpecConfigParser.getInstance()
                            .getDebtorAccountValidationRequired();
                    // sort code account number is sent as "account_id" this needs to be refactored
                    String schemeName = "account_id";
                    String debtorSortCodeAcc = null;
                    //if debtor account validation is disabled, the bank backend will validate the allowed payable
                    // accounts and return the allowed accounts set
                    if (!checkDebtorAccIdentification && payableAccountIterator.hasNext()) {
                        hasPermission = true;
                    } else {
                        //if debtor account validation is enabled, a check is done to see whether the accounts retrieved
                        //include the debtor account sent in initiation
                        if(debtorAccount.getSchemeName().contains("IBAN")){
                            schemeName = "iban";
                        } else if(debtorAccount.getSchemeName().contains("bic")){
                            schemeName = "bic";
                        }
                        while (payableAccountIterator.hasNext()) {
                            JSONObject payableAccount = (JSONObject) payableAccountIterator.next();
                            if (payableAccount.get(schemeName).toString().equals(debtorAccount.getIdentification())) {
                                hasPermission = true;
                                debtorSortCodeAcc = payableAccount.get("account_id").toString();
                                break;
                            }
                        }
                    }
                    if (!hasPermission) {
                        paymentDataSet.put(IS_ERROR, "Invalid account id submitted for debtor account");
                        return paymentDataSet;
                    }
                    paymentDataSet.put(DEBTOR_ACCOUNT, debtorAccount);
                    paymentDataSet.put("debtorSortCodeAcc", debtorSortCodeAcc);

                    //payment charges information is updated
                    payerAccountIdentification = debtorSortCodeAcc;
                    payerReference = debtorAccount.getName();
                }
                String payeeIdentification = null;
                if (paymentSetUpResponse.getData().getInitiation().getCreditorAccount() != null) {
                    if (paymentSetUpResponse.getData().getInitiation().getCreditorAccount().getSchemeName()
                            .contains("SortCodeAccountNumber")){
                        payeeIdentification = paymentSetUpResponse.getData().getInitiation().getCreditorAccount()
                                .getIdentification();
                    } else {
                        payeeIdentification = paymentSetUpResponse.getData().getInitiation().getCreditorAccount()
                                .getIdentification().substring(8);
                    }
                }
                // Fixed null pointer issue for stading orders. SO request body doesnt contain InstructedAmount
                if(paymentSetUpResponse.getPaymentType()!=null && paymentSetUpResponse.getPaymentType().contains("standing-order")){
                    paymentChargesRequestInfo = new PaymentChargesRequestInfoBuilder()
                            .setPayerAccountIdentification(payerAccountIdentification)
                            .setPaymentAmount(paymentSetUpResponse.getData().getInitiation()
                                    .getFirstPaymentAmount().getAmount())
                            .setPaymentCurrency(paymentSetUpResponse.getData()
                                    .getInitiation().getFirstPaymentAmount().getCurrency())
                            .setPayeeAccountIdentification(payeeIdentification)
                            .setPayeeReference(paymentSetUpResponse.getData().getInitiation()
                                    .getCreditorAccount().getName())
                            .setPayerReference(payerReference)
                            .build();
                } if(paymentSetUpResponse.getPaymentType()!=null && paymentSetUpResponse.getPaymentType().
                        contains("file-payment")){
                    // Refer to: https://support.wso2.com/jira/browse/ALRAYANSUB-196
                    paymentChargesRequestInfo = new PaymentChargesRequestInfoBuilder()
                            .setPayerAccountIdentification(payerAccountIdentification)
                            .setPaymentAmount(paymentSetUpResponse.getData().getInitiation().getControlSum().toString())
                            .setPaymentCurrency(paymentSetUpResponse.getData()
                                    .getInitiation().getCurrencyOfTransfer())
                            .setPayeeAccountIdentification(payeeIdentification)
                            .setPayeeReference(null) // multi payees can set in file payments
                            .setPayerReference("File Payment" + payerReference)
                            .build();
                } else {
                    paymentChargesRequestInfo = new PaymentChargesRequestInfoBuilder()
                            .setPayerAccountIdentification(payerAccountIdentification)
                            .setPaymentAmount(paymentSetUpResponse.getData().getInitiation()
                                    .getInstructedAmount().getAmount())
                            .setPaymentCurrency(paymentSetUpResponse.getData()
                                    .getInitiation().getInstructedAmount().getCurrency())
                            .setPayeeAccountIdentification(payeeIdentification)
                            .setPayeeReference(paymentSetUpResponse.getData().getInitiation()
                                    .getCreditorAccount().getName())
                            .setPayerReference(payerReference)
                            .build();
                }

            }

            if (paymentSetUpResponse != null && paymentSetUpResponse.getData().getInitiation().getCreditorAccount() != null) {

                paymentInitiationData.add(CREDITOR_ACCOUNT_ID + " :" + paymentSetUpResponse.getData()
                        .getInitiation().getCreditorAccount().getIdentification());
                paymentInitiationData.add(CREDITOR_ACCOUNT_SCHEME_NAME + " :" + paymentSetUpResponse.getData()
                        .getInitiation().getCreditorAccount().getSchemeName());
                paymentInitiationData.add(CREDITOR_ACCOUNT_NAME + " :" + paymentSetUpResponse.getData()
                        .getInitiation().getCreditorAccount().getIdentification());

            }

            if (paymentInitiationData.size() < 1) {
                logger.error("No payment initiation data found");
                paymentDataSet.put(IS_ERROR, "No payment initiation data found");
                return paymentDataSet;
            }
            paymentDataSet.put(PAYMENT_INITIATION_DATA, paymentInitiationData.toString());
            // Put payment information required for the bank charges API.
            paymentDataSet.put(PAYMENT_CHARGES_REQUEST_DATA, paymentChargesRequestInfo);

        } catch (ConsentMgtException e) {
            logger.error("Error while retrieving payment consent data : " + e);
            paymentDataSet.put(IS_ERROR, "Error while retrieving payment consent data");
        } catch (ParseException e) {
            logger.error("Error while parsing the JSON object for retrieved payable accounts ",e);
        }

        return paymentDataSet;

    }

    /**
     * Check if the expiry date time of the consent has elapsed
     *
     * @param expiryDate The expiry date/time of consent
     * @return boolean result of validation
     */
    private static boolean validateExpiryDateTime(String expiryDate) throws ConsentMgtException {

        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expiryDate);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Parsed OffsetDateTime: %s, current OffsetDateTime: %s", expDate,
                        OffsetDateTime.now()));
            }
            return OffsetDateTime.now().isAfter(expDate);
        } catch (DateTimeParseException e) {
            logger.error("Error occurred while parsing the expiration date.");
            throw new ConsentMgtException("Error occurred while parsing the expiration date.", e);
        }

    }

    /**
     * get the loggedInUser to retrieve the payable accounts
     * */
    public void setUserId(String loggedInUser) {
        this.userId = loggedInUser;
    }

    /**
     * Populate File Payment Details
     * @param paymentSetUpResponse
     * @param paymentInitiationData
     * @throws ConsentMgtException
     */
    private void populateFilePaymentInfo(PaymentSetUpResponse paymentSetUpResponse, JSONArray paymentInitiationData) throws ConsentMgtException {

        Initiation initiation = paymentSetUpResponse.getData().getInitiation();

        String fileReference = initiation.getFileReference();
        String noOfTransactions = initiation.getNumberOfTransactions();
        BigDecimal controlSum = initiation.getControlSum();
        String executionDateTime = initiation.getRequestedExecutionDateTime();
        String localInstrument = initiation.getLocalInstruments();
        String currencyOfTransfer = initiation.getCurrencyOfTransfer();

        PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);

        PaymentFileResponse paymentFileResponse = paymentsConsentMgtService.retrievePaymentsFile(consentId, clientId);

        if (paymentFileResponse.isBulkPayment()) {
            paymentInitiationData.add(PAYMENT_TYPE + " : Bulk File Payment");
        } else {
            paymentInitiationData.add(PAYMENT_TYPE + " : Batch File Payment");
        }

        if (fileReference != null)
            paymentInitiationData.add(REFERENCE + " : " + fileReference);
        if (noOfTransactions != null)
            paymentInitiationData.add(NO_OF_PAYEES + " : " + noOfTransactions);
        if (controlSum != null)
            paymentInitiationData.add(INSTRUCTED_AMOUNT + " : " + controlSum.toString());
        if (executionDateTime != null)
            paymentInitiationData.add(REQUESTED_EXECUTION_DATE_TIME + " : " + executionDateTime);
        if (localInstrument != null)
            paymentInitiationData.add(PAYMENT_METHOD + " : " + localInstrument);
        if (localInstrument != null)
            paymentInitiationData.add(INSTRUCTED_CURRENCY + " : " + currencyOfTransfer);

    }

    /**
     * Retrieve Application Attributes.
     *
     * @param applicationName service provider name.
     * @return attribute key, pair map
     * @throws OpenBankingException thrown when unable to retrieve application attributes.
     */
    public Map<String, String> getApplicationAttributesMap(String applicationName) throws OpenBankingException {

        Map<String, String> attributeMap = new HashMap<>();
        Map<String, String> applicationAttributes = APIDataRetriever
                .getApiDataRetriever()
                .getServiceProviderProperties(applicationName);

        APIDataRetriever.getSingleAttributeByPrefix(applicationAttributes, APP_NAME_PREFIX)
                .ifPresent(appName -> attributeMap.put(APP_NAME, appName));

        APIDataRetriever.getSingleAttributeByPrefix(applicationAttributes, APP_ORG_NAME_PREFIX)
                .ifPresent(orgName -> attributeMap.put(APP_ORG_NAME, orgName));

        APIDataRetriever.getSingleAttributeByPrefix(applicationAttributes, APP_ON_BEHALF_OF_PREFIX)
                .ifPresent(onBehalfOf -> attributeMap.put(APP_ON_BEHALF_OF, onBehalfOf));

        return attributeMap;

    }

}
