package org.wso2.carbon.identity.application.authentication.endpoint.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is responsible for maintaining the request attributes for the payment charges request.
 *
 * @since 1.0.0
 */
@JsonInclude()
public class PaymentChargesRequestInfo {

    @JsonProperty("payer_account_identification")
    private String payerAccountIdentification;
    @JsonProperty("payment_amount")
    private String paymentAmount;
    @JsonProperty("payment_currency")
    private String paymentCurrency;
    @JsonProperty("payee_account_identification")
    private String payeeAccountIdentification;
    @JsonProperty("payee_reference")
    private String payeeReference;
    @JsonProperty("payer_reference")
    private String payerReference;

    /**
     * Creates an instance of {@link PaymentChargesRequestInfo}.
     * <p>
     * This constructor is for the purpose of creating an object using the JSON
     */
    public PaymentChargesRequestInfo() {
    }

    /**
     * Creates an instance of {@link PaymentChargesRequestInfo}.
     *
     * @param payerAccountIdentification payer account identification
     * @param paymentAmount              payment amount
     * @param paymentCurrency            payment currency
     * @param payeeAccountIdentification payment account identification
     * @param payeeReference             payee reference
     * @param payerReference             payer reference
     */
    public PaymentChargesRequestInfo(String payerAccountIdentification, String paymentAmount, String paymentCurrency,
                                     String payeeAccountIdentification, String payeeReference, String payerReference) {
        this.payerAccountIdentification = payerAccountIdentification;
        this.paymentAmount = paymentAmount;
        this.paymentCurrency = paymentCurrency;
        this.payeeAccountIdentification = payeeAccountIdentification;
        this.payeeReference = payeeReference;
        this.payerReference = payerReference;
    }

    /**
     * Returns the payer account identification.
     *
     * @return payer account identification
     */
    public String getPayerAccountIdentification() {
        return payerAccountIdentification;
    }

    /**
     * Sets the payer account identification.
     *
     * @param payerAccountIdentification payer account identification.
     */
    public void setPayerAccountIdentification(String payerAccountIdentification) {
        this.payerAccountIdentification = payerAccountIdentification;
    }

    /**
     * Returns the payment amount.
     *
     * @return payment amount
     */
    public String getPaymentAmount() {
        return paymentAmount;
    }

    /**
     * Returns the payment currency.
     *
     * @return payment currency
     */
    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    /**
     * Returns the payee account identification.
     *
     * @return payee account identification
     */
    public String getPayeeAccountIdentification() {
        return payeeAccountIdentification;
    }

    /**
     * Returns the payee reference.
     *
     * @return payee reference
     */
    public String getPayeeReference() {
        return payeeReference;
    }

    /**
     * Returns the payer reference.
     *
     * @return payer reference
     */
    public String getPayerReference() {
        return payerReference;
    }

    /**
     * Sets the payer reference.
     *
     * @param payerReference payer reference
     */
    public void setPayerReference(String payerReference) {
        this.payerReference = payerReference;
    }

    @Override
    public String toString() {
        return "PaymentChargesRequestInfo{" +
               "payerAccountIdentification='" + payerAccountIdentification + '\'' +
               ", paymentAmount='" + paymentAmount + '\'' +
               ", paymentCurrency='" + paymentCurrency + '\'' +
               ", payeeAccountIdentification='" + payeeAccountIdentification + '\'' +
               ", payeeReference='" + payeeReference + '\'' +
               ", payerReference='" + payerReference + '\'' +
               '}';
    }
}
