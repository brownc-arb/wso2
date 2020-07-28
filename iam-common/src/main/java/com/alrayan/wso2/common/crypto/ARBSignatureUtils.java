package com.alrayan.wso2.common.crypto;

import com.alrayan.wso2.common.jwt.ARBJWTTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.mail.AuthenticationFailedException;

/**
 * Signature handle utils class.
 *
 * @since 1.0.0
 */
public class ARBSignatureUtils implements Serializable {

    private static final long serialVersionUID = -4435665817593413082L;

    ARBJWTTokenService arbjwtTokenService = new ARBJWTTokenService();
    private static Logger log = LoggerFactory.getLogger(ARBSignatureUtils.class);


    /**
     * Returns true if the signature validation passes.
     *
     * @param consentDetails consent details that application received back.
     * @return map which contains the signature of the consent is verified
     */
    public boolean verifySignature(AuthenticationContext context, String consentDetails)
            throws AuthenticationFailedException {

        PublicKey publicKey = null;
        X509Certificate x509Certificate;

        try {
            x509Certificate = (X509Certificate) IdentityApplicationManagementUtil
                    .decodeCertificate(context.getExternalIdP().getIdentityProvider().getCertificate());
            publicKey = x509Certificate.getPublicKey();
            String encodedConsentJWTbody = (arbjwtTokenService.getJWTBody(consentDetails));
            String consentSingnature = (arbjwtTokenService.getJWTSignature(consentDetails));

            return arbjwtTokenService.verifySignature(encodedConsentJWTbody, consentSingnature, publicKey);
        } catch (Exception e) {
            log.error("Exception occurred during the signature validation");
            throw new AuthenticationFailedException("Exception occurred during the signature validation");
        }
    }

}
