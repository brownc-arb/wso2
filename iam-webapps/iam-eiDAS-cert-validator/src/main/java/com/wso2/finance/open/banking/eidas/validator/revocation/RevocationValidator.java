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
package com.wso2.finance.open.banking.eidas.validator.revocation;

import com.wso2.finance.open.banking.common.exception.CertificateValidationException;
import com.wso2.finance.open.banking.eidas.validator.model.RevocationStatus;

import java.security.cert.X509Certificate;

/**
 * This interface needs to be implemented by any certificate revocation validator.
 */
public interface RevocationValidator {

    /**
     * Checks revocation status of the peer certificate.
     *
     * @param peerCert   peer certificate
     * @param issuerCert issuer certificate
     * @return revocation status
     * @throws CertificateValidationException when an error occurs while checking the revocation status
     */
    RevocationStatus checkRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateValidationException,
            com.wso2.finance.open.banking.common.exception.CertificateValidationException;

    /**
     * Get revocation validator retry count.
     *
     * @return validator retry count
     */
    int getRetryCount();
}
