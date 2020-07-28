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

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.wso2.finance.open.banking.common.config.uk.UKSpecConfigParser;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Retrieve Debtor Accounts
 */
public class DebtorAccountRetriever {

    private static Logger log = LoggerFactory.getLogger(APIDataRetriever.class);

    public static final String ACCEPT_HEADER_NAME = "Accept";
    public static final String ACCEPT_HEADER_VALUE = "application/json";
    public static final String GET_METHOD = "GET";
    public static final String CHAR_SET = "UTF-8";
    public static final String SERVICE_URL_SLASH = "/";
    public static final String USER_ID_KEY_NAME = "userID";
    public static final String ENDPOINT = "Endpoint";
    public static final String SANDBOX = "Sandbox";
    public static final String PRODUCTION = "Production";

    public static String getPayableAccounts(Map<String, String> parameters, String appName) {
        String payableAccountsRetrieveUrl = appName.endsWith("_SANDBOX") ?
                AlRayanConfiguration.SANDBOX_PAYABLE_ACCOUNTS_RETRIEVE_ENDPOINT.getValue() :
                UKSpecConfigParser.getInstance().getPayableAccountsRetriveEndpoint();

        String endpoint_value = appName.endsWith("_SANDBOX") ?
                SANDBOX :
                PRODUCTION;
        String retrieveUrl = payableAccountsRetrieveUrl;

        if (!payableAccountsRetrieveUrl.endsWith(SERVICE_URL_SLASH)) {
            retrieveUrl = payableAccountsRetrieveUrl + SERVICE_URL_SLASH;
        } else {
            retrieveUrl = payableAccountsRetrieveUrl;
        }

        retrieveUrl = buildRequestURL(retrieveUrl, parameters);

        if (log.isDebugEnabled()) {
            log.debug("Payable accounts retrieve endpoint : " + retrieveUrl);
        }

        BufferedReader reader = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(retrieveUrl);
            request.addHeader(ACCEPT_HEADER_NAME, ACCEPT_HEADER_VALUE);
            request.addHeader(ENDPOINT, endpoint_value);
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Retrieving payable accounts failed");
                return null;
            } else {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), CHAR_SET));
                String inputLine;
                StringBuffer buffer = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Payable accounts endpoints returned : " + buffer.toString());
                }
                return buffer.toString();
            }
        } catch (IOException e) {
            log.error("Exception occurred while retrieving payable accounts", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing buffered reader");
                }
            }
        }

        return null;
    }

    public static String getSharableAccounts(Map<String, String> parameters, String appName) {

        String sharableAccountsRetriveUrl =
                appName.endsWith("_SANDBOX") ?
                        AlRayanConfiguration.SANDBOX_SHARABLE_ACCOUNTS_RETRIEVE_ENDPOINT.getValue() :
                        UKSpecConfigParser.getInstance().getSharableAccountsRetriveEndpoint();

        String endpoint_value = appName.endsWith("_SANDBOX") ?
                SANDBOX :
                PRODUCTION;

        String retrieveUrl = sharableAccountsRetriveUrl;
        if (!sharableAccountsRetriveUrl.endsWith(SERVICE_URL_SLASH)) {
            retrieveUrl = sharableAccountsRetriveUrl + SERVICE_URL_SLASH;
        } else {
            retrieveUrl = sharableAccountsRetriveUrl;
        }

        retrieveUrl = buildRequestURL(retrieveUrl, parameters);

        if (log.isDebugEnabled()) {
            log.debug("Sharable accounts retrieve endpoint : " + retrieveUrl);
        }

        BufferedReader reader = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(retrieveUrl);
            request.addHeader(ACCEPT_HEADER_NAME, ACCEPT_HEADER_VALUE);
            request.addHeader(ENDPOINT, endpoint_value);

            // add custom headers
            parameters.forEach(request::addHeader);
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Retrieving sharable accounts failed");
                return null;
            } else {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), CHAR_SET));
                String inputLine;
                StringBuffer buffer = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Sharable accounts endpoints returned : " + buffer.toString());
                }
                return buffer.toString();
            }
        } catch (IOException e) {
            log.error("Exception occurred while retrieving sharable accounts", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing buffered reader", e);
                }
            }
        }

        return null;
    }

    /**
     * Build the complete URL with query parameters sent in the map
     *
     * @param baseURL    the base URL
     * @param parameters map of parameters
     * @return the output URL
     */
    private static String buildRequestURL(String baseURL, Map<String, String> parameters) {

        List<NameValuePair> pairs = new ArrayList<>();

        for (Map.Entry<String, String> key : parameters.entrySet()) {
            if (key.getKey() != null && key.getValue() != null) {
                pairs.add(new BasicNameValuePair(key.getKey(), key.getValue()));
            }
        }
        String queries = URLEncodedUtils.format(pairs, "UTF-8");
        return baseURL + "?" + queries;
    }

}
