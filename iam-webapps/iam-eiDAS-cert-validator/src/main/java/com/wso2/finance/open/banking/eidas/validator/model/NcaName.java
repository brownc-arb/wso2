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

import org.bouncycastle.asn1.DERUTF8String;

/**
 * Model class to hold the NCA name of an eIDAS certificate.
 */
public class NcaName extends DERUTF8String {

    public NcaName(String string) {

        super(string);
    }

    public static NcaName getInstance(Object obj) {

        if (obj instanceof NcaName) {
            return (NcaName) obj;
        }
        return new NcaName(DERUTF8String.getInstance(obj).getString());
    }
}

