/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.client;

import com.wso2.finance.open.banking.common.exception.OpenBankingException;
import com.wso2.finance.open.banking.multiple.authorization.mgmt.model.MultipleAuthorizationData;
import com.wso2.finance.open.banking.multiple.authorization.mgmt.service.MultipleAuthorizationMgtService;
import com.wso2.finance.open.banking.multiple.authorization.mgmt.util.MultipleAuthorizationUserStatusEnum;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Retriever for Multiple Authorization Management.
 */
public class MultipleAuthenticationDataRetriever {

    public static class Keys {

        public static final String INITIATED = "Initiated";
        public static final String IS_ERROR = "Error";
        public static final String SELECTED_ACCOUNT = "SelectedAccount";
        public static final String MULTI_AUTH_DATA = "MultiAuthData";
    }

    public static Map<String, Object> getMultipleAuthorizationSession(String consentId, String loggedInUser) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(Keys.INITIATED, "false");
        dataMap.put(Keys.IS_ERROR, "false");

        MultipleAuthorizationMgtService mgtService = (MultipleAuthorizationMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(MultipleAuthorizationMgtService.class, null);

        // Check if Multiple Authentication Session is Initiated
        MultipleAuthorizationData authorizationData = null;
        try {
            authorizationData = mgtService.getMultipleAuthorizationByConsentId(consentId);
        } catch (OpenBankingException e) {
            dataMap.put(Keys.INITIATED, "false");
        }

        if (authorizationData != null) {
            dataMap.put(Keys.MULTI_AUTH_DATA, authorizationData);
            dataMap.put(Keys.SELECTED_ACCOUNT, authorizationData.getSelectedAccount());
            // Verify if party is valid for multiple authentication
            if (!authorizationData.getUsers().containsKey(loggedInUser)) {

                dataMap.put(Keys.IS_ERROR, "Not A Valid Authorization Party");
                return dataMap;
            }
            // Verify if the user is not previously authenticated
            if (!MultipleAuthorizationUserStatusEnum.PENDING.toString()
                    .equals(authorizationData.getUsers().get(loggedInUser).getStatus())) {

                dataMap.put(Keys.IS_ERROR, "User already Authenticated");
                return dataMap;
            }
        }

        return dataMap;
    }

    public static MultipleAuthorizationData getMultipleAuthorizationObject(String consentId) {

        MultipleAuthorizationMgtService mgtService = (MultipleAuthorizationMgtService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(MultipleAuthorizationMgtService.class, null);

        try {
           return mgtService.getMultipleAuthorizationByConsentId(consentId);
        } catch (OpenBankingException e) {
            return null;
        }

    }
}
