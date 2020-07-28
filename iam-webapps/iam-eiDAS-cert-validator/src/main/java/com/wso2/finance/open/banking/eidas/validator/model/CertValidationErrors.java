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
 * Enum class to hold the certificate validation errors.
 */
public enum CertValidationErrors {
    CERTIFICATE_INVALID("Content of the certificate is invalid."),
    EXTENSION_NOT_FOUND("X509 V3 Extensions not found in the certificate."),
    QCSTATEMENT_INVALID("Invalid QCStatement in the certificate."),
    QCSTATEMENTS_NOT_FOUND("QCStatements not found in the certificate."),
    PSD2_QCSTATEMENT_NOT_FOUND("No PSD2 QCStatement found in the certificate.");

    private String description;

    CertValidationErrors(String description) {

        this.description = description;
    }

    @Override
    public String toString() {

        return description;
    }

}
