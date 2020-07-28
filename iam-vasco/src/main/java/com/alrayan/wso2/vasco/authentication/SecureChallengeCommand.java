package com.alrayan.wso2.vasco.authentication;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.HTTPClientException;
import com.alrayan.wso2.common.utils.HTTPClientUtil;
import com.alrayan.wso2.vasco.VASCOCommand;
import com.alrayan.wso2.vasco.VASCOException;
import com.alrayan.wso2.vasco.model.SecureChallengeResult;
import com.alrayan.wso2.vasco.soap.SOAPElementType;
import com.alrayan.wso2.vasco.soap.SOAPNamespace;
import com.alrayan.wso2.vasco.soap.SOAPUtil;
import com.alrayan.wso2.vasco.soap.SOAPValueElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * This class is responsible for executing the VASCO secure challenge command.
 *
 * @since 1.0.0
 */
public class SecureChallengeCommand implements VASCOCommand<SecureChallengeResult> {

    private static final Logger log = LoggerFactory.getLogger(SecureChallengeCommand.class);
    private String userId;
    private String message;
    private String appName;

    /**
     * Constructs an instance of {@link SecureChallengeCommand}.
     *
     * @param userId  Salesforce ID of the user
     * @param message message to embed in the CRONTO image
     * @param appName service provider application name
     */
    public SecureChallengeCommand(String userId, String message, String appName) {
        this.userId = userId;
        this.message = message;
        this.appName = appName;
    }

    @Override
    public SecureChallengeResult execute() throws VASCOException {
        HTTPClientUtil.Response response = null;
        try {
            response = invokeGetSecureChallenge(userId, message, appName);
            boolean isSuccess = SOAPUtil.isStatSuccess(response);
            if (!isSuccess) {
                return getErrorSecureChallengeResult(response);
            }
            String requestMessage = SOAPUtil.getSOAPBodyAttributeValue(response, "CREDFLD_REQUEST_MESSAGE")
                    .replace("\n", "");
            String challengeKey = SOAPUtil.getSOAPBodyAttributeValue(response, "CREDFLD_CHALLENGE_KEY");
            return new SecureChallengeResult(challengeKey, requestMessage);
        } catch (NoSuchElementException e) {
            log.error(AlRayanError.VASCO_SECURE_CHALLENGE_RESPONSE_NOT_FOUND.getErrorMessageWithCode() + " - {" +
                      "response body: " + (response != null ? response.getResponseBody() : "") +
                      "}");
            throw new VASCOException(AlRayanError.VASCO_SECURE_CHALLENGE_RESPONSE_NOT_FOUND
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Returns the erroneous secure challenge result.
     *
     * @param response HTTP response
     * @return erroneous secure challenge result
     */
    private SecureChallengeResult getErrorSecureChallengeResult(HTTPClientUtil.Response response) {
        SecureChallengeResult secureChallengeResult = new SecureChallengeResult(null, null);
        secureChallengeResult.setError(true);
        try {
            String error = SOAPUtil.getSOAPBodyAttributeValue(response, "CREDFLD_STATUS_MESSAGE");
            secureChallengeResult.setErrorMessage(error);
        } catch (VASCOException | NoSuchElementException e) {
            log.error("Error on getting VASCO secure challenge error message - {" +
                      "response body: " + response.getResponseBody() +
                      "}");
        }
        return secureChallengeResult;
    }


    /**
     * Obtains the secure code challenge.
     *
     * @param userId  user ID to obtain the secure code challenge
     * @param message message to embed in the CRONTO image
     * @param appName service provider application name
     * @return {@link HTTPClientUtil.Response} containing the secure code challenge
     * @throws VASCOException thrown when error on preparing request or sending the request
     */
    private HTTPClientUtil.Response invokeGetSecureChallenge(String userId, String message, String appName)
            throws VASCOException {
        String vascoURL = "";
        String soapMessageString = "";
        try {
            SOAPMessage soapMessage = SOAPUtil.createSOAPMessage();
            SOAPBody soapBody = SOAPUtil.getSOAPBody(soapMessage, SOAPNamespace.AUT);

            QName qname = new QName(SOAPNamespace.AUT.getPrefix() + ":getSecureChallenge");
            SOAPBodyElement logonElement = soapBody.addBodyElement(qname);
            SOAPElement attributeSetElement = logonElement.addChildElement("credentialAttributeSet");

            Map<String, SOAPValueElement> attributeMap = new HashMap<>();
            attributeMap.put("CREDFLD_COMPONENT_TYPE", new SOAPValueElement("SOAP Client", SOAPElementType.STRING));
            attributeMap.put("CREDFLD_USERID", new SOAPValueElement(userId, SOAPElementType.STRING));
            attributeMap.put("CREDFLD_CHALLENGE_MESSAGE", new SOAPValueElement(message, SOAPElementType.STRING));

            SOAPUtil.createAttributeSOAPElement(attributeSetElement, attributeMap);
            soapMessageString = SOAPUtil.convertSOAPMessageToString(soapMessage);
            vascoURL = appName.endsWith("_SANDBOX") ?
                       AlRayanConfiguration.SANDBOX_VASCO_ENDPOINT.getValue() :
                       AlRayanConfiguration.VASCO_AUTHENTICATION_URL.getValue();
            if (StringUtils.isEmpty(vascoURL)) {
                throw new VASCOException(AlRayanError.VASCO_AUTHENTICATION_URL_NOT_DEFINED.getErrorMessageWithCode());
            }

            return HTTPClientUtil.post(vascoURL)
                    .setEntity(soapMessageString, ContentType.APPLICATION_XML)
                    .execute();
        } catch (HTTPClientException e) {
            log.error(AlRayanError.ERROR_HTTP_REQUEST_TO_VASCO.getErrorMessageWithCode() + " - {" +
                      "VASCO URL: " + vascoURL + "," +
                      "SOAP request string: " + soapMessageString +
                      "}");
            throw new VASCOException(AlRayanError.ERROR_HTTP_REQUEST_TO_VASCO.getErrorMessageWithCode(), e);
        } catch (SOAPException e) {
            log.error(AlRayanError.ERROR_CONSTRUCTING_SOAP_BODY_CONTENT.getErrorMessageWithCode() + " - {" +
                      "VASCO URL: " + vascoURL + "," +
                      "SOAP request string: " + soapMessageString +
                      "}");
            throw new VASCOException(AlRayanError.ERROR_CONSTRUCTING_SOAP_BODY_CONTENT.getErrorMessageWithCode(), e);
        } catch (IOException e) {
            log.error(AlRayanError.ERROR_CONSTRUCTING_SOAP_MESSAGE.getErrorMessageWithCode());
            throw new VASCOException(AlRayanError.ERROR_CONSTRUCTING_SOAP_MESSAGE.getErrorMessageWithCode(), e);
        }
    }
}
