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
import com.wso2.finance.open.banking.eidas.validator.model.RevocationStatus;
import com.wso2.finance.open.banking.eidas.validator.revocation.CRLValidator;
import com.wso2.finance.open.banking.eidas.validator.revocation.OCSPValidator;
import com.wso2.finance.open.banking.eidas.validator.revocation.RevocationValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.cert.X509Certificate;

/**
 * Manager class implementation responsible for validating client certificates.
 */
public class CertRevocationValidationManagerImpl implements CertRevocationValidationManager {

    private static final Log log = LogFactory.getLog(CertRevocationValidationManagerImpl.class);

    @Override
    public boolean verify(X509Certificate peerCertificate, X509Certificate issuerCertificate, int retryCount)
            throws CertificateValidationException {
        // OCSP validation is checked first as it is faster than the CRL validation. Moving to CRL validation
        // only if an error occurs during the OCSP validation.
        RevocationValidator[] validators =
                new RevocationValidator[]{new OCSPValidator(retryCount), new CRLValidator(retryCount)};
        for (RevocationValidator validator : validators) {
            RevocationStatus revocationStatus = isRevoked(validator, peerCertificate, issuerCertificate);
            if (RevocationStatus.GOOD == revocationStatus) {
                return true;
            } else if (RevocationStatus.REVOKED == revocationStatus) {
                return false;
            } else {
                continue;
            }
        }
        log.error("Unable to verify certificate revocation information");
        return false;
    }

    private RevocationStatus isRevoked(RevocationValidator validator, X509Certificate peerCertificate,
                                       X509Certificate issuerCertificate) {

        if (log.isDebugEnabled()) {
            log.debug("X509 Certificate validation with " + validator.getClass().getSimpleName());
        }
        try {
            return validator.checkRevocationStatus(peerCertificate, issuerCertificate);
        } catch (CertificateValidationException e) {
            log.warn("Unable to validate certificate revocation with " +
                    validator.getClass().getSimpleName(), e);
            return RevocationStatus.UNKNOWN;
        }
    }
}
