package com.alrayan.wso2.common.crypto;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.exception.ARBCryptoException;
import org.apache.axiom.om.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.crypto.SecretKey;

/**
 * Crypto Class to handle all the cryptography related activities in ARBMobile Authenticator.
 *
 * @since 1.0.0
 */
public class ARBCryptoHandler implements Serializable {

    private static Logger log = LoggerFactory.getLogger(ARBCryptoHandler.class);
    ARBSymmetricKeyCryptoProvider arbSymmetricKeyCryptoProvider;
    ARBAsymmetricKeyCryptoProvider arbAsymmetricKeyCryptoProvider;
    private static final long serialVersionUID = -4455365817593413082L;


    public ARBCryptoHandler() {
        arbSymmetricKeyCryptoProvider = ARBSymmetricKeyCryptoProvider.getInstance();
        arbAsymmetricKeyCryptoProvider = new ARBAsymmetricKeyCryptoProvider();
    }

    public byte[] generateHashMessage(byte[] message) {
        byte[] thedigest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            thedigest = md.digest(message);
            return thedigest;
        } catch (NoSuchAlgorithmException e) {
            log.error("The specified algorithm is not found ", e);
        }
        return thedigest;
    }

    public byte[] encryptSymmetricKey(SecretKey secretKey , PublicKey publicKey) throws ARBCryptoException {
        try {
            return arbAsymmetricKeyCryptoProvider.encrypt(secretKey.getEncoded(), publicKey);
        } catch (Exception e) {
            log.error("Error while encrypting the symmetrickey ", e);
            throw new ARBCryptoException("Error while encrypting the symmetrickey ", e);
        }
    }

    public byte[] decryptSymmetricKey(byte[] encryptedKey) throws ARBCryptoException {
        try {
            return arbAsymmetricKeyCryptoProvider.decrypt(encryptedKey);
        } catch (Exception e) {
            log.error("Error while encrypting the symmetrickey  ", e);
            throw new ARBCryptoException("Error while encrypting the symmetrickey ", e);
        }
    }

    // TODO: 11/8/18 need to call VASCO to decrypt the consent
    //    public byte[] decryptConsent(String base64CipherText) throws ARBCryptoException {
    //    }


    public PublicKey generatePublicKeyFromCertString(String encodedCert) throws CertificateException {

        if (encodedCert != null) {
            byte[] bytes = Base64.decode(encodedCert);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(bytes));
            return cert.getPublicKey();
        } else {
            String errorMsg = "Invalid encoded certificate: \'NULL\'";
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public byte[] encryptUsingSymmetricKey(SecretKey secretKey, String plaintext) throws ARBCryptoException {

        try {
            return arbSymmetricKeyCryptoProvider.encrypt(secretKey, plaintext.getBytes());
        } catch (ARBCryptoException e) {
            log.debug("Exception occurred while encrypting with the SymmetricKey", e);
            throw new ARBCryptoException("Exception occurred while encrypting with the SymmetricKey");
        }
    }

    public byte[] decryptUsingSymmetricKey(SecretKey secretKey, byte[] ciphertext) throws ARBCryptoException {

        try {
            return arbSymmetricKeyCryptoProvider.decrypt(secretKey, ciphertext);
        } catch (ARBCryptoException e) {
            log.debug("Exception occurred while decrypting with the SymmetricKey", e);
            throw new ARBCryptoException("Exception occurred while decrypting with the SymmetricKey");
        }
    }

    public SecretKey getSymmetricKey() throws ARBCryptoException {

        SecretKey secretKey = null;

        if (!"True".equals(AlRayanConfiguration.SYMMETRIC_ENCRYPTION_ENABLED.getValue())) {
            log.error("symmetricKey encryption is not enabled");
            throw new ARBCryptoException("symmetricKey encryption is not enabled");
        }

        try {
             secretKey = arbSymmetricKeyCryptoProvider.generateSymmetricKey();
        } catch (ARBCryptoException e) {
            log.debug("Exception occured while generating the SymmetricKey", e);
            throw new ARBCryptoException("Exception occured while generating the SymmetricKey");
        }
        return secretKey;
    }


}
