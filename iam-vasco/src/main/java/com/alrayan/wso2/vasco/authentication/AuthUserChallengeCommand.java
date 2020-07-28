package com.alrayan.wso2.vasco.authentication;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.HTTPClientException;
import com.alrayan.wso2.common.utils.HTTPClientUtil;
import com.alrayan.wso2.vasco.VASCOCommand;
import com.alrayan.wso2.vasco.VASCOException;
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
 * This class is responsible for executing the VASCO auth user challenge command.
 *
 * @since 1.0.0
 */
public class AuthUserChallengeCommand implements VASCOCommand<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(AuthUserChallengeCommand.class);
    private String userId;
    private String password;
    private String secureChallengeCode;
    private String appName;

    /**
     * Constructs an instance of {@link AuthUserChallengeCommand}.
     *
     * @param userId              Salesforce ID of the user
     * @param password            CRONTO code shown by the digipass device
     * @param secureChallengeCode secure challenge code received by executing the {@link SecureChallengeCommand}
     * @param appName             service provider application name
     */
    public AuthUserChallengeCommand(String userId, String password, String secureChallengeCode, String appName) {
        this.userId = userId;
        this.password = password;
        this.secureChallengeCode = secureChallengeCode;
        this.appName = appName;
    }

    @Override
    public Boolean execute() throws VASCOException {
        HTTPClientUtil.Response response = null;
        try {
            response = invokeAuthUserChallengeKey(userId, password, secureChallengeCode,
                    appName);
            return SOAPUtil.isStatSuccess(response);
        } catch (NoSuchElementException e) {
            log.error(AlRayanError.VASCO_AUTH_USER_CHALLENGE_RESPONSE_NOT_FOUND.getErrorMessageWithCode() + " - {" +
                      "response body: " + (response != null ? response.getResponseBody() : "") +
                      "}");
            throw new VASCOException(AlRayanError.VASCO_AUTH_USER_CHALLENGE_RESPONSE_NOT_FOUND
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Obtains the challenge key for the obtained secure code challenge.
     *
     * @param userId              user ID
     * @param password            user password
     * @param secureChallengeCode challenge key received from {@code getSecureChallenge()} method
     * @param appName             service provider application name
     * @return {@link HTTPClientUtil.Response} containing the secure code challenge
     * @throws VASCOException thrown when error on preparing request or sending the request
     */
    private HTTPClientUtil.Response invokeAuthUserChallengeKey(String userId, String password,
                                                               String secureChallengeCode, String appName)
            throws VASCOException {
        String soapMessageString = "";
        String vascoURL = "";
        try {
            SOAPMessage soapMessage = SOAPUtil.createSOAPMessage();
            SOAPBody soapBody = SOAPUtil.getSOAPBody(soapMessage, SOAPNamespace.AUT);

            QName qname = new QName(SOAPNamespace.AUT.getPrefix() + ":authUser");
            SOAPBodyElement logonElement = soapBody.addBodyElement(qname);
            SOAPElement attributeSetElement = logonElement.addChildElement("credentialAttributeSet");

            Map<String, SOAPValueElement> attributeMap = new HashMap<>();
            attributeMap.put("CREDFLD_COMPONENT_TYPE", new SOAPValueElement("SIGNATURE", SOAPElementType.STRING));
            attributeMap.put("CREDFLD_USERID", new SOAPValueElement(userId, SOAPElementType.STRING));
            attributeMap.put("CREDFLD_PASSWORD_FORMAT", new SOAPValueElement("0", SOAPElementType.UNSIGNED_INTEGER));
            attributeMap.put("CREDFLD_PASSWORD", new SOAPValueElement(password, SOAPElementType.STRING));
            attributeMap
                    .put("CREDFLD_CHALLENGE_KEY", new SOAPValueElement(secureChallengeCode, SOAPElementType.STRING));

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
            log.error(AlRayanError.ERROR_CONSTRUCTING_SOAP_BODY_CONTENT.getErrorMessageWithCode());
            throw new VASCOException(AlRayanError.ERROR_CONSTRUCTING_SOAP_MESSAGE.getErrorMessageWithCode(), e);
        }
    }
}
