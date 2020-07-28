package com.alrayan.wso2.common.utils;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.exception.HTTPClientException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Utility class to handle HTTP client functionality.
 *
 * @since 1.0.0
 */
public class HTTPClientUtil {

    private static CloseableHttpClient httpClient;
    private static org.slf4j.Logger log = LoggerFactory.getLogger(HTTPClientUtil.class);

    static {
        // Pooling configuration.
        int maxConnections = Integer.parseInt(AlRayanConfiguration.HTTP_CLIENT_MAX_CONNECTIONS.getValue());
        int maxConnectionsPerRoute = Integer
                .parseInt(AlRayanConfiguration.HTTP_CLIENT_MAX_CONNECTIONS_PER_ROUTE.getValue());
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        // Request configuration.
        int requestTimeout = Integer.parseInt(AlRayanConfiguration.HTTP_CLIENT_REQUEST_TIME_OUT_SECONDS.getValue());
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(requestTimeout * 1000)
                .setConnectionRequestTimeout(requestTimeout * 1000)
                .setSocketTimeout(requestTimeout * 1000)
                .build();

        // SSL configuration.
        X509HostnameVerifier x509HostnameVerifier = new AllowAllHostnameVerifier();
        try {
            SSLConnectionSocketFactory sslConnectionSocketFactory = getSSLConnectionSocketFactory();
            httpClient = HttpClients.custom().setHostnameVerifier(x509HostnameVerifier)
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } catch (HTTPClientException e) {
            log.error("Error on initialising the HTTP client.", e);
        }
    }

    /**
     * Constructs an instance of {@link Post}.
     *
     * @param endpoint HTTP endpoint
     * @return instance of {@link Post}
     */
    public static Post post(String endpoint) {
        return new Post(endpoint);
    }

    /**
     * Constructs an instance of {@link Get}.
     *
     * @param endpoint HTTP endpoint
     * @return instance of {@link Get}
     */
    public static Get get(String endpoint) {
        return new Get(endpoint);
    }

    /**
     * Constructs an instance of {@link Put}.
     *
     * @param endpoint HTTP endpoint
     * @return instance of {@link Put}
     */
    public static Put put(String endpoint) {
        return new Put(endpoint);
    }

    /**
     * Executes the HTTP URI request.
     *
     * @param httpUriRequest HTTP URI request
     * @return {@link HttpResponse} for the executed request
     * @throws HTTPClientException thrown when error on executing the POST request
     */
    private static Response execute(HttpUriRequest httpUriRequest) throws HTTPClientException {
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpUriRequest)) {

            return new ResponseBuilder()
                    .setStatusCode(httpResponse.getStatusLine().getStatusCode())
                    .setResponseBody(getResponseBody(httpResponse))
                    .build();
        } catch (IOException e) {
            log.error("IO exception when posting to URL ", e);
            throw new HTTPClientException("IO exception when posting to URL ", e);
        }
    }

    /**
     * Returns the response body from the response.
     *
     * @param httpResponse HTTP response
     * @return HTTP response body
     * @throws IOException thrown when error on getting HTTP response body
     */
    private static String getResponseBody(HttpResponse httpResponse) throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        return entity != null ? EntityUtils.toString(entity) : null;
    }

    /**
     * Creates and returns a new SSL connection socket factory.
     *
     * @return SSL connection socket factory
     * @throws HTTPClientException thrown when error on creating a SSL connection socket factory
     */
    private static SSLConnectionSocketFactory getSSLConnectionSocketFactory() throws HTTPClientException {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            return new SSLConnectionSocketFactory(builder.build());
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("Error on initialising the HTTP client", e);
            throw new HTTPClientException("Error on initialising the HTTP client", e);
        }
    }

    /**
     * Maintains the response information from HTTP client execution.
     *
     * @since 1.1.0
     */
    public static class Response {
        private int statusCode;
        private String responseBody;

        /**
         * Avoids creating a {@link Response} instance from outside.
         */
        private Response() {
        }

        /**
         * Returns the HTTP status code.
         *
         * @return HTTP status code
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * Returns the HTTP response body.
         *
         * @return HTTP response body
         */
        public String getResponseBody() {
            return responseBody;
        }
    }

    /**
     * This class is responsible for building a {@link Response}.
     *
     * @since 1.1.0
     */
    private static class ResponseBuilder {

        private Response response;

        /**
         * Creates an instance of {@link ResponseBuilder}.
         * <p>
         * This will initiate the response object as well
         */
        private ResponseBuilder() {
            response = new Response();
        }

        /**
         * Sets the HTTP status code.
         *
         * @param statusCode HTTP status code
         * @return this {@link ResponseBuilder} instance
         */
        private ResponseBuilder setStatusCode(int statusCode) {
            response.statusCode = statusCode;
            return this;
        }

        /**
         * Sets the HTTP response body.
         *
         * @param responseBody HTTP response body
         * @return this {@link ResponseBuilder} instance
         */
        private ResponseBuilder setResponseBody(String responseBody) {
            response.responseBody = responseBody;
            return this;
        }

        /**
         * Builds the response object.
         *
         * @return {@link Response} object build by the builder
         */
        private Response build() {
            return response;
        }
    }

    /**
     * This class is responsible for constructing a HTTP POST request and executing the POST request.
     *
     * @since 1.0.0
     */
    public static class Post {

        private HttpPost httpPost;

        /**
         * Constructs an instance of {@link Post}.
         *
         * @param endpoint endpoint
         */
        private Post(String endpoint) {
            httpPost = new HttpPost(endpoint);
        }

        /**
         * Add header to the post request.
         *
         * @param name  header name
         * @param value header value
         * @return this {@link Post} instance
         */
        public Post setHeader(String name, String value) {
            httpPost.setHeader(name, value);
            return this;
        }

        /**
         * Add form parameters to the HTTP post request.
         *
         * @param formParameters form parameters
         * @return this {@link Post} instance
         * @throws HTTPClientException thrown when error on setting form parameters
         */
        public Post setFormParameters(Map<String, String> formParameters) throws HTTPClientException {
            try {
                List<BasicNameValuePair> formParams = new ArrayList<>();
                formParameters.forEach((key, value) -> formParams.add(new BasicNameValuePair(key, value)));
                httpPost.setEntity(new UrlEncodedFormEntity(formParams));
                return this;
            } catch (UnsupportedEncodingException e) {
                throw new HTTPClientException("Character encoding not supported when setting the form parameters", e);
            }
        }

        /**
         * Sets the HTTP entity for the post message.
         *
         * @param entity      HTTP entity
         * @param contentType entity content type
         * @return this {@link Post} instance
         */
        public Post setEntity(String entity, ContentType contentType) {
            HttpEntity httpEntity = new StringEntity(entity, contentType);
            httpPost.setEntity(httpEntity);
            return this;
        }

        /**
         * Executes the HTTP Post request.
         *
         * @return {@link Response} for the executed POST request
         * @throws HTTPClientException thrown when error on executing the POST request
         */
        public Response execute() throws HTTPClientException {
            return HTTPClientUtil.execute(httpPost);
        }
    }

    /**
     * This class is responsible for constructing a HTTP GET request and executing the GET request.
     *
     * @since 1.0.0
     */
    public static class Get {

        private HttpGet httpGet;

        /**
         * Constructs an instance of {@link Get}.
         *
         * @param endpoint endpoint
         */
        private Get(String endpoint) {
            httpGet = new HttpGet(endpoint);
        }

        /**
         * Add header to the get request.
         *
         * @param name  header name
         * @param value header value
         * @return the HTTP Get request instance
         */
        public Get setHeader(String name, String value) {
            httpGet.setHeader(name, value);
            return this;
        }

        /**
         * Adds a query parameter to the HTTP get request.
         *
         * @param key   query parameter key
         * @param value query parameter value
         * @return the HTTP Get request instance
         * @throws HTTPClientException thrown when error on setting query parameters
         */
        public Get addQueryParameter(String key, String value) throws HTTPClientException {
            try {
                URIBuilder uriBuilder = new URIBuilder(httpGet.getURI());
                uriBuilder.addParameter(key, value);
                URI uri = uriBuilder.build();
                httpGet.setURI(uri);
                return this;
            } catch (URISyntaxException e) {
                throw new HTTPClientException("Error occurred when adding query parameters", e);
            }
        }

        /**
         * Executes the HTTP Get request.
         *
         * @return {@link Response} for the executed GET request
         * @throws HTTPClientException thrown when error on executing the GET request
         */
        public Response execute() throws HTTPClientException {
            return HTTPClientUtil.execute(httpGet);
        }
    }

    /**
     * This class is responsible for constructing a HTTP PUT request and executing the PUT request.
     *
     * @since 1.1.0
     */
    public static class Put {

        private HttpPut httpPut;

        /**
         * Constructs an instance of {@link Put}.
         *
         * @param endpoint endpoint
         */
        private Put(String endpoint) {
            httpPut = new HttpPut(endpoint);
        }

        /**
         * Add header to the PUT request.
         *
         * @param name  header name
         * @param value header value
         * @return the HTTP PUT request instance
         */
        public Put setHeader(String name, String value) {
            httpPut.setHeader(name, value);
            return this;
        }

        /**
         * Executes the HTTP PUT request.
         *
         * @return {@link Response} for the executed PUT request
         * @throws HTTPClientException thrown when error on executing the PUT request
         */
        public Response execute() throws HTTPClientException {
            return HTTPClientUtil.execute(httpPut);
        }
    }
}
