/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */
package com.wso2.finance.open.banking.eidas.validator.util;

import com.wso2.finance.open.banking.common.config.CommonConfigParser;
import com.wso2.finance.open.banking.common.exception.CertificateValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;

/**
 * Utility class containing certificate validation util methods.
 */
public class CertificateValidationUtil {

    public static final String TRUSTSTORE_LOCATION_CONF_KEY = "Security.ProductionTrustStore.Location";
    public static final String TRUSTSTORE_PASS_CONF_KEY = "Security.ProductionTrustStore.Password";

    private static KeyStore trustStore = null;
    private static boolean clientCertCacheInit = false;
    private static boolean clientCertIssuerValidationCacheInit = false;

    private static final Log log = LogFactory.getLog(CertificateValidationUtil.class);

    private CertificateValidationUtil() {

    }

    /**
     * Get issuer certificate from the truststore.
     *
     * @param peerCertificate peer certificate
     * @return certificate issuer of the peer certificate
     * @throws CertificateValidationException when unable to validate the certificate
     */
    public static X509Certificate getIssuerCertificateFromTruststore(X509Certificate peerCertificate)
            throws CertificateValidationException {

        Enumeration enumeration;
        X509Certificate certificate;
        KeyStore loadedTrustStore = getTrustStore();
        if (loadedTrustStore == null) {
            throw new CertificateValidationException("Client truststore has not been initialized");
        }
        try {
            // Get aliases of all the certificates in the truststore.
            enumeration = loadedTrustStore.aliases();
        } catch (KeyStoreException e) {
            throw new CertificateValidationException("Error while retrieving aliases from keystore ", e);
        }

        // As there is no any specific way to query the issuer certificate from the truststore, public keys of all the
        // certificates in the truststore are validated against the signature of the peer certificate to identify the
        // issuer.
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String alias = null;
                try {
                    alias = (String) enumeration.nextElement();
                    certificate = (X509Certificate) loadedTrustStore.getCertificate(alias);
                } catch (KeyStoreException e) {
                    throw new CertificateValidationException("Unable to read the certificate from truststore with " +
                            "the alias: " + alias, e);
                }
                try {
                    peerCertificate.verify(certificate.getPublicKey());
                    if (log.isDebugEnabled()) {
                        log.debug("Valid issuer certificate found in the client truststore");
                    }
                    return certificate;
                } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException |
                        NoSuchProviderException | SignatureException e) {
                    // Unable to verify the signature. Check with the next certificate.
                    continue;
                }
            }
        } else {
            throw new CertificateValidationException("Unable to read the certificate aliases from the truststore");
        }
        throw new CertificateValidationException("Unable to find the immediate issuer from the truststore of the " +
                "certificate with the serial number " + peerCertificate.getSerialNumber() + " issued by the CA " +
                peerCertificate.getIssuerDN().toString());
    }

    /**
     * Get the truststore. This methods needs to be synchronized with the loadTrustStore() method
     *
     * @return instance of the truststore
     */
    public static synchronized KeyStore getTrustStore() {

        return trustStore;
    }

    /**
     * Check the expiry time of a certificate.
     *
     * @param x509Certificate certificate
     * @return true if the certificate is expired
     */
    public static boolean isExpired(X509Certificate x509Certificate) {

        try {
            x509Certificate.checkValidity();
        } catch (CertificateException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            return true;
        }
        return false;
    }

    /**
     * Create cache for the given name.
     *
     * @param cacheName   cache name
     * @param modifiedExp cache modified expiry time
     * @param accessExp   cache access expiry time
     * @return created cache implementation of the given cache name
     */
    private static Cache createCache(final String cacheName, final long modifiedExp, final long accessExp) {

        return Caching.getCacheManager(CertificateValidatorConstants.OB_CACHE_MANAGER)
                .createCacheBuilder(cacheName)
                .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                        modifiedExp))
                .setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                        accessExp)).setStoreByValue(false).build();
    }

    /**
     * Get client cert cache. This cache will be used to store the certificate validity status of a client certificate.
     *
     * @return cache implementation of the client cert cache.
     */
    public static Cache getClientCertCache() {

        if (!clientCertCacheInit) {
            // Cache expiry time has been configured in the open-banking.xml. Default value is 3600 seconds.
            Long cacheExpiry = CommonConfigParser.getInstance().getClientCertificateCacheExpiry();
            Cache cache = createCache(CertificateValidatorConstants.CLIENT_CERT_CACHE, cacheExpiry, cacheExpiry);
            clientCertCacheInit = true;
            return cache;
        } else {
            return Caching.getCacheManager(CertificateValidatorConstants.OB_CACHE_MANAGER)
                    .getCache(CertificateValidatorConstants.CLIENT_CERT_CACHE);
        }
    }

    /**
     * Get client cert issuer validation cache. This cache will be used to store whether the issuer of the
     * certificate is trusted.
     *
     * @return cache implementation of the client cert issuer validation cache.
     */
    public static Cache getClientCertIssuerValidationCache() {

        if (!clientCertIssuerValidationCacheInit) {
            // Cache expiry time has been configured in the open-banking.xml. Default value is 3600 seconds.
            Long cacheExpiry = CommonConfigParser.getInstance().getClientCertificateCacheExpiry();
            Cache cache = createCache(CertificateValidatorConstants.CLIENT_CERT_ISSUER_VALIDATION_CACHE,
                    cacheExpiry, cacheExpiry);
            clientCertIssuerValidationCacheInit = true;
            return cache;
        } else {
            return Caching.getCacheManager(CertificateValidatorConstants.OB_CACHE_MANAGER)
                    .getCache(CertificateValidatorConstants.CLIENT_CERT_ISSUER_VALIDATION_CACHE);
        }
    }

    /**
     * Loads the Truststore.
     *
     * @param trustStorePath     truststore path
     * @param trustStorePassword truststore password
     */
    public static synchronized void loadTrustStore(String trustStorePath, char[] trustStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        try (InputStream inputStream = new FileInputStream(trustStorePath)) {
            trustStore = KeyStore.getInstance(CertificateValidatorConstants.TRUSTSTORE_CONF_TYPE_DEFAULT);
            trustStore.load(inputStream, trustStorePassword);
        }
    }
}
