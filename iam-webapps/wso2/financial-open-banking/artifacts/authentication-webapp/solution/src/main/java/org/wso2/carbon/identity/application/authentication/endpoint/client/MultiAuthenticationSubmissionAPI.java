package org.wso2.carbon.identity.application.authentication.endpoint.client;

import com.alrayan.wso2.common.AlRayanConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiAuthenticationSubmissionAPI {

    private static Logger log = LoggerFactory.getLogger(MultiAuthenticationSubmissionAPI.class);

    private static final String IS_ERROR = "isError";
    private static final String GET = "GET";
    private static final String PAYMENT = "payment";
    private static final String SUBMISSIONS = "submissions";
    private static final String STATUS = "status";
    private static final String DATA = "data";
    private static final String USER_ID = "userId";
    private static final String ACCOUNT_ID = "accountId";
    private static final String PAYMENT_TYPE = "paymentType";

    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String ACCEPT_HEADER_NAME = "Accept";
    public static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    public static final String CHAR_SET = "UTF-8";
    public static final String SERVICE_URL_SLASH = "/";
    public static final String USER_ID_KEY_NAME = "X-User-Id";
    public static final String ACCOUNT_ID_KEY_NAME = "Account-ID";
    public static final String ENDPOINT = "Endpoint";
    public static final String SANDBOX = "Sandbox";
    public static final String PRODUCTION = "Production";

    /**
     * Create a new thread to do the payment submission for pending Multi Authorisation request.
     *
     * @param consentId consent ID
     * @param appName   service provider application name
     */
    public static void multiAuthorisationSubmission(String consentId, String appName) {
        new Thread(() -> {
            handleMultiAuthorisationSubmission(consentId, appName);
        }).start();
    }

    /**
     * Handle the multi auth pending payment request
     *
     * @param consentId consent ID
     * @param appName   service provider application name
     */
    private static void handleMultiAuthorisationSubmission(String consentId, String appName) {
        JSONObject json;
        JSONObject obj;
        String pendingStatus;
        String userId;
        String data;
        String accountId;
        String paymentType;

        String response = retrievePendingSubmissionDetails(consentId, appName);
        json = new JSONObject(response);
        obj = (JSONObject) json.getJSONObject(PAYMENT).getJSONArray(SUBMISSIONS).get(0);
        pendingStatus = (String) obj.get(STATUS);
        userId = (String) obj.get(USER_ID);
        data = (String) obj.get(DATA);
        accountId = (String) obj.get(ACCOUNT_ID);
        paymentType = (String) obj.get(PAYMENT_TYPE);

        if ("1".equals(pendingStatus)) {
            createSubmissionCall(appName, data, userId, accountId, paymentType);
        } else {
            return;
        }
    }

    /**
     * Get teh pending payment submission details
     *
     * @param consentId consent ID
     * @param appName   service provider application name
     * @return the pending payments
     */
    private static String retrievePendingSubmissionDetails(String consentId, String appName) {
        String multiAuthPendingSubmissionRetrieveUrl = appName.endsWith("_SANDBOX") ?
                AlRayanConfiguration.SANDBOX_MULTI_AUTH_PENDING_SUBMISSION_SERVICE_ENDPOINT.getValue() :
                AlRayanConfiguration.PRODUCTION_MULTI_AUTH_PENDING_SUBMISSION_SERVICE_ENDPOINT.getValue();

        URL url;
        HttpURLConnection httpURLConnection = null;
        BufferedReader in = null;
        StringBuffer response = null;
        String inputLine;

        try {
            String param = "/?ConsentId=".concat(consentId);
            url = new URL(multiAuthPendingSubmissionRetrieveUrl.concat(param));

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(GET);
            httpURLConnection.setRequestProperty(ACCEPT_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);

            httpURLConnection.connect();
            in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), CHAR_SET));
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            httpURLConnection.disconnect();
        } catch (IOException e) {
            log.error("Exception occurred while retrieving payable accounts", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Error while closing buffered reader");
                }
            }
        }
        return String.valueOf(response);
    }

    /**
     * Does a payment submission call to the payment submission API
     *
     * @param appName           application name
     * @param submissionPayload submission payload
     * @param userId            user ID
     * @param accountId         bank account ID
     * @param paymentType       payment type
     * @return
     */
    private static boolean createSubmissionCall(String appName, String submissionPayload, String userId,
                                                String accountId, String paymentType) {
        String multiAuthPaymentSubmissionUrl = appName.endsWith("_SANDBOX") ?
                AlRayanConfiguration.SANDBOX_BANK_ACCOUNT_SERVICE_ENDPOINT.getValue() :
                AlRayanConfiguration.PRODUCTION_BANK_ACCOUNT_SERVICE_ENDPOINT.getValue();

        String endpoint_value = appName.endsWith("_SANDBOX") ?
                SANDBOX :
                PRODUCTION;

        if (multiAuthPaymentSubmissionUrl.endsWith(SERVICE_URL_SLASH)) {
            // build the payment submission endpoint for the required payment type (eg :- domestic-payment)
            multiAuthPaymentSubmissionUrl = multiAuthPaymentSubmissionUrl + paymentType + SERVICE_URL_SLASH;
        } else {
            multiAuthPaymentSubmissionUrl = multiAuthPaymentSubmissionUrl + SERVICE_URL_SLASH + paymentType
                    + SERVICE_URL_SLASH;
        }
        BufferedReader reader = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(multiAuthPaymentSubmissionUrl);
            httpPost.addHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE);
            httpPost.addHeader(ENDPOINT, endpoint_value);
            httpPost.addHeader(USER_ID_KEY_NAME, userId);
            httpPost.addHeader(ACCOUNT_ID_KEY_NAME, accountId);
            httpPost.setEntity(new StringEntity(submissionPayload));
            HttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                log.error("Submitting multi authorisation payment request failed");
                return false;
            } else {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), CHAR_SET));
                String inputLine;
                StringBuffer buffer = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Submission call returned : " + buffer.toString());
                }
                log.info(buffer.toString());
                return true;
            }
        } catch (IOException e) {
            log.error("Exception occurred while retrieving making multi authorisation payment submission call", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing buffered reader");
                }
            }
        }
        return false;
    }
}
