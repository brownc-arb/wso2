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
package com.wso2.finance.open.banking.eidas.validator.service;

import com.wso2.finance.open.banking.common.exception.CertificateValidationException;

import java.security.cert.X509Certificate;

/**
 * Manager interface to be used for verifying certificate revocation status.
 */
public interface CertRevocationValidationManager {

    /**
     * Verify the status of an X509 certificate.
     *
     * @param peerCertificate   peer certificate
     * @param issuerCertificate issuer certificate
     * @param retryCount        retry count
     * @return true if the peer certificate is valid
     * @throws CertificateValidationException when an error occurs while validating the certificate
     */
    boolean verify(X509Certificate peerCertificate, X509Certificate issuerCertificate, int retryCount)
            throws CertificateValidationException, CertificateValidationException, CertificateValidationException;
}
