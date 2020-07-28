package com.alrayan.wso2.common.crypto;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.exception.ARBCryptoException;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;



/**
 * Symmetric encryption related activities will be handled here.
 *
 * @since 1.0.0
 */
public class ARBSymmetricKeyCryptoProvider implements Serializable {

    private static ARBSymmetricKeyCryptoProvider instance = null;
    private static final long serialVersionUID = -944559341308243658L;

    private static SecretKey symmetricKey = null;
    private String symmetricKeyEncryptAlgo;

    private static Logger log = LoggerFactory.getLogger(ARBSymmetricKeyCryptoProvider.class);

    public static synchronized ARBSymmetricKeyCryptoProvider getInstance() {
        if (instance == null) {
            instance = new ARBSymmetricKeyCryptoProvider();
        }
        return instance;
    }

    public SecretKey generateSymmetricKey() throws ARBCryptoException {

        symmetricKeyEncryptAlgo = AlRayanConfiguration.SYMMETRIC_ENCRYPTION_ALGORITHM.getValue();

        if (StringUtils.isEmpty(symmetricKeyEncryptAlgo)) {
            throw new ARBCryptoException("symmetricKey encryption is not enabled or the algorithm is not added");
        }

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(symmetricKeyEncryptAlgo);
            keyGen.init(256);
        } catch (NoSuchAlgorithmException e) {
            log.error("Algorithm is not been configured or server couldn't find the algorithm" , e);
        }

        SecretKey secretKey = keyGen.generateKey();

        return secretKey;
    }

    /**
     * Encrypt a given plain text.
     *
     * @param plainText The plaintext bytes to be encrypted
     * @param secretKey The Symmetric key that should be used for the encryption
     * @return The cipher text bytes (self-contained ciphertext)
     * @throws ARBCryptoException On error during encryption
     */

    public byte[] encrypt(SecretKey secretKey, byte[] plainText) throws ARBCryptoException {
        Cipher c = null;
        byte[] encryptedData = null;

        try {
            c = Cipher.getInstance(AlRayanConfiguration.SYMMETRIC_ENCRYPTION_ALGORITHM.getValue());
            c.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedData = c.doFinal(plainText);
        } catch (Exception e) {
            log.error("Error when encrypting data.", e);
            throw new ARBCryptoException("Error when encrypting data.", e);
        }
        return encryptedData;
    }


    /**
     * Decrypt the given cipher text value using the WSO2 key.
     *
     * @param ciphertext The cipher text to be decrypted
     * @param secretKey The Symmetric key that should be used for the encryption
     * @return Decrypted bytes
     * @throws ARBCryptoException On an error during decryption
     */

    public byte[] decrypt(SecretKey secretKey, byte[] ciphertext) throws ARBCryptoException {
        Cipher c = null;
        byte[] decryptedData = null;
        try {
            c = Cipher.getInstance(AlRayanConfiguration.SYMMETRIC_ENCRYPTION_ALGORITHM.getValue());
            c.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedData = c.doFinal(ciphertext);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException |
                NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("Error when decrypting data.", e);
            throw new ARBCryptoException("Error when decrypting data.", e);
        }
        return decryptedData;
    }
}
