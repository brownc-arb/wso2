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

import com.wso2.finance.open.banking.common.exception.CertificateValidationException;
import com.wso2.finance.open.banking.eidas.validator.model.CertValidationErrors;
import com.wso2.finance.open.banking.eidas.validator.model.PSD2QCType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.qualified.QCStatement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Optional;

/**
 * Utility class to hold the PSD2 QC statement utility methods.
 */
public class PSD2QCStatementUtil {

    private static final ASN1ObjectIdentifier psd2QcStatementOid = new ASN1ObjectIdentifier("0.4.0.19495.2");
    private static final Log log = LogFactory.getLog(PSD2QCStatementUtil.class);

    /**
     * Get PSD2 QC statement from an PSD2 eIDAS certificate.
     *
     * @param cert PSD2 eIDAS certificate
     * @return PSD2 QC statement of the PSD2 eIDAS certificate
     * @throws CertificateValidationException when aan error occurs while reading the QC statement from the PSD2 eIDAS
     *                                        certificate
     */
    public static PSD2QCType getPsd2QCType(X509Certificate cert) throws CertificateValidationException {

        byte[] extensionValue = cert.getExtensionValue(Extension.qCStatements.getId());  //1.3.6.1.5.5.7.1.3
        if (extensionValue == null) {
            if (log.isDebugEnabled()) {
                log.debug("Extension that contains the QCStatement not found in the certificate");
            }
            throw new CertificateValidationException(CertValidationErrors.EXTENSION_NOT_FOUND.toString());
        }

        QCStatement qcStatement = extractQCStatement(extensionValue);
        ASN1Encodable statementInfo = qcStatement.getStatementInfo();
        return PSD2QCType.getInstance(statementInfo);
    }

    private static QCStatement extractQCStatement(byte[] extensionValue) throws CertificateValidationException {

        ASN1Sequence qcStatements;
        try {
            try (ASN1InputStream derAsn1InputStream = new ASN1InputStream(new ByteArrayInputStream(extensionValue))) {
                DEROctetString oct = (DEROctetString) (derAsn1InputStream.readObject());
                try (ASN1InputStream asn1InputStream = new ASN1InputStream(oct.getOctets())) {
                    qcStatements = (ASN1Sequence) asn1InputStream.readObject();
                }
            }
        } catch (IOException e) {
            throw new CertificateValidationException(CertValidationErrors.QCSTATEMENT_INVALID.toString(), e);
        }

        if (qcStatements.size() <= 0) {
            throw new CertificateValidationException(CertValidationErrors.QCSTATEMENTS_NOT_FOUND.toString());
        }

        ASN1Encodable object = qcStatements.getObjectAt(0);
        if (object.toASN1Primitive() instanceof ASN1ObjectIdentifier) {
            return getSingleQcStatement(qcStatements);
        }

        return extractPsd2QcStatement(qcStatements)
                .orElseThrow(() ->
                        new CertificateValidationException(CertValidationErrors.PSD2_QCSTATEMENT_NOT_FOUND
                                .toString()));
    }

    private static QCStatement getSingleQcStatement(ASN1Sequence qcStatements) throws CertificateValidationException {

        QCStatement qcStatement = QCStatement.getInstance(qcStatements);
        if (!psd2QcStatementOid.getId().equals(qcStatement.getStatementId().getId())) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid QC statement type in psd2 certificate. expected [" +
                        psd2QcStatementOid.getId() + "] but found [" + qcStatement.getStatementId().getId() + "]");
            }
            throw new CertificateValidationException(CertValidationErrors.PSD2_QCSTATEMENT_NOT_FOUND.toString());
        }
        return qcStatement;
    }

    private static Optional<QCStatement> extractPsd2QcStatement(ASN1Sequence qcStatements) {

        Iterator iterator = qcStatements.iterator();
        while (iterator.hasNext()) {
            QCStatement qcStatement = QCStatement.getInstance(iterator.next());
            if (qcStatement != null && qcStatement.getStatementId().getId().equals(psd2QcStatementOid.getId())) {
                return Optional.of(qcStatement);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("PSD2 QC statement not found");
        }
        return Optional.empty();
    }
}
