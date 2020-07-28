/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alrayan.wso2.common.utils;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.exception.StringDecryptionException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * This class is responsible for handling keystore related operations.
 *
 * @since 1.0.0
 */
public class KeyStoreUtils {

    private static final String SYSTEM_PROPERTY_CARBON_HOME = "${carbon.home}";

    /**
     * Returns the public key for the given params.
     * <p>
     * Refer {@link AlRayanConfiguration} configuration for further information. This is where the key store
     * parameters are configured.
     *
     * @param alias        key store alias
     * @param password     key store password
     * @param keyStorePath key store file path - if this value starts with {@value SYSTEM_PROPERTY_CARBON_HOME}, then
     *                     the path relative to carbon home will be taken
     * @return public key for the given key store params
     * @throws KeyStoreException         thrown when error on getting public key
     * @throws UnrecoverableKeyException thrown when error on getting public key
     * @throws NoSuchAlgorithmException  thrown when error on getting public key
     * @throws CertificateException      thrown when error on getting public key
     * @throws IOException               thrown when error on locating the keystore
     */
    public static PublicKey getPublicKey(String alias, char[] password, String keyStorePath)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException,
            CertificateException {
        keyStorePath = keyStorePath.toLowerCase().startsWith(SYSTEM_PROPERTY_CARBON_HOME) ?
                       keyStorePath.replace(SYSTEM_PROPERTY_CARBON_HOME, System.getProperty("carbon.home")) :
                       keyStorePath;
        FileInputStream fileInputStream = new FileInputStream(keyStorePath);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(fileInputStream, password);
        Key key = keyStore.getKey(alias, password);
        if (key instanceof PrivateKey) {
            Certificate cert = keyStore.getCertificate(alias);
            return cert.getPublicKey();
        }
        return (PublicKey) key;
    }

    /**
     * Returns the private key for the given params.
     * <p>
     * Refer {@link AlRayanConfiguration} configuration for further information. This is where the key store
     * parameters are configured.
     *
     * @param alias        key store alias
     * @param password     key store password
     * @param keyStorePath key store file path - if this value starts with {@value SYSTEM_PROPERTY_CARBON_HOME}, then
     *                     the path relative to carbon home will be taken
     * @return private key for the given key store params
     * @throws KeyStoreException         thrown when error on getting public key
     * @throws UnrecoverableKeyException thrown when error on getting public key
     * @throws NoSuchAlgorithmException  thrown when error on getting public key
     * @throws CertificateException      thrown when error on getting public key
     * @throws IOException               thrown when error on locating the keystore
     */
    public static PrivateKey getPrivateKey(String alias, char[] password, String keyStorePath)
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        keyStorePath = keyStorePath.toLowerCase().startsWith(SYSTEM_PROPERTY_CARBON_HOME) ?
                       keyStorePath.replace(SYSTEM_PROPERTY_CARBON_HOME, System.getProperty("carbon.home")) :
                       keyStorePath;
        FileInputStream fileInputStream = new FileInputStream(keyStorePath);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(fileInputStream, password);
        Key key = keyStore.getKey(alias, password);
        return (PrivateKey) key;
    }

    /**
     * Returns the encrypted string for the given public key.
     * <p>
     * Please note to encode once the encryption is done to preserve special characters.
     *
     * @param publicKey       public key
     * @param stringToEncrypt string to encrypt
     * @return encrypted string
     * @throws StringDecryptionException thrown when error on string encryption
     */
    public static byte[] encryptFromPublicKey(PublicKey publicKey, String stringToEncrypt)
            throws StringDecryptionException {
        try {
            Cipher encrypt = Cipher.getInstance("RSA");
            encrypt.init(Cipher.ENCRYPT_MODE, publicKey);
            return encrypt.doFinal(stringToEncrypt.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new StringDecryptionException("RSA algorithm not found for decrypting string.", e);
        } catch (InvalidKeyException e) {
            throw new StringDecryptionException("Invalid key on initialising the public key for encryption.", e);
        } catch (NoSuchPaddingException e) {
            throw new StringDecryptionException("Padding not found for string encryption.", e);
        } catch (BadPaddingException e) {
            throw new StringDecryptionException("Bad padding exception on string encryption.", e);
        } catch (IllegalBlockSizeException e) {
            throw new StringDecryptionException("Illegal block size on string encryption.", e);
        }
    }

    /**
     * Decrypts the given string using the given private key.
     *
     * @param privateKey  private key to be used for decryption
     * @param stringBytes string to be decrypt
     * @return decrypted string
     * @throws StringDecryptionException thrown when error on string decryption
     */
    public static String decryptFromPrivateKey(PrivateKey privateKey, byte[] stringBytes)
            throws StringDecryptionException {
        try {
            Cipher decrypt = Cipher.getInstance("RSA");
            decrypt.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(decrypt.doFinal(stringBytes), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new StringDecryptionException("RSA algorithm not found for decrypting string.", e);
        } catch (InvalidKeyException e) {
            throw new StringDecryptionException("Invalid key on initialising the private key for decryption.", e);
        } catch (NoSuchPaddingException e) {
            throw new StringDecryptionException("Padding not found for decrypting string.", e);
        } catch (BadPaddingException e) {
            throw new StringDecryptionException("Bad padding exception on string decryption.", e);
        } catch (IllegalBlockSizeException e) {
            throw new StringDecryptionException("Illegal block size on string decryption.", e);
        }
    }
}
