package org.wso2.carbon.identity.application.authentication.endpoint.client;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.HTTPClientException;
import com.alrayan.wso2.common.utils.HTTPClientUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.identity.application.authentication.endpoint.client.model.PaymentChargesRequestInfo;
import org.wso2.carbon.identity.application.authentication.endpoint.client.model.PaymentChargesResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for retrieving the bank charges.
 *
 * @since 1.0.0
 */
public class UKBankChargesAPI {

    private static final String IS_ERROR = "isError";
    private static final String AL_RAYAN_ERROR_MESSAGE = "alRayanErrorMessage";

    /**
     * Returns the bank charges information for the given parameters.
     *
     * @param paymentChargesRequestInfo info required to obtain payment charges information
     * @param appName                   service provider application name
     * @return map containing the success / failure status of invoking the bank charges API + the bank charges response
     */
    public static Map<String, Object> getBankCharges(PaymentChargesRequestInfo paymentChargesRequestInfo,
                                                     String appName) {
        final String ENDPOINT = "Endpoint";
        final String PRODUCTION = "Production";
        final String SANDBOX = "Sandbox";

        Map<String, Object> dataset = new HashMap<>();
        try {
            String paymentChargesURL = appName.endsWith("_SANDBOX") ?
                    AlRayanConfiguration.SANDBOX_BANK_CHARGES_ENDPOINT.getValue() :
                    AlRayanConfiguration.OPEN_BANKING_PAYMENT_CHARGES_URL.getValue();

            dataset.put(IS_ERROR, false);
            ObjectMapper mapper = new ObjectMapper();
            String paymentChargesRequestInfoJson = mapper.writeValueAsString(paymentChargesRequestInfo);

            String responseBody = "";
            if (appName.endsWith("_SANDBOX")) {
                HTTPClientUtil.Response response = HTTPClientUtil
                        .post(paymentChargesURL)
                        .setHeader(ENDPOINT, SANDBOX)
                        .setEntity(paymentChargesRequestInfoJson, ContentType.APPLICATION_JSON)
                        .execute();
                responseBody = response.getResponseBody();
            } else {
                HTTPClientUtil.Response response = HTTPClientUtil
                        .post(paymentChargesURL)
                        .setHeader(ENDPOINT, PRODUCTION)
                        .setEntity(paymentChargesRequestInfoJson, ContentType.APPLICATION_JSON)
                        .execute();
                responseBody = response.getResponseBody();
            }

            if (responseBody.contains("Error")){
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
                    dataset.put("BackEndError",jsonResponse.get("Error"));
                    dataset.put("BackEndErrorCode",jsonResponse.get("ErrorCode"));
                    return dataset;
                } catch (ParseException e) {
                    dataset.put(IS_ERROR, true);
                    dataset.put(AL_RAYAN_ERROR_MESSAGE,
                            AlRayanError.ERROR_MAPPING_BANK_CHARGES_RESPONSE_INFO.getErrorMessageWithCode());
                    return dataset;
                }
            }
            else {
                PaymentChargesResponse paymentChargesResponse = getPaymentChargesResponseFromResponse(responseBody);
                dataset.put("paymentChargesResponse", paymentChargesResponse);
                return dataset;
            }
        } catch (HTTPClientException e) {
            dataset.put(IS_ERROR, true);
            dataset.put(AL_RAYAN_ERROR_MESSAGE,
                    AlRayanError.ERROR_INVOKING_BANK_CHARGES_ENDPOINT.getErrorMessageWithCode());
            return dataset;
        } catch (JsonProcessingException e) {
            dataset.put(IS_ERROR, true);
            dataset.put(AL_RAYAN_ERROR_MESSAGE,
                    AlRayanError.ERROR_MAPPING_BANK_CHARGES_REQUEST_INFO.getErrorMessageWithCode());
            return dataset;
        } catch (IOException e) {
            dataset.put(IS_ERROR, true);
            dataset.put(AL_RAYAN_ERROR_MESSAGE,
                    AlRayanError.ERROR_MAPPING_BANK_CHARGES_RESPONSE_INFO.getErrorMessageWithCode());
            return dataset;
        }
    }

    /**
     * Returns an instance of {@link PaymentChargesResponse} from the given response body.
     *
     * @param responseBody HTTP response body
     * @return an instance of {@link PaymentChargesResponse}
     * @throws IOException thrown when error on mapping the response body to an instance of
     *                     {@link PaymentChargesResponse}
     */
    private static PaymentChargesResponse getPaymentChargesResponseFromResponse(String responseBody)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(responseBody, PaymentChargesResponse.class);
    }
}
