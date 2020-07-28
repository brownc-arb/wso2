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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.local.auth.api.core.ParameterResolverService;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Retrieve sensitive parameters hidden from redirect URLs.
 */
public class SensitiveDataRetriever {

    private static Logger log = LoggerFactory.getLogger(SensitiveDataRetriever.class);
    private final static String IS_ERROR = "isError";
    private final static String CONSENT_KEY = "OauthConsentKey";
    private final static String REQUEST_KEY = "AuthRequestKey";

    /**
     * Get the sensitive data corresponding to the session data consent key.
     *
     * @param sessionDataKeyConsent The session data key corresponding to the data hidden from redirect URLs
     * @return The hidden sensitive data as key-value pairs.
     */
    public static Map<String, Serializable> getSensitiveDataWithConsentKey(String sessionDataKeyConsent) {
        return getSensitiveData(sessionDataKeyConsent, false);
    }


    /**
     * Get the sensitive data corresponding to the session data key.
     *
     * @param sessionDataKey The session data key corresponding to the data hidden from redirect URLs
     * @return The hidden sensitive data as key-value pairs.
     */
    public static Map<String, Serializable> getSensitiveDataWithSessionKey(String sessionDataKey) {
        return getSensitiveData(sessionDataKey, true);
    }

    /**
     * Get the sensitive data corresponding to the session data key or the session data consent key.
     *
     * @param key The session data key or session data consent key corresponding to the data hidden from redirect URLs
     * @return The hidden sensitive data as key-value pairs.
     */
    public static Map<String, Serializable> getSensitiveData(String key,
                                                             Boolean isWithSessionDataKey) {

        Map<String, Serializable> sensitiveDataSet = new HashMap<>();
        sensitiveDataSet.put(IS_ERROR, "false");

        Object serviceObj = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ParameterResolverService.class, null);
        if (serviceObj instanceof ParameterResolverService) {
            ParameterResolverService resolverService = (ParameterResolverService) serviceObj;

            if (!isWithSessionDataKey && !resolverService.isResolverRegisteredForKey(CONSENT_KEY)) {
                log.error("No resolver registered for OauthConsentKey");
                sensitiveDataSet.put(IS_ERROR, "No resolver registered for OauthConsentKey");
                return sensitiveDataSet;
            }
            else if (isWithSessionDataKey && !resolverService.isResolverRegisteredForKey(REQUEST_KEY)) {
                log.error("No resolver registered for AuthRequestKey");
                sensitiveDataSet.put(IS_ERROR, "No resolver registered for AuthRequestKey");
                return sensitiveDataSet;
            }

            Set<String> filter = Collections.emptySet();

            if (!isWithSessionDataKey) {
                sensitiveDataSet.putAll((resolverService)
                        .resolveParameters(CONSENT_KEY, key, filter));
            } else {
                sensitiveDataSet.putAll((resolverService)
                        .resolveParameters(REQUEST_KEY, key, filter));
            }

            if (sensitiveDataSet.isEmpty()) {
                log.error("No available data for key provided");
                sensitiveDataSet.put(IS_ERROR, "No available data for key provided");
                return sensitiveDataSet;
            }
            return sensitiveDataSet;

        } else {
            log.error("Could not retrieve ParameterResolverService OSGi service");
            sensitiveDataSet.put(IS_ERROR, "Could not retrieve parameter service");
            return sensitiveDataSet;
        }
    }
}
