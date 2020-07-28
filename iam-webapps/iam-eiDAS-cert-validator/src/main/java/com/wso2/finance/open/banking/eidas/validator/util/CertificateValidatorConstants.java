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
 * Constants required for the CertificateRevocationValidationHandler.
 */
public class CertificateValidatorConstants {

    public static final String TRUSTSTORE_CONF_TYPE_DEFAULT = "JKS";
    public static final String KEYSTORE_CONF_TYPE_DEFAULT = "JKS";

    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_CONTENT_TYPE_OCSP = "application/ocsp-request";
    public static final String HTTP_ACCEPT = "Accept";
    public static final String HTTP_ACCEPT_OCSP = "application/ocsp-response";

    public static final String OB_CACHE_MANAGER = "OB_CERTIFICATE_CACHE";
    public static final String CLIENT_CERT_CACHE = "ClientCertCache";
    public static final String CLIENT_CERT_ISSUER_VALIDATION_CACHE = "ClientCertIssuerValidationCache";

    private CertificateValidatorConstants() {

    }
}
