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

package com.wso2.finance.open.banking.eidas.validator.util;

/**
 * Model class to hold PSD2 constants.
 */
public class PSD2Constants {

    //Role Names on the certificate
    public static final String PSP_AS = "PSP_AS";
    public static final String PSP_PI = "PSP_PI";
    public static final String PSP_AI = "PSP_AI";
    public static final String PSP_IC = "PSP_IC";

    //PSD2 Role OIDs in the certificate
    public static final String PSP_AS_OID = "0.4.0.19495.1.1";
    public static final String PSP_PI_OID = "0.4.0.19495.1.2";
    public static final String PSP_AI_OID = "0.4.0.19495.1.3";
    public static final String PSP_IC_OID = "0.4.0.19495.1.4";

    //PSD2 Role Names
    public static final String ASPSP = "ASPSP";
    public static final String PISP = "PISP";
    public static final String AISP = "AISP";
    public static final String PIISP = "PIISP";
}
