/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.client;

import com.wso2.finance.open.banking.common.exception.ConsentMgtException;
import com.wso2.finance.open.banking.consent.mgt.stet.v140.mgt.model.PaymentRequestBodyCreditTransferTransaction;
import com.wso2.finance.open.banking.consent.mgt.stet.v140.mgt.model.PaymentSetupResponse;
import com.wso2.finance.open.banking.consent.mgt.stet.v140.mgt.service.PaymentsConsentMgtService;
import com.wso2.finance.open.banking.consent.mgt.stet.v140.mgt.service.PaymentsConsentMgtServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Call Consent Management STET APIs and retrieve required data to populate consent pages.
 */
public class STETAPIDataRetriever extends APIDataRetriever {

    private static Log log = LogFactory.getLog(PaymentsConsentMgtServiceImpl.class);
    private final static String IS_ERROR = "isError";

    private final static String RECEIVED = "Received";
    private final static String PENDING = "Pending";
    private final static String INSTRUCTED_AMOUNT = "Instructed Amount";
    private final static String INSTRUCTED_CURRENCY = "Instructed Currency";
    private final static String DEBTOR_ACCOUNT = "Debtor iban";
    private final static String CREDITOR_NAME = "Creditor Name";
    private final static String CREDITOR_ACCOUNT = "Creditor iban";
    private final static String EXECUTION_RULE = "Execution Rule";
    private final static String FREQUENCY = "Frequency";
    private final static String REQUESTED_EXECUTION_DATE = "Requested day of Execution";
    private final static String PAYMENT_INITIATION_DATA = "paymentInitiationData";

    private String consentId = null;
    private String clientId = null;

    @Override
    /**
     * Get the data set required for approval in the consent page for AISP flow.
     *
     * @param requestObject request object containing consentID and ClientID
     * @return account data to show in consent page
     */
    public Map<String, Object> getAccountDataSet(String requestObject) {
        //No details needed to be sent for the consent page for accounts
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getAccountDataSet(String requestObject, String appname) {
        return getAccountDataSet(requestObject);
    }

    @Override
    /**
     * Get the data set required for approval in the consent page for PISP flow.
     *
     * @param requestObject request object
     * @return payment data to show in consent page
     */
    public Map<String, Object> getPaymentDataSet(String requestObject,String appname) {

        Map<String, Object> paymentDataSet = new HashMap<>();
        paymentDataSet.put(IS_ERROR, "false");

        try {

            List<String> scopeList = Arrays.asList(requestObject.split(":"));

            this.clientId = scopeList.get(0);
            this.consentId = scopeList.get(2);

            PaymentsConsentMgtService paymentsConsentMgtService = (PaymentsConsentMgtService)
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(PaymentsConsentMgtService.class, null);

            PaymentSetupResponse paymentRequestResponse = paymentsConsentMgtService
                    .getPaymentRequest(consentId, clientId);

            JSONArray paymentInitiationData = new JSONArray();

            if (paymentRequestResponse != null && !paymentRequestResponse.isError()) {

                if (!RECEIVED.equals(paymentRequestResponse.getRequestBody().getPaymentInformationStatus().toString())) {
                    paymentDataSet.put(IS_ERROR, "Consent is not in Received state");
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Consent for the payment request which map to the resource id: '%s'. is not in Received state", consentId));
                    }
                    return paymentDataSet;
                }

                String debtorAccount = paymentRequestResponse.getRequestBody().getDebtorAccount()
                        .getIban();
                String creditorAccount = paymentRequestResponse.getRequestBody().getBeneficiary()
                        .getCreditorAccount().getIban();
                String creditorName = paymentRequestResponse.getRequestBody().getBeneficiary()
                        .getCreditor().getName();
                String requestedExecutionDate = paymentRequestResponse.getRequestBody()
                        .getRequestedExecutionDate();

                List<PaymentRequestBodyCreditTransferTransaction> creditTransferTransactions =
                        paymentRequestResponse.getRequestBody().getCreditTransferTransaction();

                for (PaymentRequestBodyCreditTransferTransaction creditTransferTransaction :
                        creditTransferTransactions) {
                    if (PENDING.equals(creditTransferTransaction.getTransactionStatus().toString())) {
                        paymentInitiationData.add(INSTRUCTED_AMOUNT + " : " +
                                creditTransferTransaction.getInstructedAmount().getAmount());
                        paymentInitiationData.add(INSTRUCTED_CURRENCY + " : " + creditTransferTransaction.getInstructedAmount().getCurrency());
                        paymentInitiationData.add(DEBTOR_ACCOUNT + " : " + debtorAccount);

                        if (creditTransferTransaction.getBeneficiary().getCreditorAccount() != null) {
                            creditorAccount = creditTransferTransaction.getBeneficiary()
                                    .getCreditorAccount().getIban();
                        }
                        if (creditTransferTransaction.getBeneficiary().getCreditor() != null) {
                            creditorName = creditTransferTransaction.getBeneficiary().getCreditor()
                                    .getName();
                        }
                        paymentInitiationData.add(CREDITOR_NAME + " : " + creditorName);
                        paymentInitiationData.add(CREDITOR_ACCOUNT + " : " + creditorAccount);
                        if (creditTransferTransaction.getExecutionRule() != null) {
                            paymentInitiationData.add(EXECUTION_RULE + " : " + creditTransferTransaction.getExecutionRule());
                        }

                        if (creditTransferTransaction.getRequestedExecutionDate() != null) {
                            requestedExecutionDate = creditTransferTransaction
                                    .getRequestedExecutionDate();
                        }
                        paymentInitiationData.add(REQUESTED_EXECUTION_DATE + " : " + requestedExecutionDate);
                        if (creditTransferTransaction.getFrequency() != null) {
                            paymentInitiationData.add(FREQUENCY + " : " + creditTransferTransaction.getFrequency());
                        }
                        paymentInitiationData.add("linebreak");
                        paymentDataSet.put(PAYMENT_INITIATION_DATA, paymentInitiationData.toString());
                    }
                }
            } else {
                log.error(String.format("Error while retrieving payment request data for the " +
                        "request with resource id : '%s' and client id : '%s'", consentId, clientId));
                if (paymentRequestResponse != null) {
                    paymentDataSet.put(IS_ERROR, paymentRequestResponse.getErrorMessage());
                } else {
                    paymentDataSet.put(IS_ERROR, String.format("Error while retrieving payment request data for the " +
                            "request with resource id : '%s' and client id : '%s'", consentId, clientId));
                }
            }
        } catch (ConsentMgtException e) {
            log.error("Error while retrieving payment data", e);
            paymentDataSet.put(IS_ERROR, "Error while retrieving payment data");
        }
        if (log.isDebugEnabled()) {
            log.debug("Payment data set retrieved successfully");
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
