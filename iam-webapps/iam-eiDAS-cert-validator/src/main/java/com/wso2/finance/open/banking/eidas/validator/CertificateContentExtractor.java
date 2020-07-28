package com.wso2.finance.open.banking.eidas.validator;

import com.wso2.finance.open.banking.common.exception.CertificateValidationException;
import com.wso2.finance.open.banking.eidas.validator.model.CertValidationErrors;
import com.wso2.finance.open.banking.eidas.validator.model.CertificateContent;
import com.wso2.finance.open.banking.eidas.validator.model.PSD2QCType;
import com.wso2.finance.open.banking.eidas.validator.model.PSPRole;
import com.wso2.finance.open.banking.eidas.validator.model.PSPRoles;
import com.wso2.finance.open.banking.eidas.validator.util.PSD2QCStatementUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will be used to extract the eidas certificate content.
 */
public class CertificateContentExtractor {

    private static final Log log = LogFactory.getLog(CertificateContentExtractor.class);

    public static CertificateContent extract(X509Certificate cert) throws CertificateValidationException {

        CertificateContent tppCertData;

        if (cert == null) {
            throw new CertificateValidationException(CertValidationErrors.CERTIFICATE_INVALID.toString());
        }

        PSD2QCType psd2QcType = PSD2QCStatementUtil.getPsd2QCType(cert);
        PSPRoles pspRoles = psd2QcType.getPspRoles();
        PSPRole[] rolesArray = pspRoles.getRoles();

        List<String> roles = new ArrayList<>();
        for (PSPRole pspRole : rolesArray) {
            roles.add(pspRole.getPsd2RoleName());
        }
        try {
            X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
            tppCertData = new CertificateContent(getNameValueFromX500Name(x500name, BCStyle.ORGANIZATION_IDENTIFIER),
                    roles, getNameValueFromX500Name(x500name, BCStyle.CN), psd2QcType.getnCAName().getString(),
                    psd2QcType.getnCAId().getString(), cert.getNotAfter(), cert.getNotBefore());
            if (log.isDebugEnabled()) {
                log.debug("Extracted TPP eIDAS certificate data: " + "[ " + tppCertData.toString() + " ]");
            }
        } catch (CertificateEncodingException e) {
            throw new CertificateValidationException(CertValidationErrors.CERTIFICATE_INVALID.toString(), e);
        }
        return tppCertData;
    }

    private static String getNameValueFromX500Name(X500Name x500Name, ASN1ObjectIdentifier asn1ObjectIdentifier) {

        if (ArrayUtils.contains(x500Name.getAttributeTypes(), asn1ObjectIdentifier)) {
            return IETFUtils.valueToString(x500Name.getRDNs(asn1ObjectIdentifier)[0].getFirst().getValue());
        } else {
            return "";
        }
    }

}
