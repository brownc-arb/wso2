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
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * Model class to hold the PSD2 QC statement.
 */
public class PSD2QCType {

    private final PSPRoles pspRoles;
    private final NcaName nCAName;
    private final NcaId nCAId;

    public PSD2QCType(PSPRoles pspRoles, NcaName nCAName, NcaId nCAId) {

        this.pspRoles = pspRoles;
        this.nCAName = nCAName;
        this.nCAId = nCAId;
    }

    public static PSD2QCType getInstance(ASN1Encodable asn1Encodable) {

        ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Encodable);
        PSPRoles pspRoles = PSPRoles.getInstance(sequence.getObjectAt(0));
        NcaName nCAName = NcaName.getInstance(sequence.getObjectAt(1));
        NcaId nCAId = NcaId.getInstance(sequence.getObjectAt(2));
        return new PSD2QCType(pspRoles, nCAName, nCAId);
    }

    public PSPRoles getPspRoles() {

        return pspRoles;
    }

    public NcaName getnCAName() {

        return nCAName;
    }

    public NcaId getnCAId() {

        return nCAId;
    }
}
