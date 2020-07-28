package com.wso2.finance.open.banking.eidas.validator;

import com.wso2.finance.open.banking.common.config.CommonConfigParser;
import com.wso2.finance.open.banking.common.exception.CertificateValidationException;
import com.wso2.finance.open.banking.eidas.validator.dto.CertificateValidatorErrorResponse;
import com.wso2.finance.open.banking.eidas.validator.dto.CertificateValidatorResponse;
import com.wso2.finance.open.banking.eidas.validator.model.CertificateContent;
import com.wso2.finance.open.banking.eidas.validator.service.CertRevocationValidationManager;
import com.wso2.finance.open.banking.eidas.validator.service.CertRevocationValidationManagerImpl;
import com.wso2.finance.open.banking.eidas.validator.util.CertificateValidationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.springframework.http.HttpStatus;
import org.wso2.carbon.base.ServerConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Certificate validator API.
 */
public class ApplicationCertificateValidator {

    private static final Log log = LogFactory.getLog(ApplicationCertificateValidator.class);

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/application-registration")
    @Produces("application/json; charset=utf-8")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response validateCertificate(@Multipart(value = "certificate") Attachment attr) throws IOException,
            CertificateException {

        InputStream in = attr.getObject(InputStream.class);
        CertificateValidatorResponse certificateValidatorResponse = new CertificateValidatorResponse();
        CertificateValidatorErrorResponse certificateValidatorErrorResponse = new CertificateValidatorErrorResponse();
        certificateValidatorErrorResponse.setErrorCode(HttpStatus.BAD_REQUEST.value());

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(in);
        try {
            if ((isValidClientCertificate(x509Certificate)) && (certificateRevocationValidation(x509Certificate))) {
                CertificateContent certificateContent = CertificateContentExtractor.extract(x509Certificate);
                certificateValidatorResponse.setIsIssuerTrusted(true);
                certificateValidatorResponse.setIsCertificateRevoked(false);
                certificateValidatorResponse.setCertificateContent(certificateContent.toString());
                return Response.status(HttpStatus.OK.value()).entity(certificateValidatorResponse.toString()).build();
            }
            certificateValidatorErrorResponse.setErrorMessage("Certificate validation failed");
        } catch (CertificateValidationException | IOException | CertificateException | NoSuchAlgorithmException
                | KeyStoreException e) {
            log.error(e);
            certificateValidatorErrorResponse.setErrorMessage(e.getMessage());
        }
        return Response.status(HttpStatus.BAD_REQUEST.value()).entity(certificateValidatorErrorResponse.toString())
                .build();
    }

    private boolean isValidClientCertificate(X509Certificate x509Certificate) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException, CertificateValidationException {

        CertificateValidationUtil.loadTrustStore(ServerConfiguration.getInstance()
                        .getFirstProperty("Security.ProductionTrustStore.Location"),
                ServerConfiguration.getInstance().getFirstProperty("Security.ProductionTrustStore.Password")
                        .toCharArray());
        log.info("client truststore successfully loaded into certificate validator");

        CertificateValidationUtil.getIssuerCertificateFromTruststore(x509Certificate);
        return !CertificateValidationUtil.isExpired(x509Certificate);
    }

    private boolean certificateRevocationValidation(X509Certificate x509Certificate)
            throws CertificateValidationException {

        // Check certificate expiry.
        if (CertificateValidationUtil.isExpired(x509Certificate)) {
            throw new CertificateValidationException("Certificate with the serial number " +
                    x509Certificate.getSerialNumber() + " issued by the CA " +
                    x509Certificate.getIssuerDN().toString() + " is expired");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Client certificate expiry validation completed successfully");
            }
        }

        Cache clientCertCache =
                CertificateValidationUtil.getClientCertCache();
        String certificateValidationCacheKey =
                x509Certificate.getIssuerDN().getName() + x509Certificate.getSerialNumber().toString();
        if (clientCertCache.containsKey(certificateValidationCacheKey)) {
            if (log.isDebugEnabled()) {
                log.debug("Fetched certificate validation information from cache");
            }
            return Boolean.valueOf(clientCertCache.get(certificateValidationCacheKey).toString());
        } else {
            Integer certificateRevocationValidationRetryCount =
                    CommonConfigParser.getInstance().getCertificateRevocationValidationRetryCount();

            boolean isValid;
            // Check certificate revocation status.
            if (CommonConfigParser.getInstance().isCertificateRevocationValidationEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Client certificate revocation validation is enabled");
                }

                // Skip certificate revocation validation if the certificate is self-signed.
                if (x509Certificate.getSubjectDN().getName().equals(x509Certificate.getIssuerDN().getName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Client certificate is self signed. Hence, excluding the certificate revocation " +
                                "validation");
                    }
                    return true;
                }

                List<String> revocationValidationExcludedIssuers =
                        CommonConfigParser.getInstance().getCertificateRevocationValidationExcludedIssuers();
                if (revocationValidationExcludedIssuers.contains(x509Certificate.getIssuerDN().getName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("The issuer of the client certificate has been configured to exclude from " +
                                "certificate revocation validation. Hence, excluding the certificate " +
                                "revocation validation");
                    }
                    return true;
                }

                // Get issuer certificate from the truststore to continue with the certificate validation.
                java.security.cert.X509Certificate issuerCertificate =
                        CertificateValidationUtil.getIssuerCertificateFromTruststore(x509Certificate);

                CertRevocationValidationManager certRevocationValidationManager;
                String certificateRevocationValidatorImplClass =
                        CommonConfigParser.getInstance().getRevocationValidationManagerImpl();
                if (certificateRevocationValidatorImplClass != null) {
                    try {
                        certRevocationValidationManager =
                                (CertRevocationValidationManager) Class.forName(certificateRevocationValidatorImplClass)
                                        .newInstance();
                    } catch (ClassNotFoundException e) {
                        throw new CertificateValidationException("Unable to find the certificate revocation " +
                                "validation manager class implementation", e);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new CertificateValidationException("Error occurred while loading the certificate " +
                                "revocation validation manager class implementation", e);
                    }
                } else {
                    // Use the default certificate revocation validator class implementation.
                    certRevocationValidationManager = new CertRevocationValidationManagerImpl();
                }
                isValid = certRevocationValidationManager.verify(x509Certificate, issuerCertificate,
                        certificateRevocationValidationRetryCount);
            } else {
                isValid = true;
            }

            clientCertCache.put(certificateValidationCacheKey, isValid);
            if (log.isDebugEnabled()) {
                log.debug("Stored certificate validation status in cache");
            }
            return isValid;
        }
    }
}
