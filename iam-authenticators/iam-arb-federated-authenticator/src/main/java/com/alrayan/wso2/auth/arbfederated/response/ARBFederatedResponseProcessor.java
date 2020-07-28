package com.alrayan.wso2.auth.arbfederated.response;

import com.alrayan.wso2.common.crypto.ARBSignatureUtils;
import com.alrayan.wso2.common.jwt.ARBJWTTokenService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

/**
 * ARBFederatedResponseProcessor to process response.
 *
 * @since 1.0.0
 */
public class ARBFederatedResponseProcessor {

    private static final String USER_NAME = "salesforceId";


    ARBSignatureUtils arbSignatureUtils = new ARBSignatureUtils();
    ARBJWTTokenService arbjwtTokenService = new ARBJWTTokenService();

    /**
     * Returns true if the signature validation passes.
     *
     * @param dbpResponse consent details that application received back.
     * @return map which contains the signature of the consent is verified
     */
    public boolean verifyDBPSignature(AuthenticationContext context, String dbpResponse)
            throws AuthenticationFailedException {
        try {
            return arbSignatureUtils.verifySignature(context, dbpResponse);
        } catch (Exception e) {
            throw new AuthenticationFailedException("Exception occured during the signature validation");
        }
    }

    public String retirveUsername(String dbpResponse) {

        JSONParser parser = new JSONParser();
        String salesforceID = null;
        String payload;

        try {
            payload = arbjwtTokenService.getJWTBody(dbpResponse);
            JSONObject jsonObject = (JSONObject) parser.parse
                    (new String(java.util.Base64.getDecoder().decode(payload)));

            // extract client id
            if (jsonObject.containsKey(USER_NAME)) {
                salesforceID = (String) jsonObject.get(USER_NAME);
            }

        } catch (Exception e) {
        }

        return salesforceID;

    }
}
