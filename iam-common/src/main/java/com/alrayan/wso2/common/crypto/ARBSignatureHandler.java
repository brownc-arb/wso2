package com.alrayan.wso2.common.crypto;

import com.alrayan.wso2.common.exception.ARBCryptoException;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
 * JWT builder class.
 *
 * @since 1.0.0
 */
public class ARBSignatureHandler {

    private static final long serialVersionUID = -3400082445536581759L;
    private static Logger log = LoggerFactory.getLogger(ARBSignatureHandler.class);

    private static final String SHA256_WITH_RSA = "SHA256withRSA";
    private String signatureAlgorithm = SHA256_WITH_RSA;
    private static final String NONE = "NONE";

    public byte[] signMessage(String assertion, PrivateKey privateKey) {

        byte[] signedInfo = null;
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(privateKey);

            byte[] dataInBytes = assertion.getBytes(UTF_8);
            signature.update(dataInBytes);
            signedInfo = signature.sign();
        } catch (NoSuchAlgorithmException e) {
            log.error("Signature algorithm " + signatureAlgorithm + " not found.", e);
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException occurred ", e);
        } catch (SignatureException e) {
            log.error("Exception occurred whiling signing the message", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Exception occurred whiling signing the message", e);
        }
        return signedInfo;
    }

    public boolean verifySignature(String plainText, String signature, PublicKey publicKey) throws ARBCryptoException {

        boolean verified = false;
        if (StringUtils.isEmpty(signature)) {
            throw new IllegalArgumentException("The SignedJWT must not be null");
        }

        if (publicKey == null) {
            throw new IllegalArgumentException("The public key must not be null");
        }

        try {
            Signature publicSignature = Signature.getInstance(signatureAlgorithm);
            publicSignature.initVerify(publicKey);
            publicSignature.update(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64Utils.decode(signature);
            verified = publicSignature.verify(signatureBytes);

        } catch (InvalidKeyException e) {
            log.error("Invalid Key has been passed for Signature verification", e);
            throw new ARBCryptoException("Exception occurred during the signature verification");
        } catch (SignatureException e) {
            log.error("Exception occured during the signature verification", e);
            throw new ARBCryptoException("Exception occurred during the signature verification");
        } catch (NoSuchAlgorithmException e) {
            log.error("Configured Algorithm is not been found", e);
            throw new ARBCryptoException("Exception occurred during the signature verification");
        }

        return verified;
    }
}

