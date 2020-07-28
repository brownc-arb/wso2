package com.alrayan.wso2.common.crypto;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.exception.ARBCryptoException;
import com.alrayan.wso2.common.utils.KeyStoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Asymmetric encryption related activities will be handled here.
 *
 * @since 1.0.0
 */
public class ARBAsymmetricKeyCryptoProvider implements Serializable {

    private static final long serialVersionUID = -4456365817593413082L;
    private static Logger log = LoggerFactory.getLogger(ARBAsymmetricKeyCryptoProvider.class);

    /**
     * Encrypt a given plain text.
     *
     * @param plainText The plaintext bytes to be encrypted
     * @param publicKey The key that should be used for the encryption
     * @return The cipher text bytes (self-contained ciphertext)
     * @throws ARBCryptoException On error during encryption
     */
    public byte[] encrypt(byte[] plainText, PublicKey publicKey) throws ARBCryptoException {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(AlRayanConfiguration.ASYMMETRIC_ENCRYPTION_ALGORITHM.getValue());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plainText);
        } catch (Exception e) {
            throw new ARBCryptoException("Error while encrypting the content ", e);
        }
    }

    /**
     * Decrypt the given cipher text value using the WSO2 key.
     *
     * @param ciphertext The cipher text to be decrypted
     * @return Decrypted bytes
     * @throws ARBCryptoException On an error during decryption
     */
    public byte[] decrypt(byte[] ciphertext) throws ARBCryptoException {
        try {
            PrivateKey privateKey = KeyStoreUtils
                    .getPrivateKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());
            Cipher cipher = Cipher.getInstance(AlRayanConfiguration.ASYMMETRIC_ENCRYPTION_ALGORITHM.getValue());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] secretKey = cipher.doFinal(ciphertext);
            return secretKey;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException e) {
            throw new ARBCryptoException("Error while decrypting the content ", e);
        } catch (CertificateException | UnrecoverableKeyException | KeyStoreException | IOException e) {
            throw new ARBCryptoException("Error while obtaining the configured private key.", e);
        }
    }

    public static X509Certificate getServerCert(String alias) {
        X509Certificate x509Certificate = null;

        String type = ServerConfiguration.getInstance().getFirstProperty("Security.TrustStore.Type");
        String password = ServerConfiguration.getInstance().getFirstProperty("Security.TrustStore.Password");
        String storeFile = new File(ServerConfiguration.getInstance().
                getFirstProperty("Security.TrustStore.Location")).getAbsolutePath();


        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(org.wso2.carbon.utils.multitenancy.
                MultitenantConstants.SUPER_TENANT_ID);

        KeyStore keyStore = keyStoreManager.loadKeyStoreFromFileSystem(storeFile,
                password, type);
        try {
            x509Certificate = (X509Certificate) keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            log.error("Error while retrieving the certificate from the client-truststore", e);
        }

        return x509Certificate;
    }
}
