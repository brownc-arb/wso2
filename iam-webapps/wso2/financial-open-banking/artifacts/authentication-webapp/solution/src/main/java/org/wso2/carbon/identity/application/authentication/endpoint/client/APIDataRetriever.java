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

import com.wso2.finance.open.banking.common.config.CommonConfigParser;
import com.wso2.finance.open.banking.common.config.uk.UKSpecConfigParser;
import com.wso2.finance.open.banking.common.exception.OpenBankingException;
import com.wso2.finance.open.banking.common.util.CommonConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class APIDataRetriever {

    private static final String IDENTITY_APPLICATION_MANAGEMENT_SERVICE = "IdentityApplicationManagementService";
    private static volatile APIDataRetriever apiDataRetriever = null;
    private static volatile String deployedSpec = null;

    public static APIDataRetriever getApiDataRetriever() {

        if (apiDataRetriever == null) {
            if (CommonConstants.BERLIN_SPEC_NAME.equalsIgnoreCase(CommonConfigParser.getInstance().getDeployedSpecification())) {
                apiDataRetriever = new BerlinAPIDataRetriever();
            } else if (CommonConstants.UK_SPEC_NAME.equalsIgnoreCase(
                    UKSpecConfigParser.getInstance().getDeployedSpecification())) {
                apiDataRetriever = new UKAPIDataRetriever();
            } else if (CommonConstants.STET_SPEC_NAME.equalsIgnoreCase(
                    CommonConfigParser.getInstance().getDeployedSpecification())) {
                apiDataRetriever = new STETAPIDataRetriever();
            }
            return apiDataRetriever;
        } else {
            return apiDataRetriever;
        }
    }

    public static String getDeployedSpec() {

        if (deployedSpec == null) {
            if (CommonConstants.BERLIN_SPEC_NAME.equalsIgnoreCase(CommonConfigParser.getInstance().getDeployedSpecification())) {
                deployedSpec = CommonConstants.BERLIN_SPEC_NAME;
            } else if (CommonConstants.UK_SPEC_NAME.equals(UKSpecConfigParser.getInstance().getDeployedSpecification())) {
                deployedSpec = CommonConstants.UK_SPEC_NAME;
            } else if (CommonConstants.STET_SPEC_NAME.equals(CommonConfigParser.getInstance().getDeployedSpecification())) {
                deployedSpec = CommonConstants.STET_SPEC_NAME;
            }
            return deployedSpec;
        } else {
            return deployedSpec;
        }
    }

    /**
     * Get the data set required for approval in the consent page for AISP flow.
     *
     * @param requestObject request object
     * @return account data to show in consent page
     */
    public abstract Map<String, Object> getAccountDataSet(String requestObject);


    /**
     * Get the data set required for approval in the consent page for AISP flow.
     *
     * @param requestObject request object
     * @param appname appname
     * @return account data to show in consent page
     */
    public abstract Map<String, Object> getAccountDataSet(String requestObject,String appname);


    /**
     * Get the data set required for approval in the consent page for PISP flow.
     *
     * @param requestObject request object
     * @return payment data to show in consent page
     */
    public abstract Map<String, Object> getPaymentDataSet(String requestObject);


    /**
     * Get the data set required for approval in the consent page for PISP flow.
     *
     * @param requestObject request object
     * @param appname applicationName
     * @return payment data to show in consent page
     */
    public abstract Map<String, Object> getPaymentDataSet(String requestObject, String appname);

    /**
     * Get the data set required for approval in the consent page for funds confirmation flow.
     *
     * @param requestObject request object
     * @return funds confirmation data to show in consent page
     */
    public abstract Map<String, Object> getFundsConfirmationDataSet(String requestObject);

    /**
     * Get Service Provider Properties.
     *
     * @param appName name of the application.
     * @return attribute map.
     * @throws OpenBankingException thrown when exception coccus in retrieval.
     * TODO : Migrate this to use Application Info API
     */
    public Map<String, String> getServiceProviderProperties(String appName) throws OpenBankingException {

        Map<String, String> properties = new HashMap<>();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String backendServerURL = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        String appMgtServiceURL = backendServerURL + IDENTITY_APPLICATION_MANAGEMENT_SERVICE;

        //IAM stub creation
        IdentityApplicationManagementServiceStub stub = null;
        try {
            stub = new IdentityApplicationManagementServiceStub(appMgtServiceURL);
        } catch (AxisFault axisFault) {
            throw new OpenBankingException("Unable to create identity application management service stub", axisFault);
        }
        String applicationName = APIUtil.replaceEmailDomain(appName);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setProperty(HTTPConstants.COOKIE_STRING, null);

        //Pass admin credentials to manage session
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        String adminUsername = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String adminPassword =
                String.valueOf(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray());
        auth.setUsername(adminUsername);
        auth.setPassword(String.valueOf(adminPassword));
        auth.setPreemptiveAuthentication(true);
        option.setProperty(HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);

        //Map service provider properties
        ServiceProvider sp = null;
        try {
            sp = stub.getApplication(applicationName);
        } catch (RemoteException e) {
            throw new OpenBankingException("Unable to retrieve service provider from stub", e);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            throw new OpenBankingException("identity application management exception thrown", e);
        }
        ServiceProviderProperty[] serviceProviderProperties = sp.getSpProperties();
        for (ServiceProviderProperty serviceProviderProperty : serviceProviderProperties) {
            properties.put(serviceProviderProperty.getName(), serviceProviderProperty.getValue());

        }
        return properties;
    }

    /**
     * Extract a single attribute from attribute map by attribute prefix.
     *
     * @param attributeMap attribute map.
     * @param prefix       prefix of parameter.
     * @return attribute value.
     */
    public static Optional<String> getSingleAttributeByPrefix(Map<String, String> attributeMap, String prefix) {

        return attributeMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix)).findFirst().map(Map.Entry::getValue);

    }
}
