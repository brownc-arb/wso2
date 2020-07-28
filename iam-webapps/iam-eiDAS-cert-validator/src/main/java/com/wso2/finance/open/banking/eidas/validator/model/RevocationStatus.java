/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */
package com.wso2.finance.open.banking.eidas.validator.model;

/**
 * This is used to get Revocation Status message.
 */
public enum RevocationStatus {

    GOOD("Good"), UNKNOWN("Unknown"), REVOKED("Revoked"),
    ;
    private String message;

    RevocationStatus(String message) {

        this.message = message;
    }

    /**
     * Get revocation status message.
     *
     * @return status message
     */
    public String getMessage() {

        return message;
    }

}
