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

import com.wso2.finance.open.banking.common.exception.ConsentMgtException;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.PaymentInitiationRequestBody;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.AccountReference;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.AccountConsent;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.AccountConsentIdentifier;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.AccountResource;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.model.PaymentSetupResponse;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.service.AccountsConsentMgtService;
import com.wso2.finance.open.banking.consent.mgt.berlin.v100.mgt.service.PaymentsConsentMgtService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Call Consent Management APIs and retrieve required data to populate consent pages.
 */
public class BerlinAPIDataRetriever extends APIDataRetriever {

    private static Logger logger = LoggerFactory.getLogger(BerlinAPIDataRetriever.class);
    private final static String IS_ERROR = "isError";

    private final static String RECEIVED = "Received";
    private final static String INSTRUCTED_AMOUNT = "Instructed Amount";
    private final static String INSTRUCTED_CURRENCY = "Instructed Currency";
    private final static String DEBTOR_ACCOUNT = "Debtor iban";
    private final static String CREDITOR_NAME = "Creditor Name";
    private final static String CREDITOR_ACCOUNT = "Creditor iban";
    private final static String REMITTANCE_INFORMATION_UNSTRUCTURED = "Remittance Information Unstructured";
    private final static String START_DATE = "Start Date";
    private final static String EXECUTION_RULE = "Execution Rule";
    private final static String FREQUENCY = "Frequency";
    private final static String DAY_OF_EXECUTION = "Day of Execution";
    private final static String PAYMENT_INITIATION_DATA = "paymentInitiationData";

    private String consentId = null;
    private String tppUniqueId = null;

    @Override
    /**
     * Get the data set required for approval in the consent page for AISP flow.
     *
     * @param requestObject request object containing consentID and ClientID
     * @return account data to show in consent page
     */
    public Map<String, Object> getAccountDataSet(String requestObject) {
        return getAccountDataSet(requestObject,"");
    }


    @Override
    /**
     * Get the data set required for approval in the consent page for AISP flow.
     *
     * @param requestObject request object containing consentID and ClientID
     * @param appname appname
     * @return account data to show in consent page
     */
    public Map<String, Object> getAccountDataSet(String requestObject,String appname) {

        Map<String, Object> accountDataSet = new HashMap<>();
        accountDataSet.put(IS_ERROR, "false");

        try {
            AccountConsentIdentifier accountConsentIdentifier = new AccountConsentIdentifier();

            //Extracting consentID and ClientID(tppUniqueID) from request
            List<String> listOfIDs = Arrays.asList(requestObject.split(":"));
            this.tppUniqueId = listOfIDs.get(0);
            this.consentId = listOfIDs.get(2);
            String psuId = listOfIDs.get(3);

            accountConsentIdentifier.setConsentId(consentId);
            accountConsentIdentifier.setTPPUniqueId(tppUniqueId);

            //Initializing AccountsConsentMgtService as an OSGI service
            AccountsConsentMgtService accountsConsentMgtService = (AccountsConsentMgtService)
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().
                            getOSGiService(AccountsConsentMgtService.class,null);

            AccountResource accountResource =  accountsConsentMgtService.getAccountConsent(accountConsentIdentifier);
            AccountConsent accountConsent = accountResource.getAccountConsent();

            if (accountResource.getPSUId() != null) {
                if (!accountResource.getPSUId().equals(psuId)) {
                    logger.info("PSU-ID of the consent does not match with the logged in user");
                    accountDataSet.put(IS_ERROR, "PSU-ID of the consent does not match with the logged in user");
                    return accountDataSet;
                }
            }

            //Content are added separately to reduce the overhead from client side.
            //AccountConsent details added from accountConsent obj
            accountDataSet.put("Accounts", accountConsent.getAccounts());
            accountDataSet.put("Balances", accountConsent.getBalances());
            accountDataSet.put("Transactions", accountConsent.getTransactions());
            accountDataSet.put("Validtill", accountConsent.getValidUntil());
            accountDataSet.put("Frequency", accountConsent.getFrequencyPerDay());
            accountDataSet.put("CombinedService", accountConsent.getCombinedServiceIndicator());
            accountDataSet.put("RecurringIndicator", accountConsent.getRecurringIndicator());

            //Other details from accountResource obj
            accountDataSet.put("TPPUniqueID", accountResource.getTPPUniqueId());
            accountDataSet.put("PSUUniqueID", accountResource.getPSUId());
            accountDataSet.put("LastActionDate", accountResource.getLastActionDate());
            accountDataSet.put("TransactionStatus", accountResource.getTransactionStatus());
            accountDataSet.put("ConsentStatus", accountResource.getConsentStatus());
            accountDataSet.put("Links", accountResource.getLinks());
            accountDataSet.put("AccessCount", accountResource.getAccessCount());
            //Permissions are taken separately as permissions are not currently being set to accountConsent obj
            accountDataSet.put("Permission",accountResource.getPermission());

            if (!validateAccountResource(accountResource, appname)){
                accountDataSet.put(IS_ERROR, "Error while getting account data from request : Account list mismatch");
            }
        } catch (ConsentMgtException e){
            logger.info("Error while getting account data from request", e);
            accountDataSet.put(IS_ERROR, "true");
        }
        return accountDataSet;
    }

    private boolean validateAccountResource(AccountResource accountResource, String appname) {

        //prepare header set for req
        Map<String, String> parameters = new HashMap();
        parameters.put("X-Request-ID", UUID.randomUUID().toString());
        parameters.put("PSU-ID",Base64.encodeBase64String(accountResource.getPSUId().getBytes(Charset.forName("UTF-8"))));

        String sharableAccounts = DebtorAccountRetriever.getSharableAccounts(parameters,appname);
        JSONObject sharableAccountsJson = new JSONObject(sharableAccounts);
        
        // Extract account numbers from resource
        List<AccountReference> accountNumbers = new ArrayList<>();

        accountNumbers.addAll(accountResource.getAccountConsent().getAccounts());
        accountNumbers.addAll(accountResource.getAccountConsent().getBalances());
        accountNumbers.addAll(accountResource.getAccountConsent().getTransactions());
        
        // Extract User Account List
        List<String> userAccountList = new ArrayList<>();

        org.json.JSONArray jArray = sharableAccountsJson.getJSONArray("accounts");

        for(int i = 0; i < jArray.length(); i++) {
            JSONObject obj = (JSONObject) jArray.get(i);
            userAccountList.add(obj.getString("iban"));
        }
        /*
         * was this code, but there's a mized uo with JSONObejct somewhere
         * 
        sharableAccountsJson.getJSONArray("accounts").forEach( object -> {
            userAccountList.add(( object).getString("iban"));
        });
        */

        return accountNumbers.stream().allMatch(userAccountList::contains);
    }

    @Override
    /**
     * @param requestObject request object
     * @return payment data to show in consent page
     */
    public Map<String, Object> getPaymentDataSet(String requestObject, String appname) {

        Map<String, Object> paymentDataSet = new HashMap<>();
        paymentDataSet.put(IS_ERROR, "false");

        try {

            List<String> scopeList = Arrays.asList(requestObject.split(":"));
            this.tppUniqueId = scopeList.get(0);
            this.consentId = scopeList.get(2);
            String psuId = scopeList.get(3);

            PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService)
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);

            PaymentSetupResponse paymentSetupResponse = paymentsConsentMgtService.getPaymentConsentRequest(consentId,
                    tppUniqueId);

            JSONArray paymentInitiationData = new JSONArray();

            if (paymentSetupResponse != null && !paymentSetupResponse.isError()) {
                if (!RECEIVED.equals(paymentSetupResponse.getTransactionStatus().toString())) {
                    paymentDataSet.put(IS_ERROR, "Consent is not in Received state");
                    return paymentDataSet;
                }

                if(paymentSetupResponse.getPSUId() != null) {
                    if (!paymentSetupResponse.getPSUId().equals(psuId)) {
                        logger.info("PSU-ID of the consent does not match with the logged in user");
                        paymentDataSet.put(IS_ERROR, "PSU-ID of the consent does not match with the logged in user");
                        return paymentDataSet;
                    }
                }

                List<PaymentInitiationRequestBody> paymentInitiationRequestBodies = paymentSetupResponse.getData();

                for (PaymentInitiationRequestBody paymentInitiationRequestBody: paymentInitiationRequestBodies) {
                    paymentInitiationData.add(INSTRUCTED_AMOUNT + " : " + paymentInitiationRequestBody
                            .getInstructedAmount().getContent());
                    paymentInitiationData.add(INSTRUCTED_CURRENCY + " : " + paymentInitiationRequestBody
                            .getInstructedAmount().getCurrency());
                    paymentInitiationData.add(DEBTOR_ACCOUNT + " : " + paymentInitiationRequestBody.getDebtorAccount
                            ().getIban());
                    paymentInitiationData.add(CREDITOR_NAME + " : " + paymentInitiationRequestBody.getCreditorName());
                    paymentInitiationData.add(CREDITOR_ACCOUNT + " : " + paymentInitiationRequestBody
                            .getCreditorAccount().getIban());

                    if (paymentInitiationRequestBody.getRemittanceInformationUnstructured() != null) {
                        paymentInitiationData.add(REMITTANCE_INFORMATION_UNSTRUCTURED + " : " +
                                paymentInitiationRequestBody.getRemittanceInformationUnstructured());
                    }
                    if (paymentInitiationRequestBody.getStartDate() != null) {
                        paymentInitiationData.add(START_DATE + " : " + paymentInitiationRequestBody.getStartDate());
                    }
                    if (paymentInitiationRequestBody.getExecutionRule() != null) {
                        paymentInitiationData.add(EXECUTION_RULE + " : " + paymentInitiationRequestBody.getExecutionRule());
                    }
                    if (paymentInitiationRequestBody.getDayOfExecution() != null) {
                        paymentInitiationData.add(DAY_OF_EXECUTION + " : " + paymentInitiationRequestBody
                                .getDayOfExecution());
                    }
                    if (paymentInitiationRequestBody.getFrequency() != null) {
                        paymentInitiationData.add(FREQUENCY + " : " + paymentInitiationRequestBody.getFrequency());
                    }
                    paymentInitiationData.add("linebreak");
                    paymentDataSet.put(PAYMENT_INITIATION_DATA, paymentInitiationData.toString());
                }
            } else {
                logger.error("Error while retrieving payment data");
                if (paymentSetupResponse != null) {
                    paymentDataSet.put(IS_ERROR, paymentSetupResponse.getErrorMessage());
                } else {
                    paymentDataSet.put(IS_ERROR, "Error while retrieving payment data");
                }
            }
        } catch (ConsentMgtException e) {
            logger.error("Error while retrieving payment data");
            paymentDataSet.put(IS_ERROR, "Error while retrieving payment data");
        }
        return paymentDataSet;
    }

    @Override
    public Map<String, Object> getPaymentDataSet(String requestObject) {
        return getPaymentDataSet(requestObject,"");
    }

    @Override
    public Map<String, Object> getFundsConfirmationDataSet(String requestObject) {

        return null;
    }
}
