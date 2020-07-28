package org.wso2.carbon.identity.application.authentication.endpoint.client.model;

/**
 * Builder class for the {@link PaymentChargesRequestInfo}.
 *
 * @since 1.0.0
 */
public class PaymentChargesRequestInfoBuilder {

    private String payerAccountIdentification;
    private String paymentAmount;
    private String paymentCurrency;
    private String payeeAccountIdentification;
    private String payeeReference;
    private String payerReference;

    /**
     * Sets the payer account identification.
     *
     * @param payerAccountIdentification payer account identification
     * @return this instance
     */
    public PaymentChargesRequestInfoBuilder setPayerAccountIdentification(String payerAccountIdentification) {
        this.payerAccountIdentification = payerAccountIdentification;
        return this;
    }

    /**
     * Sets the payment amount.
     *
     * @param paymentAmount payment amount
     * @return this instance
     */
    public PaymentChargesRequestInfoBuilder setPaymentAmount(String paymentAmount) {
        this.paymentAmount = paymentAmount;
        return this;
    }

    /**
     * Sets the payment currency.
     *
     * @param paymentCurrency payment currency
     * @return this instance
     */
    public PaymentChargesRequestInfoBuilder setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
        return this;
    }

    /**
     * Sets the payee account identification
     *
     * @param payeeAccountIdentification payee account identification
     * @return this instance
     */
    public PaymentChargesRequestInfoBuilder setPayeeAccountIdentification(String payeeAccountIdentification) {
        this.payeeAccountIdentification = payeeAccountIdentification;
        return this;
    }

    /**
     * Sets the payee reference.
     *
     * @param payeeReference payee reference
     * @return this instance
     */
    public PaymentChargesRequestInfoBuilder setPayeeReference(String payeeReference) {
        this.payeeReference = payeeReference;
        return this;
    }

    /**
     * Sets the payer reference.
     *
     * @param payerReference payer reference
     * @return this instance
     */
    public PaymentChargesRequestInfoBuilder setPayerReference(String payerReference) {
        this.payerReference = payerReference;
        return this;
    }

    /**
     * Creates an returns an instance of {@link PaymentChargesRequestInfo} for the values set.
     *
     * @return an instance of {@link PaymentChargesRequestInfo}
     */
    public PaymentChargesRequestInfo build() {
        return new PaymentChargesRequestInfo(payerAccountIdentification, paymentAmount, paymentCurrency,
                payeeAccountIdentification, payeeReference, payerReference);
    }
}
