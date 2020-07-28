package com.alrayan.wso2.common.jwt;

import com.alrayan.wso2.common.crypto.ARBSignatureHandler;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * JWT builder class.
 *
 * @since 1.0.0
 */
public class ARBJWTTokenService extends ARBSignatureHandler implements Serializable {
    private static final long serialVersionUID = -3413082445536581759L;
    private static Logger log = LoggerFactory.getLogger(ARBJWTTokenService.class);

    private static final String SHA256_WITH_RSA = "SHA256withRSA";
    private String signatureAlgorithm = SHA256_WITH_RSA;
    private static final String NONE = "NONE";


    public String createJWT(String message,
                            PrivateKey privateKey) {

        String jwtHeader = buildHeader();
        String base64EncodedHeader = Base64Utils.encode(jwtHeader.getBytes(StandardCharsets.UTF_8));
        String base64EncodedMessageBody = Base64Utils.encode(message.getBytes(StandardCharsets.UTF_8));
        String base64EncodedMessageSignature = Base64Utils.encode(signMessage(base64EncodedMessageBody, privateKey));
        String jwt = base64EncodedHeader + "." + base64EncodedMessageBody + "." + base64EncodedMessageSignature;

        return jwt;
    }


    public String createJWT(String message) {

        String jwtHeader = buildHeader();
        String base64EncodedHeader = Base64Utils.encode(jwtHeader.getBytes(StandardCharsets.UTF_8));
        String base64EncodedMessageBody = Base64Utils.encode(message.getBytes(StandardCharsets.UTF_8));
        String jwt = base64EncodedHeader + "." + base64EncodedMessageBody;

        return jwt;
    }

    public String buildHeader() {

        StringBuilder jwtHeaderBuilder = new StringBuilder();
        jwtHeaderBuilder.append("{\"alg\":\"");

        if (NONE.equals(signatureAlgorithm)) {
            jwtHeaderBuilder.append(JWTSignatureAlg.NONE.getJwsCompliantCode());
            jwtHeaderBuilder.append("\"");
        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            jwtHeaderBuilder.append(JWTSignatureAlg.SHA256_WITH_RSA.getJwsCompliantCode());
            jwtHeaderBuilder.append("\",");
        }
        jwtHeaderBuilder.append("\"typ\":\"JWT\"");
        jwtHeaderBuilder.append("}");
        return jwtHeaderBuilder.toString();
    }

    public String getJWTHeader(String consentjwt) throws Exception {

        String[] consentValues = consentjwt.split("\\.");
        if (StringUtils.isEmpty(consentjwt)) {
            log.error("Consent value is empty and cannnot parse the JWT");
            throw new Exception("Consent value is empty and cannnot parse the JWT");
        }

        return consentValues[0];
    }

    public String getJWTBody(String consentjwt) throws Exception {

        String[] consentValues = consentjwt.split("\\.");
        if (StringUtils.isEmpty(consentjwt) || consentValues.length < 2) {
            log.error("Consent body is empty, cannot be parsed");
            throw new Exception("Cannot parse the consent object body");
        }
        return consentValues[1];
    }

    public String getJWTSignature(String consentjwt) throws Exception {

        String[] consentValues = consentjwt.split("\\.");
        if (StringUtils.isEmpty(consentjwt) || consentValues.length != 3) {
            throw new Exception("Consent signature is not found");
        }
        return consentValues[2];
    }

}

