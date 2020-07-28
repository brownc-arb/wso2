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
package com.wso2.finance.open.banking.eidas.validator.revocation;

import com.wso2.finance.open.banking.common.config.CommonConfigParser;
import com.wso2.finance.open.banking.common.exception.CertificateValidationException;
import com.wso2.finance.open.banking.eidas.validator.model.RevocationStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is used to verify whether a certificate is revoked or not by using the Certificate Revocation List published
 * by the CA.
 */
public class CRLValidator implements RevocationValidator {

    private static final Log log = LogFactory.getLog(CRLValidator.class);

    private int retryCount;

    public CRLValidator(int retryCount) {

        this.retryCount = retryCount;
    }

    /**
     * Checks revocation status (Good, Revoked) of the peer certificate.
     *
     * @param peerCert   peer certificate
     * @param issuerCert issuer certificate of the peer
     * @return revocation status of the peer certificate
     * @throws CertificateValidationException certificateValidationException
     */
    @Override
    public RevocationStatus checkRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateValidationException {

        List<String> crlUrls = getCRLUrls(peerCert);
        return getCRLRevocationStatus(peerCert, issuerCert, retryCount, crlUrls);
    }

    @Override
    public int getRetryCount() {

        return retryCount;
    }

    /**
     * ****************************************
     * Util methods for CRL Validation
     * ****************************************
     */

    /**
     * Extracts all CRL distribution point URLs from the "CRL Distribution Point" extension in a X.509 certificate.
     * If CRL distribution point extension or CRL Urls are unavailable, throw an exception.
     *
     * @param cert X509 certificate
     * @return List of CRL Urls in the certificate
     * @throws CertificateValidationException certificateValidationException
     */
    public static List<String> getCRLUrls(X509Certificate cert) throws CertificateValidationException {

        List<String> crlUrls;
        byte[] crlDPExtensionValue = getCRLDPExtensionValue(cert);
        if (crlDPExtensionValue == null) {
            throw new CertificateValidationException("Certificate with serial num:" + cert.getSerialNumber()
                    + " doesn't have CRL Distribution points");
        }
        CRLDistPoint distPoint = getCrlDistPoint(crlDPExtensionValue);
        crlUrls = getCrlUrlsFromDistPoint(distPoint);

        if (crlUrls.isEmpty()) {
            throw new CertificateValidationException("Cannot get CRL urls from certificate with serial num:" +
                    cert.getSerialNumber());
        }
        return crlUrls;
    }

    /**
     * Get revocation status of a certificate using CRL Url.
     *
     * @param peerCert   peer certificate
     * @param retryCount retry count to connect to CRL Url and get the CRL
     * @param crlUrls    List of CRL Urls
     * @return Revocation status of the certificate
     * @throws CertificateValidationException certificateValidationException
     */
    public static RevocationStatus getCRLRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert,
                                                          int retryCount,
                                                          List<String> crlUrls)
            throws CertificateValidationException {

        // Check with distributions points in the list one by one. if one fails go to the other.
        for (String crlUrl : crlUrls) {
            if (log.isDebugEnabled()) {
                log.debug("Trying to get CRL for URL: " + crlUrl);
            }
            X509CRL x509CRL = downloadCRLFromWeb(crlUrl, retryCount, peerCert, issuerCert);
            if (x509CRL != null) {
                return getRevocationStatusFromCRL(x509CRL, peerCert);
            }
        }
        throw new CertificateValidationException("Cannot check revocation status with the certificate");
    }

    private static boolean isValidX509Crl(X509CRL x509CRL, X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateValidationException {

        Date currentDate = new Date();
        Date nextUpdate = x509CRL.getNextUpdate();
        boolean isValid = false;

        if (isValidX509CRLFromIssuer(x509CRL, peerCert, issuerCert)) {
            isValid = isValidX509CRLFromNextUpdate(x509CRL, currentDate, nextUpdate);
        }
        return isValid;
    }

    private static boolean isValidX509CRLFromIssuer(X509CRL x509CRL, X509Certificate peerCert,
                                                    X509Certificate issuerCert)
            throws CertificateValidationException {

        if (!peerCert.getIssuerDN().equals(x509CRL.getIssuerDN())) {
            throw new CertificateValidationException("X509 CRL is not valid. Issuer DN in the peer " +
                    "certificate: " + peerCert.getIssuerDN() + " does not match with the Issuer DN in the X509 CRL: " +
                    x509CRL.getIssuerDN());
        }

        // Verify the signature of the CRL.
        try {
            x509CRL.verify(issuerCert.getPublicKey());
            return true;
        } catch (CRLException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException |
                SignatureException e) {
            throw new CertificateValidationException("CRL signature cannot be verified", e);
        }
    }

    private static boolean isValidX509CRLFromNextUpdate(X509CRL x509CRL, Date currentDate, Date nextUpdate)
            throws CertificateValidationException {

        if (nextUpdate != null) {
            if (log.isDebugEnabled()) {
                log.debug("Validating the next update date: " + nextUpdate.toString() + " with the current date: " +
                        currentDate.toString());
            }
            if (currentDate.before(x509CRL.getNextUpdate())) {
                return true;
            } else {
                throw new CertificateValidationException("X509 CRL is not valid. Next update date: " +
                        nextUpdate.toString() + " is before the current date: " + currentDate.toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't validate the X509 CRL, next update date is not available.");
            }
        }
        return false;
    }

    private static X509CRL downloadCRLFromWeb(String crlURL, int retryCount, X509Certificate peerCert,
                                              X509Certificate issuerCert)
            throws CertificateValidationException {

        X509CRL x509CRL = null;
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance();
        boolean isCertificateRevocationProxyEnabled = commonConfigParser.isCertificateRevocationProxyEnabled();
        if (log.isDebugEnabled()) {
            log.debug("Certificate revocation check proxy enabled: " + isCertificateRevocationProxyEnabled);
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet httpGet = new HttpGet(crlURL);
            if (isCertificateRevocationProxyEnabled) {
                log.debug("Setting certificate revocation proxy started.");
                String certificateRevocationProxyHost = commonConfigParser.getCertificateRevocationProxyHost();
                if (StringUtils.isEmpty(certificateRevocationProxyHost)) {
                    String message = "Certificate revocation proxy server host is not configured. Please do set the " +
                            "'CertificateManagement -> CertificateRevocationProxy -> ProxyHost' file";
                    log.error(message);
                    throw new CertificateValidationException(message);
                }
                int certificateRevocationProxyPort = commonConfigParser.getCertificateRevocationProxyPort();
                if (log.isDebugEnabled()) {
                    log.debug("Certificate revocation proxy: " + certificateRevocationProxyHost + ":" +
                            certificateRevocationProxyPort);
                }
                HttpHost proxy = new HttpHost(certificateRevocationProxyHost, certificateRevocationProxyPort);
                RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
                httpGet.setConfig(config);
                log.debug("Setting certificate revocation proxy finished.");
            }
            HttpResponse httpResponse = client.execute(httpGet);
            //Check errors in response:
            if (httpResponse.getStatusLine().getStatusCode() / 100 != 2) {
                throw new CertificateValidationException("Error getting crl response." +
                        "Response code is " + httpResponse.getStatusLine().getStatusCode());
            }
            InputStream in = httpResponse.getEntity().getContent();

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL x509CRLDownloaded = (X509CRL) cf.generateCRL(in);
            if (log.isDebugEnabled()) {
                log.debug("CRL is downloaded from CRL Url: " + crlURL);
            }

            if (isValidX509Crl(x509CRLDownloaded, peerCert, issuerCert)) {
                x509CRL = x509CRLDownloaded;
            }
        } catch (MalformedURLException e) {
            throw new CertificateValidationException("CRL Url is malformed", e);
        } catch (IOException e) {
            if (retryCount == 0) {
                throw new CertificateValidationException("Cant reach the CRL Url: " + crlURL, e);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Cant reach CRL Url: " + crlURL + ". Retrying to connect - attempt " + retryCount);
                }
                return downloadCRLFromWeb(crlURL, --retryCount, peerCert, issuerCert);
            }
        } catch (CertificateException e) {
            throw new CertificateValidationException("Error when generating certificate factory.", e);
        } catch (CRLException e) {
            throw new CertificateValidationException("Cannot generate X509CRL from the stream data", e);
        }
        return x509CRL;
    }

    private static RevocationStatus getRevocationStatusFromCRL(X509CRL x509CRL, X509Certificate peerCert) {

        if (x509CRL.isRevoked(peerCert)) {
            return RevocationStatus.REVOKED;
        } else {
            return RevocationStatus.GOOD;
        }
    }

    private static byte[] getCRLDPExtensionValue(X509Certificate cert) {

        //DER-encoded octet string of the extension value for CRLDistributionPoints identified by the passed-in oid
        return cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
    }

    private static CRLDistPoint getCrlDistPoint(byte[] crlDPExtensionValue) throws CertificateValidationException {

        //crlDPExtensionValue is encoded in ASN.1 format
        //DER (Distinguished Encoding Rules) is one of ASN.1 encoding rules defined in ITU-T X.690, 2002, specification.
        //ASN.1 encoding rules can be used to encode any data object into a binary file. Read the object in octets.
        CRLDistPoint distPoint;
        try (ASN1InputStream crlDPEx = new ASN1InputStream(crlDPExtensionValue);
             ASN1InputStream asn1InOctets =
                     new ASN1InputStream(((DEROctetString) (crlDPEx).readObject()).getOctets())) {
            //Get Input stream in octets
            ASN1Primitive crlDERObject = asn1InOctets.readObject();
            distPoint = CRLDistPoint.getInstance(crlDERObject);
        } catch (IOException e) {
            throw new CertificateValidationException("Cannot read certificate to get CRL urls", e);
        }
        return distPoint;
    }

    private static List<String> getCrlUrlsFromDistPoint(CRLDistPoint distPoint) {

        List<String> crlUrls = new ArrayList<>();
        //Loop through ASN1Encodable DistributionPoints
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            //get ASN1Encodable DistributionPointName
            DistributionPointName dpn = dp.getDistributionPoint();
            if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                //Create ASN1Encodable General Names
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                // Look for a URI
                for (GeneralName genName : genNames) {
                    if (genName.getTagNo() == GeneralName.uniformResourceIdentifier) {
                        //DERIA5String contains an ascii string.
                        //A IA5String is a restricted character string type in the ASN.1 notation
                        String url = DERIA5String.getInstance(genName.getName()).getString().trim();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }
}
