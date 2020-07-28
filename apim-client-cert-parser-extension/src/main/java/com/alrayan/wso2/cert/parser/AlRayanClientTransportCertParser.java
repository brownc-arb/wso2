package com.alrayan.wso2.cert.parser;

import com.wso2.finance.open.banking.common.exception.OpenBankingException;
import com.wso2.finance.open.banking.common.parser.ClientTransportCertParser;

import java.util.Base64;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

/**
 * Custom Client cert parser implementation for parsing the PEM encoded client transport cert from the apache httpd
 * server.
 */
public class AlRayanClientTransportCertParser implements ClientTransportCertParser {

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    @Override
    public X509Certificate parse(String transportCertificate) throws OpenBankingException {

        try {
            if (transportCertificate.contains("null")) {
                // The apache httpd server sends a header with the string value "null" when the client cert is not
                // found (not an MTLS request) in the original request from the client to the apache server.
                return null;
            } else {
                if (transportCertificate.contains(BEGIN_CERT)) {
                    transportCertificate = (transportCertificate.replaceAll(BEGIN_CERT, "")
                            .replaceAll(END_CERT, "").replaceAll("\\s", "")).trim();
                }
                byte[] decoded = Base64.getDecoder().decode(transportCertificate);
                return javax.security.cert.X509Certificate.getInstance(decoded);
            }
        } catch (CertificateException e) {
            throw new OpenBankingException("Error occurred while parsing the client certificate from header", e);
        }
    }
}
