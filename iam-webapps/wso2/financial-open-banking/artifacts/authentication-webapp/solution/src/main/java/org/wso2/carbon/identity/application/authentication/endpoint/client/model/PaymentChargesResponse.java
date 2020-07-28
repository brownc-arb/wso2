package org.wso2.carbon.identity.application.authentication.endpoint.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is responsible for maintaining the response attributes for the payment charges response.
 *
 * @since 1.0.0
 */
@JsonInclude()
public class PaymentChargesResponse {

    @JsonProperty("payment_charges")
    private String paymentCharges;

    @JsonProperty("payment_currency")
    private String paymentCurrency;

    @JsonProperty("payment_exchange_rate")
    private String paymentExchangeRate;

    /**
     * Returns the payment charges.
     *
     * @return payment charges
     */
    public String getPaymentCharges() {
        return paymentCharges;
    }

    /**
     * Sets the payment charges.
     *
     * @param paymentCharges payment charges
     */
    public void setPaymentCharges(String paymentCharges) {
        this.paymentCharges = paymentCharges;
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
     * Sets the payment currency.
     *
     * @param paymentCurrency payment currency
     */
    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    /**
     * Returns the payment exchange rate.
     *
     * @return payment exchange rate
     */
    public String getPaymentExchangeRate() {
        return paymentExchangeRate;
    }

    /**
     * Sets the payment exchange rate.
     *
     * @param paymentExchangeRate payment exchange rate
     */
    public void setPaymentExchangeRate(String paymentExchangeRate) {
        this.paymentExchangeRate = paymentExchangeRate;
    }
}
