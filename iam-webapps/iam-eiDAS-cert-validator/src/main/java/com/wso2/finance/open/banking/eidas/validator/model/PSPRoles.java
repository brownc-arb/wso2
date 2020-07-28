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

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;

import java.util.Arrays;

/**
 * Model class to hold PSP Roles.
 */
public class PSPRoles {

    private final PSPRole[] roles;

    public PSPRoles(PSPRole... roles) {

        this.roles = roles;
    }

    public static PSPRoles getInstance(Object obj) {

        if (obj instanceof PSPRoles) {
            return (PSPRoles) obj;
        }

        ASN1Encodable[] array = DERSequence.getInstance(obj).toArray();

        PSPRole[] roles = Arrays.stream(array).map(PSPRole::getInstance).toArray(PSPRole[]::new);

        return new PSPRoles(roles);
    }

    public PSPRole[] getRoles() {

        return roles.clone();
    }

}
